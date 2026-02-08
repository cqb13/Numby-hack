package cqb13.NumbyHack.modules.general;

import org.joml.Vector3d;

import cqb13.NumbyHack.NumbyHack;
import meteordevelopment.meteorclient.events.render.Render2DEvent;
import meteordevelopment.meteorclient.events.render.Render3DEvent;
import meteordevelopment.meteorclient.renderer.ShapeMode;
import meteordevelopment.meteorclient.renderer.text.TextRenderer;
import meteordevelopment.meteorclient.settings.BoolSetting;
import meteordevelopment.meteorclient.settings.ColorSetting;
import meteordevelopment.meteorclient.settings.DoubleSetting;
import meteordevelopment.meteorclient.settings.EnumSetting;
import meteordevelopment.meteorclient.settings.IntSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.utils.player.PlayerUtils;
import meteordevelopment.meteorclient.utils.render.NametagUtils;
import meteordevelopment.meteorclient.utils.render.color.Color;
import meteordevelopment.meteorclient.utils.render.color.SettingColor;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.entity.Entity;
import net.minecraft.entity.TntEntity;

/**
 * made by cqb13
 */
public class TntFuseEsp extends Module {
    private final SettingGroup sgGeneral = settings.getDefaultGroup();
    private final SettingGroup sgTextRender = settings.createGroup("Text Rendering");
    private final SettingGroup sgLitTntRender = settings.createGroup("Lit TNT Rendering");

    public final Setting<Boolean> showTntFuseText = sgGeneral.add(new BoolSetting.Builder()
            .name("tnt-fuse-text")
            .description("Show text with the amount of seconds remaining in a tnt's fuse.")
            .defaultValue(true)
            .build());

    public final Setting<Boolean> showTntFuse = sgGeneral.add(new BoolSetting.Builder()
            .name("tnt-fuse-indicator")
            .description("Draw a box around lit tnt with the color indicating fuse time.")
            .defaultValue(false)
            .build());

    public final Setting<Boolean> hideTntFlashing = sgGeneral.add(new BoolSetting.Builder()
            .name("hide-tnt-flashing")
            .description("Hides the flashing of lit tnt.")
            .defaultValue(false)
            .build());

    public final Setting<Boolean> computeColorFromFuse = sgTextRender.add(new BoolSetting.Builder()
            .name("compute-color-from-fuse")
            .description("Determines the color for the fuse indicator based on fuse time.")
            .defaultValue(false)
            .visible(showTntFuseText::get)
            .build());

    private final Setting<SettingColor> textColor = sgTextRender.add(new ColorSetting.Builder()
            .name("text-color")
            .description("The color of the text.")
            .defaultValue(new SettingColor(255, 255, 255))
            .visible(() -> !computeColorFromFuse.get() && showTntFuseText.get())
            .build());

    private final Setting<Double> textScale = sgTextRender.add(new DoubleSetting.Builder()
            .name("text-scale")
            .description("How big the text should be.")
            .defaultValue(1.25)
            .min(1)
            .sliderMax(4)
            .visible(showTntFuseText::get)
            .build());

    private final Setting<Boolean> hideWhenNear = sgTextRender.add(new BoolSetting.Builder()
            .name("hide-when-near")
            .description("Hide the text when you near the tnt.")
            .defaultValue(true)
            .visible(showTntFuseText::get)
            .build());

    private final Setting<Integer> nearDistance = sgTextRender.add(new IntSetting.Builder()
            .name("distance")
            .description("At what distance to hide the text.")
            .defaultValue(10)
            .min(5)
            .sliderMax(100)
            .visible(() -> hideWhenNear.get() && showTntFuseText.get())
            .build());

    private final Setting<Boolean> hideWhenFar = sgTextRender.add(new BoolSetting.Builder()
            .name("hide-when-far")
            .description("Hide the text when you far from the tnt.")
            .defaultValue(false)
            .visible(showTntFuseText::get)
            .build());

    private final Setting<Integer> farDistance = sgTextRender.add(new IntSetting.Builder()
            .name("distance")
            .description("At what distance to hide the text.")
            .defaultValue(1000)
            .min(5)
            .sliderMax(10000)
            .visible(() -> hideWhenFar.get() && showTntFuseText.get())
            .build());

