package cqb13.NumbyHack.modules.general;

import org.joml.Vector3d;

import cqb13.NumbyHack.NumbyHack;
import meteordevelopment.meteorclient.events.render.Render2DEvent;
import meteordevelopment.meteorclient.renderer.text.TextRenderer;
import meteordevelopment.meteorclient.settings.BoolSetting;
import meteordevelopment.meteorclient.settings.ColorSetting;
import meteordevelopment.meteorclient.settings.DoubleSetting;
import meteordevelopment.meteorclient.settings.IntSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.utils.player.PlayerUtils;
import meteordevelopment.meteorclient.utils.render.NametagUtils;
import meteordevelopment.meteorclient.utils.render.color.SettingColor;
import meteordevelopment.meteorclient.utils.world.BlockEntityIterator;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.SignBlockEntity;
import net.minecraft.block.entity.SignText;
import net.minecraft.text.Text;

/**
 * made by cqb13
 */
public class SignTextEsp extends Module {
    private final SettingGroup sgGeneral = settings.getDefaultGroup();

    private final Setting<SettingColor> textColor = sgGeneral.add(new ColorSetting.Builder()
            .name("text-color")
            .description("The color of the text.")
            .defaultValue(new SettingColor(255, 255, 255))
            .build());

    private final Setting<Double> textScale = sgGeneral.add(new DoubleSetting.Builder()
            .name("text-scale")
            .description("How big the text should be.")
            .defaultValue(1.25)
            .min(1)
            .sliderMax(4)
            .build());

    private final Setting<Boolean> hideWhenNear = sgGeneral.add(new BoolSetting.Builder()
            .name("hide-when-near")
            .description("Hide the text when you near the sign.")
            .defaultValue(true)
            .build());

    private final Setting<Integer> nearDistance = sgGeneral.add(new IntSetting.Builder()
            .name("distance")
            .description("At what distance to hide the text.")
            .defaultValue(20)
            .min(5)
            .sliderMax(100)
            .visible(hideWhenNear::get)
            .build());

    private final Setting<Boolean> hideWhenFar = sgGeneral.add(new BoolSetting.Builder()
            .name("hide-when-far")
            .description("Hide the text when you far from the sign.")
            .defaultValue(false)
            .build());

    private final Setting<Integer> farDistance = sgGeneral.add(new IntSetting.Builder()
            .name("distance")
            .description("At what distance to hide the text.")
            .defaultValue(1000)
            .min(5)
            .sliderMax(10000)
            .visible(hideWhenFar::get)
            .build());

    public SignTextEsp() {
        super(NumbyHack.CATEGORY, "sign-text-esp", "Shows you the text on signs from far away.");
    }

    @EventHandler
    private void onRender2D(Render2DEvent event) {
        BlockEntityIterator it = new BlockEntityIterator();

        while (it.hasNext()) {
            BlockEntity entity = it.next();

            if (PlayerUtils.isWithin(entity.getPos(), (double) nearDistance.get()) && hideWhenNear.get()) {
                continue;
            }

            if (!PlayerUtils.isWithin(entity.getPos(), (double) farDistance.get()) && hideWhenFar.get()) {
                continue;
            }

            if (!(entity instanceof SignBlockEntity signEntity)) {
                continue;
            }

            SignText text = signEntity.getText(true);
            Text[] msgs = text.getMessages(true);

            renderNametagLines(
                    new Vector3d(entity.getPos().getX() + 0.5, entity.getPos().getY() + 0.5,
                            entity.getPos().getZ() + 0.5),
                    textScale.get(),
                    TextRenderer.get(),
                    textColor.get(),
                    true,
                    msgs[0].getString(),
                    msgs[1].getString(),
                    msgs[2].getString(),
                    msgs[3].getString());

        }
    }

    public void renderNametagLines(
            Vector3d pos,
            double scale,
            TextRenderer renderer,
            SettingColor color,
            boolean shadow,
            String line1,
            String line2,
            String line3,
            String line4) {
        if (!NametagUtils.to2D(pos, scale)) {
            return;
        }

        NametagUtils.begin(pos);
        renderer.begin(1, false, true);

        String[] lines = { line1, line2, line3, line4 };

        double lineHeight = renderer.getHeight();
        double totalHeight = lineHeight * lines.length;

        double y = -totalHeight / 2.0;

        for (String line : lines) {
            if (line == null || line.isEmpty()) {
                y += lineHeight;
                continue;
            }

            double width = renderer.getWidth(line);
            renderer.render(line, -width / 2.0, y, color, shadow);
            y += lineHeight;
        }

        renderer.end();
        NametagUtils.end();
    }
}
