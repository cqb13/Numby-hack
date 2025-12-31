package cqb13.NumbyHack.modules.general;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReferenceArray;

import cqb13.NumbyHack.NumbyHack;
import meteordevelopment.meteorclient.events.game.GameLeftEvent;
import meteordevelopment.meteorclient.events.render.Render3DEvent;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.settings.BoolSetting;
import meteordevelopment.meteorclient.settings.ColorSetting;
import meteordevelopment.meteorclient.settings.EnumSetting;
import meteordevelopment.meteorclient.settings.IntSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.utils.Utils;
import meteordevelopment.meteorclient.utils.render.color.SettingColor;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.ComparatorBlock;
import net.minecraft.block.ObserverBlock;
import net.minecraft.block.SculkShriekerBlock;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.CalibratedSculkSensorBlockEntity;
import net.minecraft.block.entity.SculkSensorBlockEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraft.world.chunk.WorldChunk;

public class SculkRangeEsp extends Module {
    private final SettingGroup sgGeneral = settings.getDefaultGroup();

    private final Setting<Boolean> hideWhenSneaking = sgGeneral.add(new BoolSetting.Builder()
            .name("hide-when-sneaking")
            .description("Hides range indicator when the player is sneaking.")
            .defaultValue(false)
            .build());

    private final Setting<Boolean> onlyRenderImpactful = sgGeneral.add(new BoolSetting.Builder()
            .name("only-render-impactful")
            .description("Only shows the ranges of sculk sensors that can trigger redstone or shriekers.")
            .defaultValue(false)
            .build());

    public final Setting<Integer> renderDistance = sgGeneral.add(new IntSetting.Builder()
            .name("render-distance")
            .description("How many chunks around the player to show detected spawners.")
            .defaultValue(32)
            .min(6)
            .sliderRange(6, 1024)
            .build());

    public final Setting<Boolean> smartRendering = sgGeneral.add(new BoolSetting.Builder()
            .name("smart-rendering")
            .description("Renders ranges as circles, switches to spheres when the player get with a certain range.")
            .defaultValue(true)
            .build());

    public final Setting<Integer> smartRenderDistance = sgGeneral.add(new IntSetting.Builder()
            .name("smart-render-distance")
            .description("How many blocks away from the sensor to switch to sphere rendering.")
            .defaultValue(10)
            .min(1)
            .sliderRange(1, 1024)
            .visible(smartRendering::get)
            .build());

    private final Setting<Shape> shape = sgGeneral.add(new EnumSetting.Builder<Shape>()
            .name("shape")
            .description("The shape type to use.")
            .defaultValue(Shape.Circle)
            .visible(() -> !smartRendering.get())
            .build());

    private final Setting<SettingColor> shapeColor = sgGeneral.add(new ColorSetting.Builder()
            .name("shape-color")
            .description("Color of the range indicator.")
            .defaultValue(new SettingColor(146, 188, 98, 255))
            .build());

    private final Setting<Boolean> advancedView = sgGeneral.add(new BoolSetting.Builder()
            .name("advanced-view")
            .description("Detects and indicats sculk sensors that give redstone output or have shriekers in range.")
            .defaultValue(false)
            .build());

    private final Setting<SettingColor> redstoneColor = sgGeneral.add(new ColorSetting.Builder()
            .name("redstone-shape-color")
            .description("Color of the range indicator with redstone output.")
            .defaultValue(new SettingColor(170, 0, 0, 255))
            .visible(advancedView::get)
            .build());

    private final Setting<SettingColor> shriekerColor = sgGeneral.add(new ColorSetting.Builder()
            .name("shrieker-shape-color")
            .description("Color of the range indicator with a shrieker in range.")
            .defaultValue(new SettingColor(0, 69, 170, 255))
            .visible(advancedView::get)
            .build());

    private final Setting<Boolean> renderAtPlayerHeight = sgGeneral.add(new BoolSetting.Builder()
            .name("render-circle-at-player-height")
            .description("Render the circular range indicator at player height.")
            .defaultValue(false)
            .visible(() -> shape.get() == Shape.Circle || shape.get() == Shape.Both)
            .build());

    public Setting<Integer> gradation = sgGeneral.add(new IntSetting.Builder()
            .name("gradation")
            .description(
                    "Determines the smoothness/detail of the sphere shape. Higher values produce a smoother sphere, lower values make it more blocky.")
            .defaultValue(30)
            .range(20, 100)
            .sliderRange(20, 100)
            .visible(() -> shape.get() == Shape.Sphere || shape.get() == Shape.Both)
            .build());

