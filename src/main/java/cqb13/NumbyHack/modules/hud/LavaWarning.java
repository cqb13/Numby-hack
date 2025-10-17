package cqb13.NumbyHack.modules.hud;

import cqb13.NumbyHack.NumbyHack;
import cqb13.NumbyHack.utils.CHMainUtils;
import meteordevelopment.meteorclient.settings.*;
import meteordevelopment.meteorclient.systems.hud.HudElement;
import meteordevelopment.meteorclient.systems.hud.HudElementInfo;
import meteordevelopment.meteorclient.systems.hud.HudRenderer;
import meteordevelopment.meteorclient.utils.render.color.SettingColor;

public class LavaWarning extends HudElement {
    public static final HudElementInfo<LavaWarning> INFO = new HudElementInfo<>(NumbyHack.HUD_GROUP, "lava-warning",
            "Warns you when there is lava under you.", LavaWarning::new);

    private final SettingGroup sgGeneral = settings.getDefaultGroup();
    private final SettingGroup sgScale = settings.createGroup("Scale");
    private final SettingGroup sgBackground = settings.createGroup("Background");

    // General

    private final Setting<Integer> checkDistance = sgGeneral.add(new IntSetting.Builder()
            .name("check-distance")
            .description("The distance under you to check for lava.")
            .defaultValue(3)
            .min(3)
            .max(10)
            .sliderMin(3)
            .sliderMax(10)
            .build());

    private final Setting<SettingColor> textColor = sgGeneral.add(new ColorSetting.Builder()
            .name("text-color")
            .description("The color of the warning message.")
            .defaultValue(new SettingColor(204, 0, 0))
            .build());

    private final Setting<Boolean> shadow = sgGeneral.add(new BoolSetting.Builder()
            .name("shadow")
            .description("Text shadow.")
            .defaultValue(true)
            .build());

    private final Setting<Integer> border = sgGeneral.add(new IntSetting.Builder()
            .name("border")
            .description("How much space to add around the element.")
            .defaultValue(0)
            .build());

    // Scale

    private final Setting<Boolean> customScale = sgScale.add(new BoolSetting.Builder()
            .name("custom-scale")
            .description("Applies custom text scale rather than the global one.")
            .defaultValue(false)
            .build());

    private final Setting<Double> scale = sgScale.add(new DoubleSetting.Builder()
            .name("scale")
            .description("Custom scale.")
            .visible(customScale::get)
            .defaultValue(1)
            .min(0.5)
            .sliderRange(0.5, 3)
            .build());

    // Background

    private final Setting<Boolean> background = sgBackground.add(new BoolSetting.Builder()
            .name("background")
            .description("Displays background.")
            .defaultValue(false)
            .build());

    private final Setting<SettingColor> backgroundColor = sgBackground.add(new ColorSetting.Builder()
            .name("background-color")
            .description("Color used for the background.")
            .visible(background::get)
            .defaultValue(new SettingColor(25, 25, 25, 50))
            .build());

    public LavaWarning() {
        super(INFO);
    }

    @Override
    public void setSize(double width, double height) {
        super.setSize(width + border.get() * 2, height + border.get() * 2);
    }

    @Override
    public void render(HudRenderer renderer) {
        if (background.get()) {
            renderer.quad(this.x, this.y, getWidth(), getHeight(), backgroundColor.get());
        }

        if (isInEditor()) {
            render(renderer, "0");
            return;
        }

        int lava_distance = CHMainUtils.lavaIsWithinRange(checkDistance.get());

        if (lava_distance != -1) {
            render(renderer, Integer.toString(lava_distance));
        }
    }

    private void render(HudRenderer renderer, String right) {
        double x = this.x + border.get();
        double y = this.y + border.get();

        double x2 = renderer.text("Lava ", x, y, textColor.get(), shadow.get(), getScale());
        x2 = renderer.text(right, x2, y, textColor.get(), shadow.get(), getScale());
        x2 = renderer.text(" Blocks bellow you", x2, y, textColor.get(), shadow.get(), getScale());

        setSize(x2 - x, renderer.textHeight(shadow.get(), getScale()));
    }

    private double getScale() {
        return customScale.get() ? scale.get() : -1;
    }
}