    private final Setting<ShapeMode> shapeMode = sgLitTntRender.add(new EnumSetting.Builder<ShapeMode>()
            .name("shape-mode")
            .description("How the shapes are rendered.")
            .defaultValue(ShapeMode.Both)
            .visible(showTntFuse::get)
            .build());

    private final Setting<Integer> lineOpacity = sgLitTntRender.add(new IntSetting.Builder()
            .name("line-opacity")
            .description("The opacity of lines.")
            .defaultValue(255)
            .min(0)
            .max(255)
            .sliderMax(255)
            .visible(() -> showTntFuse.get() && shapeMode.get() == ShapeMode.Both || shapeMode.get() == ShapeMode.Lines)
            .build());

    private final Setting<Integer> sideOpacity = sgLitTntRender.add(new IntSetting.Builder()
            .name("side-opacity")
            .description("The opacity of sides.")
            .defaultValue(75)
            .min(0)
            .max(255)
            .sliderMax(255)
            .visible(() -> showTntFuse.get() && shapeMode.get() == ShapeMode.Both || shapeMode.get() == ShapeMode.Sides)
            .build());

    public TntFuseEsp() {
        super(NumbyHack.CATEGORY, "tnt-fuse-esp", "Shows you the fuse time of lit tnt.");
    }

    public boolean shouldHideFlashing() {
        return hideTntFlashing.get();
    }

    @EventHandler
    private void onRender3D(Render3DEvent event) {
        if (mc.world == null || mc.player == null || !showTntFuse.get()) {
            return;
        }

        for (Entity entity : mc.world.getEntities()) {
            if (!(entity instanceof TntEntity tntEntity)) {
                continue;
            }

            Color color = fuseColor(tntEntity.getFuse());

            Color sideColor = new Color(color.r, color.g, color.b, sideOpacity.get());
            Color lineColor = new Color(color.r, color.g, color.b, lineOpacity.get());

            event.renderer.box(entity.getX() - .5, entity.getY(), entity.getZ() - .5, entity.getX() + 0.5,
                    entity.getY() + 1,
                    entity.getZ() + 0.5, sideColor, lineColor, shapeMode.get(), 0);
        }
    }

    @EventHandler
    private void onRender2D(Render2DEvent event) {
        if (mc.world == null || mc.player == null || !showTntFuseText.get()) {
            return;
        }

        for (Entity entity : mc.world.getEntities()) {
            if (!(entity instanceof TntEntity tntEntity)) {
                continue;
            }

            if (PlayerUtils.isWithin(entity.getEntityPos(), (double) nearDistance.get()) && hideWhenNear.get()) {
                continue;
            }

            if (!PlayerUtils.isWithin(entity.getEntityPos(), (double) farDistance.get()) && hideWhenFar.get()) {
                continue;
            }

            Vector3d vec3 = new Vector3d(entity.getX(), entity.getY() + 0.5, entity.getZ());

            if (NametagUtils.to2D(vec3, textScale.get())) {
                NametagUtils.begin(vec3);
                TextRenderer.get().begin(1, false, true);

                String text = String.format("%.2f", (double) tntEntity.getFuse() / 20);

                double textWidth = TextRenderer.get().getWidth(text);
                double textHeight = TextRenderer.get().getHeight();

                Color color;

                if (computeColorFromFuse.get()) {
                    color = fuseColor(tntEntity.getFuse());
                } else {
                    color = textColor.get();
                }

                TextRenderer.get().render(
                        text,
                        -textWidth / 2,
                        -textHeight / 2,
                        color,
                        true);

                TextRenderer.get().end();
                NametagUtils.end();
            }
        }
    }

    private static Color fuseColor(int currentFuseTicks) {
        final int initialFuse = 80;

        double percent = (double) currentFuseTicks / initialFuse;
        percent = Math.max(0.0, Math.min(1.0, percent));

        int r = (int) (255 * (1.0 - percent));
        int g = (int) (255 * percent);

        return new Color(r, g, 0);
    }
}