    private final Set<BlockPos> positions = Collections.synchronizedSet(new HashSet<>());
    private final Set<FoundSensor> foundSensors = Collections.synchronizedSet(new HashSet<>());
    Vec3d renderPos = new Vec3d(0, 0, 0);

    public SculkRangeEsp() {
        super(NumbyHack.CATEGORY, "sculk-range-esp",
                "Draws a range indicator around the activation range of sculk sensors.");
    }

    @Override
    public void onDeactivate() {
        foundSensors.clear();
        positions.clear();
    }

    @EventHandler
    private void onGameLeft(GameLeftEvent event) {
        foundSensors.clear();
        positions.clear();
    }

    @EventHandler
    private void onPreTick(TickEvent.Pre event) {
        if (mc.world == null || mc.player == null)
            return;
        AtomicReferenceArray<WorldChunk> chunks = mc.world.getChunkManager().chunks.chunks;
        Set<WorldChunk> chunkSet = new HashSet<>();

        for (int i = 0; i < chunks.length(); i++) {
            WorldChunk chunk = chunks.get(i);
            if (chunk != null) {
                chunkSet.add(chunk);
            }
        }

        chunkSet.forEach(chunk -> extracted(chunk));
    }

    private void extracted(WorldChunk chunk) {
        List<BlockEntity> blockEntities = new ArrayList<>(chunk.getBlockEntities().values());

        for (BlockEntity blockEntity : blockEntities) {
            BlockPos pos = blockEntity.getPos();

            if (positions.contains(pos)) {
                continue;
            }

            SensorType sensor = null;

            if (blockEntity instanceof CalibratedSculkSensorBlockEntity) {
                sensor = SensorType.Calibrated;
            } else if (blockEntity instanceof SculkSensorBlockEntity) {
                sensor = SensorType.Regular;
            } else {
                continue;
            }

            boolean hasOutput = hasRedstoneOutput(chunk.getWorld(), pos);
            boolean hasShriekerInRange = hasShriekerInRange(chunk.getWorld(), pos, sensor);

            foundSensors.add(new FoundSensor(sensor, pos, hasOutput, hasShriekerInRange));
            positions.add(pos);

        }
    }

    private boolean hasRedstoneOutput(World world, BlockPos sensorPos) {
        for (Direction dir : Direction.values()) {
            BlockPos adjacentPos = sensorPos.offset(dir);
            BlockState state = world.getBlockState(adjacentPos);
            Block block = state.getBlock();

            // redstone dust next to the sensor
            if (block == Blocks.REDSTONE_WIRE) {
                return true;
            }

            // comparator coming out of sensor
            if (block instanceof ComparatorBlock) {
                Direction facing = state.get(ComparatorBlock.FACING);

                if (adjacentPos.offset(facing.getOpposite()).equals(sensorPos)) {
                    return true;
                }
            }

            // observer observing sensor
            if (block instanceof ObserverBlock) {
                Direction facing = state.get(ObserverBlock.FACING);

                if (adjacentPos.offset(facing).equals(sensorPos)) {
                    return true;
                }
            }
        }

        return false;
    }

    private boolean hasShriekerInRange(World world, BlockPos sensorPos, SensorType sensorType) {
        int range = switch (sensorType) {
            case Regular -> 8;
            case Calibrated -> 16;
        };

        for (BlockPos checkPos : BlockPos.iterateOutwards(sensorPos, range, range, range)) {
            BlockState state = world.getBlockState(checkPos);

            if (state.getBlock() instanceof SculkShriekerBlock) {
                return true;
            }
        }

        return false;
    }

    @EventHandler
    private void onRender(Render3DEvent event) {
        if (!Utils.canUpdate())
            return;

        if (hideWhenSneaking.get() && mc.player.isSneaking()) {
            return;
        }

        synchronized (foundSensors) {
            for (FoundSensor sensor : foundSensors) {
                if (onlyRenderImpactful.get() && !sensor.hasRedstoneOutput() && !sensor.shriekerInRange())
                    continue;

                BlockPos playerPos = new BlockPos(mc.player.getBlockX(), sensor.pos.getY(), mc.player.getBlockZ());
                if (!playerPos.isWithinDistance(sensor.pos, renderDistance.get() * 16))
                    continue;

                double radius = (sensor.sensorType() == SensorType.Calibrated) ? 16 : 8;

                boolean useSphere = false;

                if (smartRendering.get()) {
                    double distance = mc.player.getEntityPos().distanceTo(Vec3d.ofCenter(sensor.pos));
                    useSphere = distance <= smartRenderDistance.get();
                } else {
                    useSphere = shape.get() == Shape.Sphere || shape.get() == Shape.Both;
                }

                if (useSphere) {
                    renderSphere(event, radius, gradation.get(), sensor);
                }

                if (!useSphere && (shape.get() == Shape.Circle || shape.get() == Shape.Both || smartRendering.get())) {
                    BlockPos renderPos = sensor.pos;
                    if (renderAtPlayerHeight.get()) {
                        renderPos = new BlockPos(sensor.pos.getX(), (int) mc.player.getY(), sensor.pos.getZ());
                    }
                    renderCircle(event, radius, renderPos, sensor);
                }
            }
        }
    }

