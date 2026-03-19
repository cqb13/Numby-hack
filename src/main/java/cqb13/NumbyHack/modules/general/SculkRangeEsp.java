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
import meteordevelopment.meteorclient.settings.DoubleSetting;
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

/**
 * made by cqb13
 */
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
            .defaultValue(new SettingColor(146, 188, 98, 75))
            .build());

    private final Setting<Boolean> advancedView = sgGeneral.add(new BoolSetting.Builder()
            .name("advanced-view")
            .description("Detects and indicats sculk sensors that give redstone output or have shriekers in range.")
            .defaultValue(false)
            .build());

    private final Setting<SettingColor> redstoneColor = sgGeneral.add(new ColorSetting.Builder()
            .name("redstone-shape-color")
            .description("Color of the range indicator with redstone output.")
            .defaultValue(new SettingColor(170, 0, 0, 75))
            .visible(advancedView::get)
            .build());

    private final Setting<SettingColor> shriekerColor = sgGeneral.add(new ColorSetting.Builder()
            .name("shrieker-shape-color")
            .description("Color of the range indicator with a shrieker in range.")
            .defaultValue(new SettingColor(0, 69, 170, 75))
            .visible(advancedView::get)
            .build());

    private final Setting<Boolean> renderAtPlayerHeight = sgGeneral.add(new BoolSetting.Builder()
            .name("render-circle-at-player-height")
            .description("Render the circular range indicator at player height.")
            .defaultValue(false)
            .visible(() -> shape.get() == Shape.Circle || shape.get() == Shape.Both)
            .build());

    private final Setting<Double> circleThickness = sgGeneral.add(new DoubleSetting.Builder()
            .name("circle-thickness")
            .description("The thickness of the circle.")
            .defaultValue(0.08)
            .sliderMax(1)
            .visible(() -> shape.get() == Shape.Sphere || shape.get() == Shape.Both)
            .build());

    public Setting<Integer> gradation = sgGeneral.add(new IntSetting.Builder()
            .name("gradation")
            .description(
                    "Determines the smoothness of the sphere shape. Higher values produce a smoother sphere, lower values make it more blocky.")
            .defaultValue(30)
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
                    renderCircle(event, radius, circleThickness.get(), renderPos, sensor);
                }
            }
        }
    }

    private void renderCircle(Render3DEvent event, double radius, double thickness, BlockPos origin,
            FoundSensor sensor) {
        final double maxSegmentLength = 0.2;

        int segments = (int) Math.ceil(2 * Math.PI * radius / maxSegmentLength);
        segments = Math.max(16, segments);

        Vec3d[] outerPts = new Vec3d[segments];
        Vec3d[] innerPts = new Vec3d[segments];
        SettingColor[] cols = new SettingColor[segments];

        boolean both = sensor.hasRedstoneOutput() && sensor.shriekerInRange();
        boolean advanced = advancedView.get();

        double outerRadius = radius + thickness / 2.0;
        double innerRadius = radius - thickness / 2.0;

        if (innerRadius < 0) {
            innerRadius = 0;
        }

        for (int s = 0; s < segments; s++) {
            double angle = 2 * Math.PI * s / segments;
            double sin = Math.sin(angle);
            double cos = Math.cos(angle);

            outerPts[s] = new Vec3d(
                    origin.getX() + sin * outerRadius,
                    origin.getY(),
                    origin.getZ() + cos * outerRadius);

            innerPts[s] = new Vec3d(
                    origin.getX() + sin * innerRadius,
                    origin.getY(),
                    origin.getZ() + cos * innerRadius);

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

            event.renderer.triangles.ensureTriCapacity();

            int outer1 = event.renderer.triangles
                    .vec3(outerPts[s].x, outerPts[s].y, outerPts[s].z)
                    .color(cols[s])
                    .next();

            int outer2 = event.renderer.triangles
                    .vec3(outerPts[next].x, outerPts[next].y, outerPts[next].z)
                    .color(cols[next])
                    .next();

            int inner1 = event.renderer.triangles
                    .vec3(innerPts[s].x, innerPts[s].y, innerPts[s].z)
                    .color(cols[s])
                    .next();

            int inner2 = event.renderer.triangles
                    .vec3(innerPts[next].x, innerPts[next].y, innerPts[next].z)
                    .color(cols[next])
                    .next();

            event.renderer.triangles.triangle(outer1, outer2, inner1);
            event.renderer.triangles.triangle(inner1, outer2, inner2);
        }
    }

    private void renderSphere(Render3DEvent event, double radius, int gradation, FoundSensor sensor) {
        boolean advanced = advancedView.get();
        boolean both = sensor.hasRedstoneOutput() && sensor.shriekerInRange();

        double cx = sensor.pos.getX();
        double cy = sensor.pos.getY();
        double cz = sensor.pos.getZ();

        int horizontalSteps = Math.max(8, gradation);
        int verticalSteps = Math.max(16, gradation * 2);

        for (int hor = 0; hor < horizontalSteps; hor++) {
            double theta1 = Math.PI * hor / horizontalSteps;
            double theta2 = Math.PI * (hor + 1) / horizontalSteps;

            for (int vert = 0; vert < verticalSteps; vert++) {
                double phi1 = 2.0 * Math.PI * vert / verticalSteps;
                double phi2 = 2.0 * Math.PI * (vert + 1) / verticalSteps;

                Vec3d p1 = spherePoint(cx, cy, cz, radius, theta1, phi1);
                Vec3d p2 = spherePoint(cx, cy, cz, radius, theta1, phi2);
                Vec3d p3 = spherePoint(cx, cy, cz, radius, theta2, phi2);
                Vec3d p4 = spherePoint(cx, cy, cz, radius, theta2, phi1);

                SettingColor color;
                if (!advanced) {
                    color = shapeColor.get();
                } else if (both) {
                    int segmentIndex = hor + vert;
                    color = (segmentIndex % 2 == 0) ? redstoneColor.get() : shriekerColor.get();
                } else if (sensor.hasRedstoneOutput()) {
                    color = redstoneColor.get();
                } else if (sensor.shriekerInRange()) {
                    color = shriekerColor.get();
                } else {
                    color = shapeColor.get();
                }

                event.renderer.triangles.ensureQuadCapacity();

                int i1 = event.renderer.triangles.vec3(p1.x, p1.y, p1.z).color(color).next();
                int i2 = event.renderer.triangles.vec3(p2.x, p2.y, p2.z).color(color).next();
                int i3 = event.renderer.triangles.vec3(p3.x, p3.y, p3.z).color(color).next();
                int i4 = event.renderer.triangles.vec3(p4.x, p4.y, p4.z).color(color).next();

                event.renderer.triangles.triangle(i1, i2, i3);
                event.renderer.triangles.triangle(i1, i3, i4);
            }
        }
    }

    private Vec3d spherePoint(double cx, double cy, double cz, double r, double theta, double phi) {
        double sinTheta = Math.sin(theta);

        double x = cx + r * sinTheta * Math.cos(phi);
        double y = cy + r * Math.cos(theta);
        double z = cz + r * sinTheta * Math.sin(phi);

        return new Vec3d(x, y, z);
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
