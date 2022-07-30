package cqb13.NumbyHack.modules.general;

import cqb13.NumbyHack.NumbyHack;
import meteordevelopment.meteorclient.events.render.Render3DEvent;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.renderer.ShapeMode;
import meteordevelopment.meteorclient.settings.*;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.systems.modules.Modules;
import meteordevelopment.meteorclient.systems.modules.misc.AutoReconnect;
import meteordevelopment.meteorclient.systems.modules.movement.AutoWalk;
import meteordevelopment.meteorclient.systems.modules.movement.SafeWalk;
import meteordevelopment.meteorclient.utils.misc.Pool;
import meteordevelopment.meteorclient.utils.player.FindItemResult;
import meteordevelopment.meteorclient.utils.player.InvUtils;
import meteordevelopment.meteorclient.utils.render.color.Color;
import meteordevelopment.meteorclient.utils.render.color.SettingColor;
import meteordevelopment.meteorclient.utils.world.BlockUtils;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.block.Block;
import net.minecraft.block.FallingBlock;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemStack;
import net.minecraft.network.packet.s2c.play.DisconnectS2CPacket;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * based on Tanuki
 */
public class SafetyNet extends Module {
    private final SettingGroup sgSafety = settings.createGroup("Safety Net");
    private final SettingGroup sgSettings = settings.createGroup("General");
    private final SettingGroup sgAdvanced = settings.createGroup("Advanced");
    private final SettingGroup sgRender = settings.createGroup("Render");

    // safety net

    private final Setting<Integer> yLock = sgSafety.add(new IntSetting.Builder()
        .name("safetynet level")
        .description("The Y level of the safety net.")
        .min(1)
        .max(255)
        .sliderMin(1)
        .sliderMax(255)
        .defaultValue(5)
        .build()
    );

    private final Setting<Integer> safetyNetWindow = sgSafety.add(new IntSetting.Builder()
        .name("safetynet-window")
        .description("The activation window +Y from Y level lock.")
        .min(1)
        .max(32)
        .sliderMin(1)
        .sliderMax(32)
        .defaultValue(3)
        .build()
    );

    private final Setting<Double> safetyNetMultiplier = sgSafety.add(new DoubleSetting.Builder()
        .name("y-slowdown-multiplier")
        .description("Y velocity slowdown multiplier.")
        .min(0.1)
        .max(0.99)
        .sliderMin(0.1)
        .sliderMax(0.99)
        .defaultValue(0.7)
        .build()
    );

    // General

    private final Setting<List<Block>> blocks = sgSettings.add(new BlockListSetting.Builder()
            .name("blocks")
            .description("Selected blocks.")
            .build()
    );

    private final Setting<SafetyNet.ListMode> blocksFilter = sgSettings.add(new EnumSetting.Builder<SafetyNet.ListMode>()
            .name("blocks-filter")
            .description("How to use the block list setting")
            .defaultValue(SafetyNet.ListMode.Blacklist)
            .build()
    );

    private final Setting<Boolean> autoSwitch = sgSettings.add(new BoolSetting.Builder()
            .name("auto-switch")
            .description("Automatically swaps to a block before placing.")
            .defaultValue(true)
            .build()
    );

    private final Setting<Boolean> rotate = sgSettings.add(new BoolSetting.Builder()
            .name("rotate")
            .description("Rotates towards the blocks being placed.")
            .defaultValue(true)
            .build()
    );

    private final Setting<Boolean> toggleOff = sgSettings.add(new BoolSetting.Builder()
            .name("toggle-off")
            .description("Turns off safety net after landing.")
            .defaultValue(true)
            .build()
    );

    private final Setting<Boolean> landingLog = sgSettings.add(new BoolSetting.Builder()
            .name("landing-log")
            .description("Disconnects you after landing.")
            .defaultValue(false)
            .build()
    );

    private final Setting<Boolean> toggleAutoReconnect = sgSettings.add(new BoolSetting.Builder()
            .name("toggle-auto-reconnect")
            .description("Turns off auto reconnect when disconnecting.")
            .defaultValue(true)
            .visible(landingLog::get)
            .build()
    );

