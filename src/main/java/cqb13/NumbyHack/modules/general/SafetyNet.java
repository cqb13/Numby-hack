package cqb13.NumbyHack.modules.general;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import cqb13.NumbyHack.NumbyHack;
import meteordevelopment.meteorclient.events.render.Render3DEvent;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.renderer.ShapeMode;
import meteordevelopment.meteorclient.settings.BlockListSetting;
import meteordevelopment.meteorclient.settings.BoolSetting;
import meteordevelopment.meteorclient.settings.ColorSetting;
import meteordevelopment.meteorclient.settings.DoubleSetting;
import meteordevelopment.meteorclient.settings.EnumSetting;
import meteordevelopment.meteorclient.settings.IntSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.utils.misc.Pool;
import meteordevelopment.meteorclient.utils.player.FindItemResult;
import meteordevelopment.meteorclient.utils.player.InvUtils;
import meteordevelopment.meteorclient.utils.player.PlayerUtils;
import meteordevelopment.meteorclient.utils.render.color.Color;
import meteordevelopment.meteorclient.utils.render.color.SettingColor;
import meteordevelopment.meteorclient.utils.world.BlockUtils;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.FallingBlock;
import net.minecraft.world.phys.Vec3;

/**
 * based on Tanuki
 */
public class SafetyNet extends Module {
    private final SettingGroup sgGeneral = settings.getDefaultGroup();
    private final SettingGroup sgRender = settings.createGroup("Render");

    // safety net
    private final Setting<SafetyNet.PlaceMode> safetyMode = sgGeneral.add(new EnumSetting.Builder<SafetyNet.PlaceMode>()
            .name("safetynet")
            .description("Which safety mode to use.")
            .defaultValue(PlaceMode.SafetyNet)
            .build());

    private final Setting<Integer> yLock = sgGeneral.add(new IntSetting.Builder()
            .name("safetynet level")
            .description("The Y level of the safety net.")
            .min(1)
            .max(255)
            .sliderMin(1)
            .sliderMax(255)
            .defaultValue(1)
            .build());

    private final Setting<Integer> safetyNetWindow = sgGeneral.add(new IntSetting.Builder()
            .name("safetynet-window")
            .description("The activation window +Y from Y level lock.")
            .min(1)
            .max(32)
            .sliderMin(1)
            .sliderMax(32)
            .defaultValue(10)
            .build());

    private final Setting<Double> safetyNetMultiplier = sgGeneral.add(new DoubleSetting.Builder()
            .name("y-slowdown-multiplier")
            .description("Y velocity slowdown multiplier.")
            .min(0.1)
            .max(0.99)
            .sliderMin(0.1)
            .sliderMax(0.99)
            .defaultValue(0.7)
            .build());

    // general

    private final Setting<Boolean> autoSwitch = sgGeneral.add(new BoolSetting.Builder()
            .name("auto-switch")
            .description("Automatically swaps to a block before placing.")
            .defaultValue(true)
            .build());

    private final Setting<Boolean> renderSwing = sgGeneral.add(new BoolSetting.Builder()
            .name("swing")
            .description("Renders your client-side swing.")
            .defaultValue(false)
            .build());

    private final Setting<Boolean> rotate = sgGeneral.add(new BoolSetting.Builder()
            .name("rotate")
            .description("Rotates towards the blocks being placed.")
            .defaultValue(true)
            .build());

    private final Setting<List<Block>> blocks = sgGeneral.add(new BlockListSetting.Builder()
            .name("blocks")
            .description("Selected blocks.")
            .build());

    private final Setting<SafetyNet.ListMode> blocksFilter = sgGeneral.add(new EnumSetting.Builder<SafetyNet.ListMode>()
            .name("blocks-filter")
            .description("How to use the block list setting")
            .defaultValue(SafetyNet.ListMode.Blacklist)
            .build());

    // Render

    private final Setting<ShapeMode> shapeMode = sgRender.add(new EnumSetting.Builder<ShapeMode>()
            .name("shape-mode")
            .description("How the shapes are rendered.")
            .defaultValue(ShapeMode.Both)
            .build());

