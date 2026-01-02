package cqb13.NumbyHack.modules.general;

import cqb13.NumbyHack.NumbyHack;
import meteordevelopment.meteorclient.events.render.Render3DEvent;
import meteordevelopment.meteorclient.renderer.ShapeMode;
import meteordevelopment.meteorclient.settings.ColorSetting;
import meteordevelopment.meteorclient.settings.EnumSetting;
import meteordevelopment.meteorclient.settings.IntSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.utils.render.color.SettingColor;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.util.math.ChunkPos;

public class ChunkBorders extends Module {
    private final SettingGroup sgGeneral = settings.getDefaultGroup();
    private final SettingGroup sgRender = settings.createGroup("Render");

    private final Setting<Integer> range = sgGeneral.add(new IntSetting.Builder()
            .name("chunk-radius")
            .description("How many chunks around the current chunk to render borders.")
            .defaultValue(1)
            .min(0)
            .sliderRange(0, 32)
            .build());

    // Render
    private final Setting<ShapeMode> shapeMode = sgRender.add(new EnumSetting.Builder<ShapeMode>()
            .name("shape-mode")
            .description("How the shapes are rendered.")
            .defaultValue(ShapeMode.Both)
            .build());

    private final Setting<SettingColor> sideColor = sgRender.add(new ColorSetting.Builder()
            .name("side-color")
            .description("The color of the sides of the blocks being rendered.")
            .defaultValue(new SettingColor(146, 188, 98, 75))
            .build());

    private final Setting<SettingColor> lineColor = sgRender.add(new ColorSetting.Builder()
            .name("line-color")
            .description("The color of the lines of the blocks being rendered.")
            .defaultValue(new SettingColor(146, 188, 98, 255))
            .build());

    private final Setting<RenderPosition> renderPosition = sgRender.add(new EnumSetting.Builder<RenderPosition>()
            .name("render-position")
            .description("The Y level at which chunk borders begin to render")
            .defaultValue(RenderPosition.PlayerHeight)
            .build());

    private final Setting<Integer> playerHeightOffset = sgRender.add(new IntSetting.Builder()
            .name("player-height-offset")
            .description("The vertical offset from player height to start rendering")
            .defaultValue(0)
            .sliderRange(-384, 384)
            .visible(() -> renderPosition.get() == RenderPosition.PlayerHeightOffset)
            .build());

    private final Setting<Integer> customY = sgRender.add(new IntSetting.Builder()
            .name("custom-y")
            .description("The Y level aat which chunk borders begin to render")
            .defaultValue(64)
            .min(0)
            .sliderRange(0, 384)
            .visible(() -> renderPosition.get() == RenderPosition.Custom)
            .build());

    private final Setting<Integer> height = sgRender.add(new IntSetting.Builder()
            .name("chunk-height")
            .description("Height of the chunk borders")
            .defaultValue(0)
            .min(0)
            .sliderRange(-384, 384)
            .build());

    public ChunkBorders() {
        super(NumbyHack.CATEGORY, "chunk-borders", "Customizable chunk border rendering.");
    }

    @EventHandler
    private void onRender(Render3DEvent event) {
        if (mc.world == null || mc.player == null)
            return;

        ChunkPos center = new ChunkPos(mc.player.getBlockPos());
        int r = range.get();

        double y;
        switch (renderPosition.get()) {
            case RenderPosition.PlayerHeight:
                y = mc.player.getY();
                break;
            case RenderPosition.PlayerHeightOffset:
                y = mc.player.getY() + playerHeightOffset.get();
                break;
            case RenderPosition.WorldBottom:
                y = -64;
                break;
            case RenderPosition.Custom:
                y = customY.get();
                break;
            default:
                y = 0;
                break;
        }

        double endY = y + height.get();

        for (int dx = -r; dx <= r; dx++) {
            for (int dz = -r; dz <= r; dz++) {
                ChunkPos chunk = new ChunkPos(
                        center.x + dx,
                        center.z + dz);

                int startX = chunk.getStartX();
                int startZ = chunk.getStartZ();

                event.renderer.box(
                        startX, y, startZ,
                        startX + 16, endY, startZ + 16,
                        sideColor.get(),
                        lineColor.get(),
                        shapeMode.get(),
                        0);
            }
        }
    }

    private enum RenderPosition {
        PlayerHeight,
        PlayerHeightOffset,
        WorldBottom,
        Custom,
    }
}
