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

  private final Setting<Boolean> alwaysShow = sgGeneral.add(new BoolSetting.Builder()
      .name("always-show")
      .description("Always show the shape.")
      .defaultValue(false)
      .build());

  private final Setting<Boolean> useRainbow = sgGeneral.add(new BoolSetting.Builder()
      .name("use-rainbow")
      .defaultValue(false)
      .build());

  private final Setting<SettingColor> shapeColor = sgGeneral.add(new ColorSetting.Builder()
      .name("shape-color")
      .description("Color for shape rendering")
      .defaultValue(new SettingColor(146, 188, 98, 255))
      .visible(() -> !useRainbow.get())
      .build());

  private final Setting<Double> shapeRadius = sgGeneral.add(new DoubleSetting.Builder()
      .name("shape-radius")
      .description("How big the shape should be drawn.")
      .defaultValue(3.5)
      .min(0)
      .build());

  private final Setting<Boolean> makeItASphere = sgGeneral.add(new BoolSetting.Builder()
      .name("make-it-a-sphere")
      .description("Make the shape a sphere.")
      .defaultValue(false)
      .build());

  public Setting<Integer> gradation = sgGeneral.add(new IntSetting.Builder()
      .name("gradation")
      .defaultValue(30)
      .range(20, 100)
      .sliderRange(20, 100)
      .visible(makeItASphere::get)
      .build());

  public JumpHelper() {
    super(NumbyHack.CATEGORY, "jump-helper", "Draws a shape around you when you jump to help with movement.");
  }

  private double shapeResolution = 30;
  Vec3d renderPos = new Vec3d(0, 0, 0);

  @EventHandler
  private void onTick(TickEvent.Post event) {
    if (mc.player == null)
      return;
    if (mc.player.isOnGround()) {
      renderPos = mc.player.getPos();
    }
  }

  @EventHandler
  private void onRender(Render3DEvent event) {
    if (!Utils.canUpdate())
      return;

    if (mc.player == null)
      return;
    if (mc.player.isOnGround() && !alwaysShow.get())
      return;

    boolean rainbow = useRainbow.get();
    double radius = shapeRadius.get();

    if (makeItASphere.get()) {
      renderSphere(event, radius, gradation.get(), renderPos);
    } else {
      renderCircle(event, rainbow, radius, renderPos);
    }
  }

  private void renderCircle(Render3DEvent event, boolean rainbow, double radius, Vec3d origin) {
    Vec3d last = null;
    double shapeSection = 0.0;
    shapeSection += rainbow ? 1.0 : 0;
    if (shapeSection > 360)
      shapeSection = 0;

    for (int i = 0; i <= 360; i += rainbow ? 1 : 7) {
      Color drawShapeColor;
      if (!rainbow)
        drawShapeColor = shapeColor.get();
      else {
        double rot = (255.0 * 3) * (((((double) i) + shapeSection) % 360) / 360.0);
        int seed = (int) Math.floor(rot / 255.0);
        double current = rot % 255;
        double red = seed == 0 ? current : (seed == 1 ? Math.abs(current - 255) : 0);
        double green = seed == 1 ? current : (seed == 2 ? Math.abs(current - 255) : 0);
        double blue = seed == 2 ? current : (seed == 0 ? Math.abs(current - 255) : 0);
        drawShapeColor = new Color((int) red, (int) green, (int) blue);
      }
      double rad = Math.toRadians(i);
      double sin = Math.sin(rad) * radius;
      double cos = Math.cos(rad) * radius;
      Vec3d c = new Vec3d(origin.x + sin, origin.y, origin.z + cos);
      if (last != null)
        event.renderer.line(last.x, last.y, last.z, c.x, c.y, c.z, drawShapeColor);
      last = c;
    }
  }

  private void renderSphere(Render3DEvent event, double radius, int gradation, Vec3d pos) {
    float alpha, beta;

    for (alpha = 0.0f; alpha < Math.PI; alpha += Math.PI / gradation) {
      for (beta = 0.0f; beta < 2.0 * Math.PI; beta += Math.PI / gradation) {
        double x1 = (float) (pos.getX() + (radius * Math.cos(beta) * Math.sin(alpha)));
        double y1 = (float) (pos.getY() + (radius * Math.sin(beta) * Math.sin(alpha)));
        double z1 = (float) (pos.getZ() + (radius * Math.cos(alpha)));

        double sin = Math.sin(alpha + Math.PI / gradation);
        double x2 = (float) (pos.getX() + (radius * Math.cos(beta) * sin));
        double y2 = (float) (pos.getY() + (radius * Math.sin(beta) * sin));
        double z2 = (float) (pos.getZ() + (radius * Math.cos(alpha + Math.PI / gradation)));

        event.renderer.line(x1, y1, z1, x2, y2, z2, shapeColor.get());
      }
    }
  }
}
