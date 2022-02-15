package cqb13.Numby.modules;

import cqb13.Numby.Numby;
import meteordevelopment.meteorclient.settings.ColorSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.settings.StringSetting;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.utils.player.ChatUtils;
import meteordevelopment.meteorclient.utils.render.color.SettingColor;
import net.minecraft.text.BaseText;
import net.minecraft.text.LiteralText;
import net.minecraft.text.TextColor;

public class NumbyPrefix extends Module {
    private final SettingGroup sgGeneral = settings.getDefaultGroup();

    private final Setting<String> prefix = sgGeneral.add(new StringSetting.Builder().name("prefix").description("What to be displayed as Numby hack Prefix").defaultValue("Numby hack").build());
    private final Setting<SettingColor> prefixColors = sgGeneral.add(new ColorSetting.Builder().name("prefix-color").description("Color display for the prefix").defaultValue(new SettingColor(146, 188, 98,255)).build());
    private final Setting<String> leftBracket = sgGeneral.add(new StringSetting.Builder().name("left-bracket").description("What to be displayed as left bracket for the prefix").defaultValue("[").build());
    private final Setting<String> rightBracket = sgGeneral.add(new StringSetting.Builder().name("right-bracket").description("What to be displayed as right bracket for the prefix").defaultValue("]").build());
    private final Setting<SettingColor> bracketColors = sgGeneral.add(new ColorSetting.Builder().name("bracket-color").description("Color display for the brackets").defaultValue(new SettingColor(146, 188, 98,255)).build());

    public NumbyPrefix() {
        super(Numby.CATEGORY, "numby hack prefix", "Prefix for Numby hack modules");
    }

    @Override
    public void onActivate() {
        ChatUtils.registerCustomPrefix("cqb13.Numby.modules", this::getPrefix);
    }

    public LiteralText getPrefix() {
        BaseText logo = new LiteralText(prefix.get());
        BaseText left = new LiteralText(leftBracket.get());
        BaseText right = new LiteralText(rightBracket.get());
        LiteralText prefix = new LiteralText("");
        logo.setStyle(logo.getStyle().withColor(TextColor.fromRgb(prefixColors.get().getPacked())));
        left.setStyle(left.getStyle().withColor(TextColor.fromRgb(bracketColors.get().getPacked())));
        right.setStyle(right.getStyle().withColor(TextColor.fromRgb(bracketColors.get().getPacked())));
        prefix.append(left);
        prefix.append(logo);
        prefix.append(right);
        prefix.append(" ");
        return prefix;
    }
}
