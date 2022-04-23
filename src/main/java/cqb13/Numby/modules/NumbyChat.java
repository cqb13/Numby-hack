package cqb13.Numby.modules;

import cqb13.Numby.Numby;
import cqb13.Numby.utils.Emotes;
import it.unimi.dsi.fastutil.chars.Char2CharArrayMap;
import it.unimi.dsi.fastutil.chars.Char2CharMap;
import meteordevelopment.meteorclient.events.game.ReceiveMessageEvent;
import meteordevelopment.meteorclient.events.game.SendMessageEvent;
import meteordevelopment.meteorclient.mixin.ChatHudAccessor;
import meteordevelopment.meteorclient.settings.*;
import meteordevelopment.meteorclient.systems.commands.Commands;
import meteordevelopment.meteorclient.systems.commands.commands.SayCommand;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.utils.Utils;
import meteordevelopment.meteorclient.utils.player.ChatUtils;
import meteordevelopment.meteorclient.utils.render.color.RainbowColor;
import meteordevelopment.meteorclient.utils.render.color.SettingColor;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.client.gui.hud.ChatHudLine;
import net.minecraft.text.*;
import net.minecraft.util.Formatting;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;
/**
 * Made by cqb13
 */
public class NumbyChat extends Module {
    private final SettingGroup sgGeneral = settings.getDefaultGroup();
    private final SettingGroup sgFilter = settings.createGroup("Filter");
    private final SettingGroup sgLongerChat = settings.createGroup("Longer Chat");
    private final SettingGroup sgSuffix = settings.createGroup("Suffix");
    private final SettingGroup sgCustom = settings.createGroup("Customize");


    private final Setting<Boolean> fancy = sgGeneral.add(new BoolSetting.Builder()
            .name("fancy-chat")
            .description("Makes your messages ғᴀɴᴄʏ!")
            .defaultValue(false)
            .build()
    );
    private final Setting<Boolean> timestamps = sgGeneral.add(new BoolSetting.Builder()
            .name("timestamps")
            .description("Adds client side time stamps to the beginning of chat messages.")
            .defaultValue(false)
            .build()
    );
    private final Setting<Boolean> coordsProtection = sgGeneral.add(new BoolSetting.Builder()
            .name("coords-protection")
            .description("Prevents you from sending messages in chat that may contain coordinates.")
            .defaultValue(true)
            .build()
    );

    // Filter
    private final Setting<Boolean> antiSpam = sgFilter.add(new BoolSetting.Builder()
            .name("anti-spam")
            .description("Blocks duplicate messages from filling your chat.")
            .defaultValue(true)
            .build()
    );
    private final Setting<Integer> antiSpamDepth = sgFilter.add(new IntSetting.Builder()
            .name("depth")
            .description("How many messages to filter.")
            .defaultValue(20)
            .min(1)
            .sliderMin(1)
            .visible(antiSpam::get)
            .build()
    );
    private final Setting<Boolean> filterRegex = sgFilter.add(new BoolSetting.Builder()
            .name("filter-regex")
            .description("Filter out chat messages that match the regex filter.")
            .defaultValue(false)
            .build()
    );
    private final Setting<List<String>> regexFilters = sgFilter.add(new StringListSetting.Builder()
            .name("regex-filter")
            .description("Regex filter used for filtering chat messages.")
            .visible(filterRegex::get)
            .build()
    );

    // Longer chat
    private final Setting<Boolean> infiniteChatBox = sgLongerChat.add(new BoolSetting.Builder()
            .name("infinite-chat-box")
            .description("Lets you type infinitely long messages.")
            .defaultValue(true)
            .build()
    );
    private final Setting<Boolean> longerChatHistory = sgLongerChat.add(new BoolSetting.Builder()
            .name("longer-chat-history")
            .description("Extends chat length.")
            .defaultValue(true)
            .build()
    );
    private final Setting<Integer> longerChatLines = sgLongerChat.add(new IntSetting.Builder()
            .name("extra-lines")
            .description("The amount of extra chat lines.")
            .defaultValue(1000)
            .min(100)
            .sliderRange(100, 1000)
            .visible(longerChatHistory::get)
            .build()
    );