    private void renderCircle(Render3DEvent event, double radius, BlockPos origin, FoundSensor sensor) {
        final double maxSegmentLength = 0.2;

        int segments = (int) Math.ceil(2 * Math.PI * radius / maxSegmentLength);
        segments = Math.max(16, segments);

        Vec3d[] pts = new Vec3d[segments];
        SettingColor[] cols = new SettingColor[segments];

        boolean both = sensor.hasRedstoneOutput() && sensor.shriekerInRange();
        boolean advanced = advancedView.get();

        for (int s = 0; s < segments; s++) {
            double angle = 2 * Math.PI * s / segments;
            double sin = Math.sin(angle) * radius;
            double cos = Math.cos(angle) * radius;
            pts[s] = new Vec3d(origin.getX() + sin, origin.getY(), origin.getZ() + cos);

            int deg = (int) Math.round(Math.toDegrees(angle)) % 360;

            SettingColor color;
            if (!advanced) {
                color = shapeColor.get();
            } else if (both) {
                int quadrant = (deg / 90) % 4;
                color = (quadrant % 2 == 0) ? redstoneColor.get() : shriekerColor.get();
            } else if (sensor.hasRedstoneOutput()) {
                color = redstoneColor.get();
            } else if (sensor.shriekerInRange()) {
                color = shriekerColor.get();
            } else {
                color = shapeColor.get();
            }

            cols[s] = color;
        }

        for (int s = 0; s < segments; s++) {
            int next = (s + 1) % segments;
            event.renderer.line(
                    pts[s].x, pts[s].y, pts[s].z,
                    pts[next].x, pts[next].y, pts[next].z,
                    cols[next]);
        }
    }

    private void renderSphere(Render3DEvent event, double radius, int gradation, FoundSensor sensor) {
        boolean advanced = advancedView.get();
        boolean both = sensor.hasRedstoneOutput() && sensor.shriekerInRange();

        for (float alpha = 0.0f; alpha < Math.PI; alpha += Math.PI / gradation) {
            for (float beta = 0.0f; beta < 2.0 * Math.PI; beta += Math.PI / gradation) {
                double x1 = sensor.pos.getX() + radius * Math.cos(beta) * Math.sin(alpha);
                double y1 = sensor.pos.getY() + radius * Math.sin(beta) * Math.sin(alpha);
                double z1 = sensor.pos.getZ() + radius * Math.cos(alpha);

                double sin = Math.sin(alpha + Math.PI / gradation);
                double x2 = sensor.pos.getX() + radius * Math.cos(beta) * sin;
                double y2 = sensor.pos.getY() + radius * Math.sin(beta) * sin;
                double z2 = sensor.pos.getZ() + radius * Math.cos(alpha + Math.PI / gradation);

                SettingColor color;

                if (!advanced) {
                    color = shapeColor.get();
                } else if (both) {
                    int segmentIndex = (int) (alpha / (Math.PI / gradation) + beta / (Math.PI / gradation));
                    color = (segmentIndex % 2 == 0) ? redstoneColor.get() : shriekerColor.get();
                } else if (sensor.hasRedstoneOutput()) {
                    color = redstoneColor.get();
                } else if (sensor.shriekerInRange()) {
                    color = shriekerColor.get();
                } else {
                    color = shapeColor.get();
                }

                event.renderer.line(x1, y1, z1, x2, y2, z2, color);
            }
        }
    }

    private record FoundSensor(SensorType sensorType, BlockPos pos, boolean hasRedstoneOutput,
            boolean shriekerInRange) {
    }

    private enum SensorType {
        Regular,
        Calibrated,
    }

    private enum Shape {
        Circle,
        Sphere,
        Both,
    }
}