    // Advanced

    private final Setting<Boolean> advanced = sgAdvanced.add(new BoolSetting.Builder()
            .name("advanced-settings")
            .description("More settings.")
            .defaultValue(false)
            .build()
    );

    private final Setting<Boolean> toggleSafeWalk = sgAdvanced.add(new BoolSetting.Builder()
            .name("toggle-safe-walk")
            .description("Turns on safe walk after landing.")
            .defaultValue(false)
            .visible(advanced::get)
            .build()
    );

    private final Setting<Boolean> toggleAutoWalk = sgAdvanced.add(new BoolSetting.Builder()
            .name("toggle-auto-walk")
            .description("Turns off auto walk after landing.")
            .defaultValue(false)
            .visible(advanced::get)
            .build()
    );

    private final Setting<Integer> backUpYLock = sgAdvanced.add(new IntSetting.Builder()
            .name("back up safetynet level")
            .description("If safety net does not work at first Y lock it will try again at this height.")
            .min(1)
            .max(255)
            .sliderMin(1)
            .sliderMax(255)
            .defaultValue(5)
            .visible(advanced::get)
            .build()
    );

    private final Setting<Boolean> lastChance = sgAdvanced.add(new BoolSetting.Builder()
            .name("last-resort-log")
            .description("Logs out if safety net does not catch you at either height.")
            .defaultValue(false)
            .visible(advanced::get)
            .build()
    );

    // Render

    private final Setting<Boolean> renderSwing = sgRender.add(new BoolSetting.Builder()
            .name("swing")
            .description("Renders your client-side swing.")
            .defaultValue(false)
            .build()
    );

    private final Setting<ShapeMode> shapeMode = sgRender.add(new EnumSetting.Builder<ShapeMode>()
        .name("shape-mode")
        .description("How the shapes are rendered.")
        .defaultValue(ShapeMode.Both)
        .build()
    );

    private final Setting<SettingColor> sideColor = sgRender.add(new ColorSetting.Builder()
        .name("side-color")
        .description("The side color of the target block rendering.")
        .defaultValue(new SettingColor(146,188,98, 75))
        .build()
    );

    private final Setting<SettingColor> lineColor = sgRender.add(new ColorSetting.Builder()
        .name("line-color")
        .description("The line color of the target block rendering.")
        .defaultValue(new SettingColor(146,188,98, 255))
        .build()
    );

    private final Pool<SafetyNet.RenderBlock> renderBlockPool = new Pool<>(SafetyNet.RenderBlock::new);
    private final List<SafetyNet.RenderBlock> renderBlocks = new ArrayList<>();

    private final BlockPos.Mutable bp = new BlockPos.Mutable();
    private final BlockPos.Mutable prevBp = new BlockPos.Mutable();

    public SafetyNet() {
        super(NumbyHack.CATEGORY, "safety-net", "Places a block under you at a set Y level, 100% chance to stop a void death.");
    }

    @Override
    public void onActivate() {
        for (SafetyNet.RenderBlock renderBlock : renderBlocks) renderBlockPool.free(renderBlock);
        renderBlocks.clear();
    }

    @Override
    public void onDeactivate() {
        for (SafetyNet.RenderBlock renderBlock : renderBlocks) renderBlockPool.free(renderBlock);
        renderBlocks.clear();
    }