    // Suffix
    private final Setting<Boolean> suffix = sgSuffix.add(new BoolSetting.Builder()
            .name("suffix")
            .description("Adds a suffix to your chat messages.")
            .defaultValue(false)
            .build()
    );
    private final Setting<Boolean> suffixRandom = sgSuffix.add(new BoolSetting.Builder()
            .name("random")
            .description("Uses a random number as your suffix.")
            .defaultValue(false)
            .build()
    );
    private final Setting<String> suffixText = sgSuffix.add(new StringSetting.Builder()
            .name("text")
            .description("The text to add as your suffix.")
            .defaultValue(" ❭ 81")
            .visible(() -> !suffixRandom.get())
            .build()
    );
    private final Setting<Boolean> suffixSmallCaps = sgSuffix.add(new BoolSetting.Builder()
            .name("small-caps")
            .description("Uses small caps in the suffix.")
            .defaultValue(true)
            .visible(() -> !suffixRandom.get())
            .build()
    );

    // Customize
    public final Setting<Boolean> emotes = sgCustom.add(new BoolSetting.Builder().name("emotes").description("Enables the Ghostware emote system.").defaultValue(false).build());
    public final Setting<Boolean> customPrefix = sgCustom.add(new BoolSetting.Builder().name("custom-prefix").description("Lets you set a custom prefix.").defaultValue(false).build());
    public final Setting<String> prefixText = sgCustom.add(new StringSetting.Builder().name("custom-prefix-text").description("Override the [Numby hack] prefix.").defaultValue("Numby hack").visible(customPrefix :: get).build());
    public final Setting<Boolean> customPrefixColor = sgCustom.add(new BoolSetting.Builder().name("custom-prefix-color").description("Lets you set a custom prefix.").defaultValue(false).build());
    public final Setting<SettingColor> prefixColor = sgCustom.add(new ColorSetting.Builder().name("prefix-color").description("Color of the prefix text.").defaultValue(new SettingColor(146, 188, 98)).visible(customPrefixColor :: get).build());
    public final Setting<Boolean> chromaPrefix = sgCustom.add(new BoolSetting.Builder().name("chroma-prefix").description("Lets you set a custom prefix.").defaultValue(false).build());
    public final Setting<Double> chromaSpeed = sgCustom.add(new DoubleSetting.Builder().name("chroma-speed").description("Speed of the chroma animation.").defaultValue(0.09).min(0.01).sliderMax(5).decimalPlaces(2).visible(chromaPrefix :: get).build());
    public final Setting<Boolean> themeBrackets = sgCustom.add(new BoolSetting.Builder().name("apply-to-brackets").description("Apply the current prefix theme to the brackets.").defaultValue(false).build());
    public final Setting<Boolean> customBrackets = sgCustom.add(new BoolSetting.Builder().name("custom-brackets").description("Set custom brackets.").defaultValue(false).build());
    public final Setting<String> leftBracket = sgCustom.add(new StringSetting.Builder().name("left-bracket").description("").defaultValue("[").visible(customBrackets :: get).build());
    public final Setting<String> rightBracket = sgCustom.add(new StringSetting.Builder().name("right-bracket").description("").defaultValue("]").visible(customBrackets :: get).build());

    private final Char2CharMap SMALL_CAPS = new Char2CharArrayMap();
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm");
    RainbowColor prefixChroma = new RainbowColor();


    public NumbyChat() {
        super(Numby.CATEGORY, "numby-chat", "Improves your chat experience in various ways.");

        String[] a = "abcdefghijklmnopqrstuvwxyz".split("");
        String[] b = "ᴀʙᴄᴅᴇꜰɢʜɪᴊᴋʟᴍɴᴏᴩqʀꜱᴛᴜᴠᴡxyᴢ".split("");
        for (int i = 0; i < a.length; i++) SMALL_CAPS.put(a[i].charAt(0), b[i].charAt(0));
    }

