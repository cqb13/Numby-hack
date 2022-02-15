package cqb13.Numby.modules.hud;

import meteordevelopment.meteorclient.settings.ColorSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.systems.hud.HUD;
import meteordevelopment.meteorclient.systems.hud.modules.DoubleTextHudElement;
import meteordevelopment.meteorclient.utils.render.color.SettingColor;

public class Numby extends DoubleTextHudElement {
    private final SettingGroup sgGeneral = settings.getDefaultGroup();

    private final Setting<SettingColor> color = sgGeneral.add(new ColorSetting.Builder().name("color").description("Color text.").defaultValue(new SettingColor(146, 188, 98)).build());

    public Numby(HUD hud) {
        super(hud, "Numby", "Number81 On Top!", "");
        rightColor = color.get();
    }

    @Override
    protected String getRight() {
        return "Number81 On Top";
    }
}