    @EventHandler
    private void onTick(TickEvent.Pre event) {
        // Ticking fade animation
        renderBlocks.forEach(SafetyNet.RenderBlock::tick);
        renderBlocks.removeIf(renderBlock -> renderBlock.ticks <= 0);

        // safety net things
        assert mc.player != null;
        if (lastChance.get() && mc.player.getY() < backUpYLock.get() - safetyNetWindow.get() + 1) {
            mc.player.networkHandler.onDisconnect(new DisconnectS2CPacket(Text.literal("[Safety Net] Safety Net could not catch you.")));
            if (Modules.get().isActive(AutoReconnect.class)) Modules.get().get(AutoReconnect.class).toggle();
            this.toggle();
        }
        
        if (mc.player.getY() < yLock.get() + safetyNetWindow.get() + 1) 

        // dont read thus wrote it very late and itd very ba
        if ((mc.player.getY() < (yLock.get() + safetyNetWindow.get()) - 1 && mc.player.getY() > (yLock.get() - safetyNetWindow.get())) || mc.player.getY() < (backUpYLock.get() + safetyNetWindow.get())) {
            assert mc.world != null;
            if (mc.world.getBlockState(mc.player.getBlockPos().down()).isAir()) {
                mc.player.setVelocity(mc.player.getVelocity().getX(), mc.player.getVelocity().getY() * safetyNetMultiplier.get(), mc.player.getVelocity().getZ());
                Vec3d vec = mc.player.getPos().add(mc.player.getVelocity()).add(0, -0.5f, 0);
                if (mc.player.getY() < (backUpYLock.get() + safetyNetWindow.get())) {
                    bp.set(vec.getX(), backUpYLock.get(), vec.getZ());
                } else {
                    bp.set(vec.getX(), yLock.get(), vec.getZ());
                }
            } else {
                if (toggleAutoReconnect.get() && Modules.get().isActive(AutoReconnect.class)) Modules.get().get(AutoReconnect.class).toggle();
                if (toggleSafeWalk.get() && !Modules.get().isActive(SafeWalk.class)) Modules.get().get(SafeWalk.class).toggle();
                if (toggleAutoWalk.get() && Modules.get().isActive(AutoWalk.class)) Modules.get().get(AutoWalk.class).toggle();
                if (landingLog.get()) mc.player.networkHandler.onDisconnect(new DisconnectS2CPacket(Text.literal("[Safety Net] Safety Net caught you.")));
                if (toggleOff.get()) {
                    info ("Safetynet landed | disabling");
                    this.toggle();
                }
            }
        }

        FindItemResult item = InvUtils.findInHotbar(itemStack -> validItem(itemStack, bp));
        if (!item.found()) return;


        if (item.getHand() == null && !autoSwitch.get()) return;

        if (BlockUtils.place(bp, item, rotate.get(), 50, renderSwing.get(), true)) {
            // Render block if was placed
            renderBlocks.add(renderBlockPool.get().set(bp));

            // Move player down, so they are on top of the placed block ready to jump again
            if (mc.options.jumpKey.isPressed() && !mc.options.sneakKey.isPressed() && !mc.player.isOnGround()) {
                assert mc.world != null;
                if (!mc.world.getBlockState(bp).isAir()) {
                    mc.player.setVelocity(0, -0.28f, 0);
                }
            }
        }

        assert mc.world != null;
        if (!mc.world.getBlockState(bp).isAir()) {
            prevBp.set(bp);
        }
    }

    private boolean validItem(ItemStack itemStack, BlockPos pos) {
        if (!(itemStack.getItem() instanceof BlockItem)) return false;

        Block block = ((BlockItem) itemStack.getItem()).getBlock();

        if (blocksFilter.get() == SafetyNet.ListMode.Blacklist && blocks.get().contains(block)) return false;
        else if (blocksFilter.get() == SafetyNet.ListMode.Whitelist && !blocks.get().contains(block)) return false;

        if (!Block.isShapeFullCube(block.getDefaultState().getCollisionShape(mc.world, pos))) return false;
        if (!(block instanceof FallingBlock)) return true;
        assert mc.world != null;
        return !FallingBlock.canFallThrough(mc.world.getBlockState(pos));
    }

    //TODO: fix render box location

    @EventHandler
    private void onRender(Render3DEvent event) {
        renderBlocks.sort(Comparator.comparingInt(o -> -o.ticks));
        renderBlocks.forEach(renderBlock -> renderBlock.render(event, sideColor.get(), lineColor.get(), shapeMode.get()));
    }

    // Rendering

    public enum ListMode {
        Whitelist,
        Blacklist
    }

    public static class RenderBlock {
        public BlockPos.Mutable pos = new BlockPos.Mutable();
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