    @EventHandler
    private void onMessageReceive(ReceiveMessageEvent event) {
        ((ChatHudAccessor) mc.inGameHud.getChatHud()).getVisibleMessages().removeIf((message) -> message.getId() == event.id && event.id != 0);
        ((ChatHudAccessor) mc.inGameHud.getChatHud()).getMessages().removeIf((message) -> message.getId() == event.id && event.id != 0);

        Text message = event.getMessage();

        if (filterRegex.get()) {
            for (int i = 0; i < regexFilters.get().size(); i++) {
                Pattern p;

                try {
                    p = Pattern.compile(regexFilters.get().get(i));
                }
                catch (PatternSyntaxException e) {
                    error("Removing Invalid regex: %s", regexFilters.get().get(i));
                    regexFilters.get().remove(i);
                    continue;
                }

                if (p.matcher(message.getString()).find()) {
                    event.cancel();
                    return;
                }
            }
        }

        if (timestamps.get()) {
            Matcher matcher = Pattern.compile("^(<[0-9]{2}:[0-9]{2}>\\s)").matcher(message.getString());
            if (matcher.matches()) message.getSiblings().subList(0, 8).clear();

            Text timestamp = new LiteralText("<" + dateFormat.format(new Date()) + "> ").formatted(Formatting.GRAY);

            message = new LiteralText("").append(timestamp).append(message);
        }

        for (int i = 0; i < antiSpamDepth.get(); i++) {
            if (antiSpam.get()) {
                Text antiSpammed = appendAntiSpam(message, i);

                if (antiSpammed != null) {
                    message = antiSpammed;
                    ((ChatHudAccessor) mc.inGameHud.getChatHud()).getMessages().remove(i);
                    ((ChatHudAccessor) mc.inGameHud.getChatHud()).getVisibleMessages().remove(i);
                }
            }
        }

        event.setMessage(message);
    }

    @Override
    public void onActivate() {
        ChatUtils.registerCustomPrefix("cqb13.Numby.modules", this::getPrefix);
        ChatUtils.registerCustomPrefix("cqb13.Numby.modules.hud", this::getPrefix);
    }


    public LiteralText getPrefix() {
        BaseText logo = new LiteralText("");
        LiteralText prefix = new LiteralText("");
        String logoT = "Numby hack";
        if (customPrefix.get()) logoT = prefixText.get();
        if (customPrefixColor.get() && !chromaPrefix.get()) logo.append(new LiteralText(logoT).setStyle(logo.getStyle().withColor(TextColor.fromRgb(prefixColor.get().getPacked()))));
        if (chromaPrefix.get() && !customPrefixColor.get()) {
            prefixChroma.setSpeed(chromaSpeed.get() / 100);
            for(int i = 0, n = logoT.length() ; i < n ; i++) logo.append(new LiteralText(String.valueOf(logoT.charAt(i)))).setStyle(logo.getStyle().withColor(TextColor.fromRgb(prefixChroma.getNext().getPacked())));
        }
        if (!customPrefixColor.get() && !chromaPrefix.get()) {
            if (customPrefix.get()) { logo.append(prefixText.get());
            } else { logo.append("Numby hack"); }
            logo.setStyle(logo.getStyle().withFormatting(Formatting.RED));
        }
        if (themeBrackets.get()) {
            if (customPrefixColor.get() && !chromaPrefix.get()) prefix.setStyle(prefix.getStyle().withColor(TextColor.fromRgb(prefixColor.get().getPacked())));
            if (chromaPrefix.get() && !customPrefixColor.get()) {
                prefixChroma.setSpeed(chromaSpeed.get() / 100);
                prefix.setStyle(prefix.getStyle().withColor(TextColor.fromRgb(prefixChroma.getNext().getPacked())));
            }
            if (customBrackets.get()) {
                prefix.append(leftBracket.get());
                prefix.append(logo);
                prefix.append(rightBracket.get() + " ");
            } else {
                prefix.append("[");
                prefix.append(logo);
                prefix.append("] ");
            }
        } else {
            prefix.setStyle(prefix.getStyle().withFormatting(Formatting.GRAY));
            prefix.append("[");
            prefix.append(logo);
            prefix.append("] ");
        }
        return prefix;
    }

