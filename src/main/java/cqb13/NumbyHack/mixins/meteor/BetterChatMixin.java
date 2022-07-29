package cqb13.NumbyHack.mixins.meteor;

import cqb13.NumbyHack.utils.CHMainUtils;
import meteordevelopment.meteorclient.events.game.SendMessageEvent;
import meteordevelopment.meteorclient.settings.*;
import meteordevelopment.meteorclient.systems.modules.Category;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.systems.modules.misc.BetterChat;
import meteordevelopment.meteorclient.utils.player.ChatUtils;
import meteordevelopment.meteorclient.utils.render.color.RainbowColor;
import meteordevelopment.meteorclient.utils.render.color.SettingColor;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.text.TextColor;
import net.minecraft.util.Formatting;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(BetterChat.class)
public abstract class BetterChatMixin extends Module {
    private SettingGroup numby$sgCustom;
    private Setting<Boolean> numby$emotes;
    private Setting<Boolean> numby$customPrefix;
    private Setting<String> numby$prefixText;
    private Setting<Boolean> numby$customPrefixColor;
    private Setting<SettingColor> numby$prefixColor;
    private Setting<Boolean> numby$chromaPrefix;
    private Setting<Double> numby$chromaSpeed;
    private Setting<Boolean> numby$themeBrackets;
    private Setting<Boolean> numby$customBrackets;
    private Setting<String> numby$leftBracket;
    private Setting<String> numby$rightBracket;

    public BetterChatMixin(Category category, String name, String description) {
        super(category, name, description);
    }

    @Inject(method = "<init>", at = @At("TAIL"))
    private void constructor(CallbackInfo ci) {
        numby$sgCustom = this.settings.createGroup("Customize");
        numby$emotes = numby$sgCustom.add(new BoolSetting.Builder()
                .name("emotes")
                .description("Enables the Ghostware emote system.")
                .defaultValue(false)
                .build()
        );
        numby$customPrefix = numby$sgCustom.add(new BoolSetting.Builder()
                .name("custom-prefix")
                .description("Lets you set a custom prefix.")
                .defaultValue(false)
                .build()
        );
        numby$prefixText = numby$sgCustom.add(new StringSetting.Builder()
                .name("custom-prefix-text")
                .description("Override the [Numby hack] prefix.")
                .defaultValue("Numby hack")
                .visible(numby$customPrefix::get)
                .build()
        );
        numby$customPrefixColor = numby$sgCustom.add(new BoolSetting.Builder()
                .name("custom-prefix-color")
                .description("Lets you set a custom prefix.")
                .defaultValue(false)
                .build()
        );
        numby$prefixColor = numby$sgCustom.add(new ColorSetting.Builder()
                .name("prefix-color")
                .description("Color of the prefix text.")
                .defaultValue(new SettingColor(146,188,98))
                .visible(numby$customPrefixColor::get)
                .build()
        );
        numby$chromaPrefix = numby$sgCustom.add(new BoolSetting.Builder()
                .name("chroma-prefix")
                .description("Lets you set a custom prefix.")
                .defaultValue(false)
                .build()
        );
        numby$chromaSpeed = numby$sgCustom.add(new DoubleSetting.Builder()
                .name("chroma-speed")
                .description("Speed of the chroma animation.")
                .defaultValue(0.09)
                .min(0.01)
                .sliderMax(5)
                .decimalPlaces(2)
                .visible(numby$chromaPrefix::get)
                .build()
        );
        numby$themeBrackets = numby$sgCustom.add(new BoolSetting.Builder()
                .name("apply-to-brackets")
                .description("Apply the current prefix theme to the brackets.")
                .defaultValue(false)
                .build()
        );
        numby$customBrackets = numby$sgCustom.add(new BoolSetting.Builder()
                .name("custom-brackets")
                .description("Set custom brackets.")
                .defaultValue(false)
                .build()
        );
        numby$leftBracket = numby$sgCustom.add(new StringSetting.Builder()
                .name("left-bracket")
                .description("")
                .defaultValue("[")
                .visible(numby$customBrackets::get)
                .build()
        );
        numby$rightBracket = numby$sgCustom.add(new StringSetting.Builder()
                .name("right-bracket")
                .description("")
                .defaultValue("]")
                .visible(numby$customBrackets::get)
                .build()
        );
    }

    @Override
    public void onActivate() {
        ChatUtils.registerCustomPrefix("cqb13.NumbyHack.modules", this::numby$getPrefix);
    }

    RainbowColor numby$prefixChroma = new RainbowColor();

    private MutableText numby$getPrefix() {
        MutableText logo = Text.literal("");
        MutableText prefix = Text.literal("");
        String logoT = "Numby hack";
        if (numby$customPrefix.get()) logoT = numby$prefixText.get();
        if (numby$customPrefixColor.get() && !numby$chromaPrefix.get()) logo.append(Text.literal(logoT).setStyle(logo.getStyle().withColor(TextColor.fromRgb(numby$prefixColor.get().getPacked()))));
        if (numby$chromaPrefix.get() && !numby$customPrefixColor.get()) {
            numby$prefixChroma.setSpeed(numby$chromaSpeed.get() / 100);
            for(int i = 0, n = logoT.length() ; i < n ; i++) logo.append(Text.literal(String.valueOf(logoT.charAt(i)))).setStyle(logo.getStyle().withColor(TextColor.fromRgb(numby$prefixChroma.getNext().getPacked())));
        }
        if (!numby$customPrefixColor.get() && !numby$chromaPrefix.get()) {
            if (numby$customPrefix.get()) { logo.append(numby$prefixText.get());
            } else { logo.append("Numby hack"); }
            logo.setStyle(logo.getStyle().withFormatting(Formatting.RED));
        }
        if (numby$themeBrackets.get()) {
            if (numby$customPrefixColor.get() && !numby$chromaPrefix.get()) prefix.setStyle(prefix.getStyle().withColor(TextColor.fromRgb(numby$prefixColor.get().getPacked())));
            if (numby$chromaPrefix.get() && !numby$customPrefixColor.get()) {
                numby$prefixChroma.setSpeed(numby$chromaSpeed.get() / 100);
                prefix.setStyle(prefix.getStyle().withColor(TextColor.fromRgb(numby$prefixChroma.getNext().getPacked())));
            }
            if (numby$customBrackets.get()) {
                prefix.append(numby$leftBracket.get());
                prefix.append(logo);
                prefix.append(numby$rightBracket.get() + " ");
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

    @Inject(method = "onMessageSend", at = @At("TAIL"))
    private void applyEmotes(SendMessageEvent event, CallbackInfo ci) {
        if (!numby$emotes.get()) return;
        event.message = CHMainUtils.apply(event.message);
    }
}
