package cqb13.NumbyHack.modules.general;

import cqb13.NumbyHack.NumbyHack;
import meteordevelopment.meteorclient.events.entity.player.InteractBlockEvent;
import meteordevelopment.meteorclient.settings.BoolSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.utils.misc.Names;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;

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
    if (mc.player.getMainHandStack().getItem().toString().contains("axe")) {
      if (mc.crosshairTarget.getType() == HitResult.Type.BLOCK) {
        BlockPos pos = ((BlockHitResult) mc.crosshairTarget).getBlockPos();
        String result = Names.get(mc.world.getBlockState(pos).getBlock());
        if (result.contains("Log")) {
          if (swingHand.get())
            mc.player.swingHand(mc.player.getActiveHand());
          if (chatFeedback)
            info("You can't strip logs!");
          return false;
        }
      }
    }
    return true;
  }
}
