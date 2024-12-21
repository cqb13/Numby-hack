package cqb13.NumbyHack.utils;

import it.unimi.dsi.fastutil.ints.Int2ObjectArrayMap;
import meteordevelopment.meteorclient.events.packets.PacketEvent;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.packet.c2s.play.ClickSlotC2SPacket;
import net.minecraft.network.packet.s2c.play.EntityStatusS2CPacket;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.slot.Slot;
import net.minecraft.screen.slot.SlotActionType;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.codec.digest.DigestUtils;
import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.SecretKeySpec;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;

import static meteordevelopment.meteorclient.MeteorClient.mc;

public class CHMainUtils {
  public static void clickSlotPacket(int fromIndex, int toIndex, SlotActionType type) {
    ScreenHandler sh = mc.player.currentScreenHandler;
    Slot slot = sh.getSlot(fromIndex);
    Int2ObjectArrayMap stack = new Int2ObjectArrayMap();
    stack.put(fromIndex, slot.getStack());
    mc.player.networkHandler.sendPacket(new ClickSlotC2SPacket(sh.syncId, sh.getRevision(), slot.id, toIndex, type,
        sh.getSlot(fromIndex).getStack(), stack));
  }

  public static String encrypt(String text, String key) throws InvalidKeyException, NoSuchAlgorithmException,
      NoSuchPaddingException, IllegalBlockSizeException, BadPaddingException {
    Cipher cipher = Cipher.getInstance("AES");
    cipher.init(Cipher.ENCRYPT_MODE, new SecretKeySpec(Arrays.copyOf(DigestUtils.sha1(key), 16), "AES"));

    return Base64.encodeBase64String(cipher.doFinal(text.getBytes()));
  }

  public static String decrypt(String text, String key) throws NoSuchAlgorithmException, NoSuchPaddingException,
      InvalidKeyException, IllegalBlockSizeException, BadPaddingException {
    Cipher cipher = Cipher.getInstance("AES");
    cipher.init(Cipher.DECRYPT_MODE, new SecretKeySpec(Arrays.copyOf(DigestUtils.sha1(key), 16), "AES"));

    return new String(cipher.doFinal(Base64.decodeBase64(text)));
  }

  public static Integer lavaIsWithinRange(int range) {
    for (int i = 0; i < range; i++) {
      if (mc.world.getBlockState(mc.player.getBlockPos().down(i)).getBlock().getTranslationKey().contains("lava")) {
        return i;
      }
    }
    return -1;
  }

  public static String apply(String msg) {
    if (msg.contains(":smile:"))
      msg = msg.replace(":smile:", "☺");
    if (msg.contains(":sad:"))
      msg = msg.replace(":sad:", "☹");
    if (msg.contains(":heart:"))
      msg = msg.replace(":heart:", "❤");
    if (msg.contains(":skull:"))
      msg = msg.replace(":skull:", "☠");
    if (msg.contains(":star:"))
      msg = msg.replace(":star:", "★");
    if (msg.contains(":flower:"))
      msg = msg.replace(":flower:", "❀");
    if (msg.contains(":pick:"))
      msg = msg.replace(":pick:", "⛏");
    if (msg.contains(":wheelchair:"))
      msg = msg.replace(":wheelchair:", "♿");
    if (msg.contains(":lightning:"))
      msg = msg.replace(":lightning:", "⚡");
    if (msg.contains(":rod:"))
      msg = msg.replace(":rod:", "🎣");
    if (msg.contains(":potion:"))
      msg = msg.replace(":potion:", "🧪");
    if (msg.contains(":fire:"))
      msg = msg.replace(":fire:", "🔥");
    if (msg.contains(":shears:"))
      msg = msg.replace(":shears:", "✂");
    if (msg.contains(":bell:"))
      msg = msg.replace(":bell:", "🔔");
    if (msg.contains(":bow:"))
      msg = msg.replace(":bow:", "🏹");
    if (msg.contains(":trident:"))
      msg = msg.replace(":trident:", "🔱");
    if (msg.contains(":cloud:"))
      msg = msg.replace(":cloud:", "☁");
    if (msg.contains(":cat:"))
      msg = msg.replace(":cat:", "ᓚᘏᗢ");

    return msg;
  }

  public static Entity deadEntity;

  public static boolean isDeathPacket(PacketEvent.Receive event) {
    if (event.packet instanceof EntityStatusS2CPacket packet) {
      if (packet.getStatus() == 3) {
        deadEntity = packet.getEntity(mc.world);
        return deadEntity instanceof PlayerEntity;
      }
    }
    return false;
  }
}