    private final Setting<SettingColor> sideColor = sgRender.add(new ColorSetting.Builder()
            .name("side-color")
            .description("The side color of the target block rendering.")
            .defaultValue(new SettingColor(146, 188, 98, 75))
            .build());

    private final Setting<SettingColor> lineColor = sgRender.add(new ColorSetting.Builder()
            .name("line-color")
            .description("The line color of the target block rendering.")
            .defaultValue(new SettingColor(146, 188, 98, 255))
            .build());

    private final Pool<SafetyNet.RenderBlock> renderBlockPool = new Pool<>(SafetyNet.RenderBlock::new);
    private final List<SafetyNet.RenderBlock> renderBlocks = new ArrayList<>();

    private final BlockPos.MutableBlockPos bp = new BlockPos.MutableBlockPos();
    private final BlockPos.MutableBlockPos prevBp = new BlockPos.MutableBlockPos();

    private boolean lastWasSneaking;
    private double lastSneakingY;
    private double placeRange = 4;

    public SafetyNet() {
        super(NumbyHack.CATEGORY, "safety-net", "Places a block under you at a set Y level.");
    }

    @Override
    public void onActivate() {
        lastWasSneaking = mc.options.keyShift.isDown();
        if (lastWasSneaking)
            lastSneakingY = mc.player.getY();

        for (SafetyNet.RenderBlock renderBlock : renderBlocks)
            renderBlockPool.free(renderBlock);
        renderBlocks.clear();
    }

    @Override
    public void onDeactivate() {
        for (SafetyNet.RenderBlock renderBlock : renderBlocks)
            renderBlockPool.free(renderBlock);
        renderBlocks.clear();
    }

    @EventHandler
    private void onTick(TickEvent.Pre event) {
        // Ticking fade animation
        renderBlocks.forEach(SafetyNet.RenderBlock::tick);
        renderBlocks.removeIf(renderBlock -> renderBlock.ticks <= 0);

        if (safetyMode.get() == PlaceMode.AirPlace) {
            Vec3 vec = mc.player.position().add(mc.player.getDeltaMovement()).add(0, -0.5f, 0);
            bp.set(vec.x(), vec.y(), vec.z());
        } else if (safetyMode.get() == PlaceMode.SafetyNet) {
            if (mc.player.getY() < (yLock.get() + safetyNetWindow.get()) - 1) {
                if (mc.level.getBlockState(mc.player.blockPosition().below()).isAir()) {
                    mc.player.setDeltaMovement(mc.player.getDeltaMovement().x(),
                            mc.player.getDeltaMovement().y() * safetyNetMultiplier.get(),
                            mc.player.getDeltaMovement().z());
                    Vec3 vec = mc.player.position().add(mc.player.getDeltaMovement()).add(0, -0.5f, 0);
                    bp.set(vec.x(), yLock.get(), vec.z());
                } else {
                    info("Safetynet landed on block | disabling");
                    toggle();
                }
            }
        } else {
            if (BlockUtils.getPlaceSide(mc.player.blockPosition().below()) != null) {
                bp.set(mc.player.blockPosition().below());
            } else {
                Vec3 pos = mc.player.position();
                pos = pos.add(0, -0.98f, 0);
                pos.add(mc.player.getDeltaMovement());

                if (PlayerUtils.distanceTo(prevBp) > placeRange) {
                    List<BlockPos> blockPosArray = new ArrayList<>();

                    for (int x = (int) (mc.player.getX() - placeRange); x < mc.player.getX() + placeRange; x++) {
                        for (int z = (int) (mc.player.getZ() - placeRange); z < mc.player.getZ() + placeRange; z++) {
                            for (int y = (int) Math.max(mc.level.getMinY(), mc.player.getY() - placeRange); y < Math
                                    .min(mc.level.getMaxY(), mc.player.getY() + placeRange); y++) {
                                bp.set(x, y, z);
                                if (!mc.level.getBlockState(bp).isAir()) {
                                    blockPosArray.add(new BlockPos(x, y, z));
                                }
                            }
                        }
                    }
                    if (blockPosArray.size() == 0) {
                        return;
                    }

                    blockPosArray.sort(Comparator.comparingDouble(PlayerUtils::distanceTo));

                    prevBp.set(blockPosArray.get(0));
                }

                Vec3 vecPrevBP = new Vec3((double) prevBp.getX() + 0.5f,
                        (double) prevBp.getY() + 0.5f,
                        (double) prevBp.getZ() + 0.5f);

                Vec3 sub = pos.subtract(vecPrevBP);
                Direction facing;
                if (sub.y() < -0.5f) {
                    facing = Direction.DOWN;
                } else if (sub.y() > 0.5f) {
                    facing = Direction.UP;
                } else
                    facing = Direction.getApproximateNearest(sub.x(), 0, sub.z());

                bp.set(prevBp.relative(facing));
            }
        }

        FindItemResult item = InvUtils.findInHotbar(itemStack -> validItem(itemStack, bp));
        if (!item.found())
            return;

        if (item.getHand() == null && !autoSwitch.get())
            return;

        // Move down if shifting
        if (mc.options.keyShift.isDown() && !mc.options.keyJump.isDown()) {
            if (lastSneakingY - mc.player.getY() < 0.1) {
                lastWasSneaking = false;
                return;
            }
        } else {
            lastWasSneaking = false;
        }
        if (!lastWasSneaking)
            lastSneakingY = mc.player.getY();

        if (mc.options.keyJump.isDown() && !mc.options.keyShift.isDown()) {
            mc.player.setDeltaMovement(0, 0.42f, 0);
        }

        if (BlockUtils.place(bp, item, rotate.get(), 50, renderSwing.get(), true)) {
            // Render block if was placed
            renderBlocks.add(renderBlockPool.get().set(bp));

            // Move player down so they are on top of the placed block ready to jump again
            if (mc.options.keyJump.isDown() && !mc.options.keyShift.isDown() && !mc.player.onGround()
                    && !mc.level.getBlockState(bp).isAir()) {
                mc.player.setDeltaMovement(0, -0.28f, 0);
            }
        }

        if (!mc.level.getBlockState(bp).isAir()) {
            prevBp.set(bp);
        }
    }

