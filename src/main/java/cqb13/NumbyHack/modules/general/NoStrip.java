package cqb13.NumbyHack.modules.general;

import cqb13.NumbyHack.NumbyHack;
import meteordevelopment.meteorclient.events.entity.player.InteractBlockEvent;
import meteordevelopment.meteorclient.settings.BoolSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.utils.misc.Names;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;

/**
 * made by cqb13
 */
public class NoStrip extends Module {
    private final SettingGroup sgBlocks = settings.createGroup("Blocks");

    private final Setting<Boolean> swingHand = sgBlocks.add(new BoolSetting.Builder()
            .name("swing-hand")
            .description("Renders swing hand animation.")
            .defaultValue(true)
            .build());

    public NoStrip() {
        super(NumbyHack.CATEGORY, "no-strip", "Prevents you from stripping logs.");
    }

    @EventHandler
    private void onInteractBlock(InteractBlockEvent event) {
        if (!shouldInteractBlock(event.result))
            event.cancel();
    }

    private boolean shouldInteractBlock(BlockHitResult hitResult) {
        if (mc.player.getMainHandItem().getItem().toString().contains("axe")) {
            if (mc.hitResult.getType() == HitResult.Type.BLOCK) {
                BlockPos pos = ((BlockHitResult) mc.hitResult).getBlockPos();
                String result = Names.get(mc.level.getBlockState(pos).getBlock());
                if (result.contains("Log") || result.contains("Crimson Stem") || result.contains("Warped Stem")) {
                    if (swingHand.get()) {
                        mc.player.swing(mc.player.getUsedItemHand(),
                                mc.player.getItemInHand(mc.player.getUsedItemHand()).getInteractAnimation(), false);
                    }
                    if (chatFeedback) {
                        info("You can't strip logs!");
                    }
                    return false;
                }
            }
        }
        return true;
    }
}
