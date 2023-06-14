package cqb13.NumbyHack.modules.general;

import cqb13.NumbyHack.NumbyHack;
import meteordevelopment.meteorclient.events.render.Render3DEvent;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.settings.*;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.utils.Utils;
import meteordevelopment.meteorclient.utils.render.color.Color;
import meteordevelopment.meteorclient.utils.render.color.SettingColor;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.util.math.Vec3d;

public class JumpHelper extends Module {

    private final SettingGroup sgGeneral = settings.getDefaultGroup();

    private final Setting<Boolean> useRainbow = sgGeneral.add(new BoolSetting.Builder()
            .name("use-rainbow")
            .defaultValue(false)
            .build());

    private final Setting<SettingColor> circleColor = sgGeneral.add(new ColorSetting.Builder()
            .name("circle-color")
            .description("Color for circle rendering")
            .defaultValue(new SettingColor(146,188,98, 255))
            .visible(() -> !useRainbow.get())
            .build());

    private final Setting<Double> circleRadius = sgGeneral.add(new DoubleSetting.Builder()
            .name("circle-radius")
            .description("How big the circle should be draw.")
            .defaultValue(3.5)
            .min(0)
            .build());

    public JumpHelper() {
        super(NumbyHack.CATEGORY, "jump-helper", "Draws a circle around you when you jump to help with movement.");
    }

    private double circleSection = 0.0;
    Vec3d renderPos = new Vec3d(0, 0 ,0);

    @EventHandler
    private void onTick(TickEvent.Post event) {
        if (mc.player == null) return;
        if (mc.player.isOnGround()) {
            renderPos = mc.player.getPos();
        }
    }

    @EventHandler
    private void onRender(Render3DEvent event) {
        if (!Utils.canUpdate()) return;

        if (mc.player == null) return;
        if (mc.player.isOnGround()) return;

        boolean rainbow = useRainbow.get();
        double radius = circleRadius.get();

        Vec3d last = null;
        circleSection += rainbow ? 1.0 : 0;
        if (circleSection > 360) circleSection = 0;
        for (int i = 0; i <= 360; i += rainbow ? 1 : 7) {
            Color drawCircleColor;
            if (!rainbow) drawCircleColor = circleColor.get();
            else {
                double rot = (255.0 * 3) * (((((double) i) + circleSection) % 360) / 360.0);
                int seed = (int) Math.floor(rot / 255.0);
                double current = rot % 255;
                double red = seed == 0 ? current : (seed == 1 ? Math.abs(current - 255) : 0);
                double green = seed == 1 ? current : (seed == 2 ? Math.abs(current - 255) : 0);
                double blue = seed == 2 ? current : (seed == 0 ? Math.abs(current - 255) : 0);
                drawCircleColor = new Color((int) red, (int) green, (int) blue);
            }
            Vec3d drawVec = renderPos;
            double rad = Math.toRadians(i);
            double sin = Math.sin(rad) * radius;
            double cos = Math.cos(rad) * radius;
            Vec3d c = new Vec3d(drawVec.x + sin, drawVec.y, drawVec.z + cos);
            if (last != null) event.renderer.line(last.x, last.y, last.z, c.x, c.y, c.z, drawCircleColor);
            last = c;
        }
    }
}