    private boolean validItem(ItemStack itemStack, BlockPos pos) {
        if (!(itemStack.getItem() instanceof BlockItem))
            return false;

        Block block = ((BlockItem) itemStack.getItem()).getBlock();

        if (blocksFilter.get() == SafetyNet.ListMode.Blacklist && blocks.get().contains(block))
            return false;
        else if (blocksFilter.get() == SafetyNet.ListMode.Whitelist && !blocks.get().contains(block))
            return false;

        if (!Block.isShapeFullBlock(block.defaultBlockState().getCollisionShape(mc.level, pos)))
            return false;
        return !(block instanceof FallingBlock) || !FallingBlock.isFree(mc.level.getBlockState(pos));
    }

    @EventHandler
    private void onRender(Render3DEvent event) {
        renderBlocks.sort(Comparator.comparingInt(o -> -o.ticks));
        renderBlocks
                .forEach(renderBlock -> renderBlock.render(event, sideColor.get(), lineColor.get(), shapeMode.get()));
    }

    // Rendering

    public enum ListMode {
        Whitelist,
        Blacklist
    }

    public enum PlaceMode {
        SafetyNet,
        AirPlace
    }

    public static class RenderBlock {
        public BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();
        public int ticks;

        public RenderBlock set(BlockPos blockPos) {
            pos.set(blockPos);
            ticks = 8;

            return this;
        }

        public void tick() {
            ticks--;
        }

        public void render(Render3DEvent event, Color sides, Color lines, ShapeMode shapeMode) {
            int preSideA = sides.a;
            int preLineA = lines.a;

            sides.a *= (double) ticks / 8;
            lines.a *= (double) ticks / 8;

            event.renderer.box(pos, sides, lines, shapeMode, 0);

            sides.a = preSideA;
            lines.a = preLineA;
        }
    }
}
