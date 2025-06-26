package cqb13.NumbyHack.modules.general;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Arrays;
import java.util.Base64;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import cqb13.NumbyHack.NumbyHack;
import meteordevelopment.meteorclient.events.game.ReceiveMessageEvent;
import meteordevelopment.meteorclient.events.game.SendMessageEvent;
import meteordevelopment.meteorclient.settings.BoolSetting;
import meteordevelopment.meteorclient.settings.ColorSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.settings.StringSetting;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.utils.render.color.SettingColor;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.text.HoverEvent;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.text.TextColor;

public class ChatEncryption extends Module {
  private final SettingGroup sgGeneral = settings.getDefaultGroup();
  private final SettingGroup sgChat = settings.createGroup("Chat");

  private final Setting<String> key = sgGeneral.add(new StringSetting.Builder()
      .name("key")
      .description("What key is used to encrypt and decrypt messages.")
      .defaultValue("Cathack")
      .build());

  private final Setting<String> secretKey = sgGeneral.add(new StringSetting.Builder()
      .name("secret-key")
      .description("A secondary key to ensure safety.")
      .defaultValue("Key")
      .build());

  private final Setting<String> prefix = sgGeneral.add(new StringSetting.Builder()
      .name("prefix")
      .description("Prefix placed before messages that are encrypted so they can be identified.")
      .defaultValue("NumbyHackChatEncryption:")
      .build());

  private final Setting<Boolean> onlySendWithPrefix = sgGeneral.add(new BoolSetting.Builder()
      .name("only-send-with-prefix")
      .description("Only sends an encrypted message if the message you send has a prefix in it.")
      .defaultValue(false)
      .build());

  private final Setting<String> sendPrefix = sgGeneral.add(new StringSetting.Builder()
      .name("send-prefix")
      .description("The prefix you will add before messagges you want to encrypt.")
      .visible(onlySendWithPrefix::get)
      .defaultValue("EC:")
      .build());

  private final Setting<Boolean> replaceOriginal = sgChat.add(new BoolSetting.Builder()
      .name("replace-original")
      .description("Repalace the original message with the decrypted.")
      .defaultValue(false)
      .build());

  public final Setting<SettingColor> feedbackColor = sgChat.add(new ColorSetting.Builder()
      .name("feedback-color")
      .description("Color of the feedback text.")
      .defaultValue(new SettingColor(73, 107, 190))
      .build());

  public ChatEncryption() {
    super(NumbyHack.CATEGORY, "chat-encryption", "Encrypts messages using a user-provided key.");
  }

  @EventHandler
  private void onSendMessage(SendMessageEvent event) {
    if (onlySendWithPrefix.get() && !event.message.startsWith(sendPrefix.get())) {
      return;
    }

    try {
      SecretKey aesKey = deriveKeyFromStrings(key.get(), secretKey.get());
      byte[] iv = generateIv();
      String encrypted = encrypt(event.message.replaceFirst(sendPrefix.get(), ""), aesKey, iv);
      event.message = prefix.get() + Base64.getEncoder().encodeToString(iv) + ":" + encrypted;
    } catch (Exception e) {
      return;
    }
  }

  @EventHandler
  public void onReceiveMessage(ReceiveMessageEvent event) {
    String string = removeAngleBracketPrefix(event.getMessage().getString());
    String author = extratMessageSender(event.getMessage().getString());

    if (string.startsWith(prefix.get())) {
      String message = string.replaceFirst(prefix.get(), "");

      String decryptedMsg = null;
      try {
        decryptedMsg = decrypt(message, deriveKeyFromStrings(key.get(), secretKey.get()));
      } catch (Exception e) {
        return;
      }

      if (replaceOriginal.get() && decryptedMsg != null) {
        event.setMessage(Text.literal("<" + author + "> " + decryptedMsg).append(Text.literal(" [Decrypted]")
            .setStyle(Style.EMPTY.withColor(TextColor.fromRgb(feedbackColor.get().getPacked())))));
      } else {
        event.setMessage(Text.literal("<" + author + "> " + string + " ").append(Text.literal("[Encrypted]")
            .setStyle(Style.EMPTY.withColor(TextColor.fromRgb(feedbackColor.get().getPacked()))
                .withHoverEvent(new HoverEvent.ShowText(Text.literal(decryptedMsg)
                    .setStyle(Style.EMPTY.withColor(TextColor.fromRgb(feedbackColor.get().getPacked()))))))));
      }
    }
  }

  public static String removeAngleBracketPrefix(String input) {
    if (input.startsWith("<") && input.contains(">")) {
      int lastAngleBracket = input.lastIndexOf('>');
      return input.substring(lastAngleBracket + 1).trim();
    }
    return input;
  }

  public static String extratMessageSender(String input) {
    if (input.startsWith("<") && input.contains(">")) {
      int closingIndex = input.indexOf('>');
      if (closingIndex > 1) {
        return input.substring(1, closingIndex);
      }
    }
    return null;
  }

  public static SecretKey deriveKeyFromStrings(String k1, String k2) throws Exception {
    MessageDigest sha = MessageDigest.getInstance("SHA-256");
    byte[] keyBytes = sha.digest((k1 + ":" + k2).getBytes(StandardCharsets.UTF_8));
    return new SecretKeySpec(Arrays.copyOf(keyBytes, 16), "AES");
  }

  public static byte[] generateIv() {
    byte[] iv = new byte[16];
    new SecureRandom().nextBytes(iv);
    return iv;
  }

  public static String encrypt(String plaintext, SecretKey key, byte[] iv) throws Exception {
    Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
    cipher.init(Cipher.ENCRYPT_MODE, key, new IvParameterSpec(iv));
    byte[] encryptedBytes = cipher.doFinal(plaintext.getBytes(StandardCharsets.UTF_8));
    return Base64.getEncoder().encodeToString(encryptedBytes);
  }

  public static String decrypt(String input, SecretKey key) throws Exception {
    String[] parts = input.split(":");
    byte[] iv = Base64.getDecoder().decode(parts[0]);
    byte[] ciphertext = Base64.getDecoder().decode(parts[1]);

    Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
    cipher.init(Cipher.DECRYPT_MODE, key, new IvParameterSpec(iv));
    byte[] decryptedBytes = cipher.doFinal(ciphertext);
    return new String(decryptedBytes, StandardCharsets.UTF_8);
  }
}