    private Text appendAntiSpam(Text text, int index) {
        List<ChatHudLine<OrderedText>> visibleMessages = ((ChatHudAccessor) mc.inGameHud.getChatHud()).getVisibleMessages();
        if (visibleMessages.isEmpty() || index < 0 || index > visibleMessages.size() - 1) return null;

        ChatHudLine<OrderedText> visibleMessage = visibleMessages.get(index);

        LiteralText parsed = new LiteralText("");

        visibleMessage.getText().accept((i, style, codePoint) -> {
            parsed.append(new LiteralText(new String(Character.toChars(codePoint))).setStyle(style));
            return true;
        });

        String oldMessage = parsed.getString();
        String newMessage = text.getString();

        if (oldMessage.equals(newMessage)) {
            return parsed.append(new LiteralText(" (2)").formatted(Formatting.GRAY));
        }
        else {
            Matcher matcher = Pattern.compile(".*(\\([0-9]+\\)$)").matcher(oldMessage);

            if (!matcher.matches()) return null;

            String group = matcher.group(matcher.groupCount());
            int number = Integer.parseInt(group.substring(1, group.length() - 1));

            String counter = " (" + number + ")";

            if (oldMessage.substring(0, oldMessage.length() - counter.length()).equals(newMessage)) {
                for (int i = 0; i < counter.length(); i++) parsed.getSiblings().remove(parsed.getSiblings().size() - 1);
                return parsed.append(new LiteralText( " (" + (number + 1) + ")").formatted(Formatting.GRAY));
            }
        }

        return null;
    }

    @EventHandler
    private void onMessageSend(SendMessageEvent event) {
        String message = event.message;

        if (emotes.get()) message = Emotes.apply(message);
        event.message = message;

        if (fancy.get()) message = applyFancy(message);

        message = message + getSuffix();

        if (coordsProtection.get() && containsCoordinates(message)) {
            BaseText warningMessage = new LiteralText("It looks like there are coordinates in your message! ");

            BaseText sendButton = getSendButton(message);
            warningMessage.append(sendButton);

            ChatUtils.sendMsg(warningMessage);

            event.cancel();
            return;
        }

        event.message = message;
    }

    // Annoy

    private String applyAnnoy(String message) {
        StringBuilder sb = new StringBuilder(message.length());
        boolean upperCase = true;
        for (int cp : message.codePoints().toArray()) {
            if (upperCase) sb.appendCodePoint(Character.toUpperCase(cp));
            else sb.appendCodePoint(Character.toLowerCase(cp));
            upperCase = !upperCase;
        }
        message = sb.toString();
        return message;
    }

    // Fancy

    private String applyFancy(String message) {
        StringBuilder sb = new StringBuilder();

        for (char ch : message.toCharArray()) {
            if (SMALL_CAPS.containsKey(ch)) sb.append(SMALL_CAPS.get(ch));
            else sb.append(ch);
        }

        return sb.toString();
    }

    // Prefix and Suffix

    private String getSuffix() {
        return suffix.get() ? getAffix(suffixText.get(), suffixSmallCaps.get(), suffixRandom.get()) : "";
    }

    private String getAffix(String text, boolean smallcaps, boolean random) {
        if (random) return String.format("(%03d) ", Utils.random(0, 1000));
        else if (smallcaps) return applyFancy(text);
        else return text;
    }

    // Coords Protection

    private boolean containsCoordinates(String message) {
        return message.matches(".*(?<x>-?\\d{3,}(?:\\.\\d*)?)(?:\\s+(?<y>\\d{1,3}(?:\\.\\d*)?))?\\s+(?<z>-?\\d{3,}(?:\\.\\d*)?).*");
    }

    private BaseText getSendButton(String message) {
        BaseText sendButton = new LiteralText("[SEND ANYWAY]");
        BaseText hintBaseText = new LiteralText("");

        BaseText hintMsg = new LiteralText("Send your message to the global chat even if there are coordinates:");
        hintMsg.setStyle(hintBaseText.getStyle().withFormatting(Formatting.GRAY));
        hintBaseText.append(hintMsg);

        hintBaseText.append(new LiteralText('\n' + message));

        sendButton.setStyle(sendButton.getStyle()
                .withFormatting(Formatting.DARK_RED)
                .withClickEvent(new ClickEvent(
                        ClickEvent.Action.RUN_COMMAND,
                        Commands.get().get(SayCommand.class).toString(message)
                ))
                .withHoverEvent(new HoverEvent(
                        HoverEvent.Action.SHOW_TEXT,
                        hintBaseText
                )));
        return sendButton;
    }

    // Longer chat

    public boolean isInfiniteChatBox() {
        return isActive() && infiniteChatBox.get();
    }

    public boolean isLongerChat() {
        return isActive() && longerChatHistory.get();
    }

    public int getChatLength() {
        return longerChatLines.get();
    }
}
