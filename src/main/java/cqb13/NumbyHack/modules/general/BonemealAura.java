package cqb13.NumbyHack.modules.general;

import cqb13.NumbyHack.NumbyHack;
import meteordevelopment.meteorclient.events.render.Render3DEvent;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.renderer.ShapeMode;
import meteordevelopment.meteorclient.settings.*;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.utils.Utils;
import meteordevelopment.meteorclient.utils.player.FindItemResult;
import meteordevelopment.meteorclient.utils.player.InvUtils;
import meteordevelopment.meteorclient.utils.player.Rotations;
import meteordevelopment.meteorclient.utils.render.color.SettingColor;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.block.AzaleaBlock;
import net.minecraft.block.Block;
import net.minecraft.block.CocoaBlock;
import net.minecraft.block.CropBlock;
import net.minecraft.block.MushroomPlantBlock;
import net.minecraft.block.SaplingBlock;
import net.minecraft.block.StemBlock;
import net.minecraft.block.SweetBerryBushBlock;
import net.minecraft.item.Items;
import net.minecraft.network.packet.c2s.play.PlayerInteractBlockC2SPacket;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;

public class BonemealAura extends Module {
  private final SettingGroup sgGeneral = settings.getDefaultGroup();
  private final SettingGroup sgRender = settings.createGroup("Render");

  private final Setting<Integer> horizontalRange = sgGeneral.add(new IntSetting.Builder()
      .name("horizontal-range")
      .description("How far around the player to bonemeal.")
      .defaultValue(4)
      .min(1)
      .sliderRange(1, 6)
      .build());

  private final Setting<Integer> verticalRange = sgGeneral.add(new IntSetting.Builder()
      .name("vertical-range")
      .description("How high above the player to bonemeal.")
      .defaultValue(2)
      .min(1)
      .sliderRange(1, 6)
      .build());

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
      .defaultValue(new SettingColor(146, 188, 98, 190))
      .build());

  public BonemealAura() {
    super(NumbyHack.CATEGORY, "bonemeal-aura", "Automatically bonemeal crops around the player");
  }

  public boolean isBonemealing;

  @EventHandler
  private void onTick(TickEvent.Pre event) {
    BlockPos crop = getCrop();
    if (crop == null) {
      isBonemealing = false;
      return;
    }

    FindItemResult bonemeal = InvUtils.findInHotbar(Items.BONE_MEAL);
    if (!bonemeal.found()) {
      isBonemealing = false;
      return;
    }

    isBonemealing = true;
    Rotations.rotate(Rotations.getYaw(crop), Rotations.getPitch(crop), () -> {
      InvUtils.swap(bonemeal.slot(), false);
      mc.player.networkHandler.sendPacket(new PlayerInteractBlockC2SPacket(Hand.MAIN_HAND,
          new BlockHitResult(Utils.vec3d(crop), Direction.UP, crop, false), 0));
      mc.player.swingHand(Hand.MAIN_HAND);
    });
  }

  private BlockPos getCrop() {
    for (int x = -horizontalRange.get(); x < horizontalRange.get(); x++) {
      for (int y = -verticalRange.get(); y < verticalRange.get(); y++) {
        for (int z = -horizontalRange.get(); z < horizontalRange.get(); z++) {
          BlockPos blockPos = mc.player.getBlockPos().add(x, y, z);
          Block block = mc.world.getBlockState(blockPos).getBlock();
          if (block instanceof CropBlock cropBlock) {
            int age = cropBlock.getAge(mc.world.getBlockState(blockPos));
            if (age < cropBlock.getMaxAge())
              return blockPos;
          }
          if (block instanceof CocoaBlock) {
            int age = mc.world.getBlockState(blockPos).get(CocoaBlock.AGE);
            if (age < 2)
              return blockPos;
          }
          if (block instanceof StemBlock) {
            int age = mc.world.getBlockState(blockPos).get(StemBlock.AGE);
            if (age < StemBlock.MAX_AGE)
              return blockPos;
          }
          if (block instanceof MushroomPlantBlock) {
            return blockPos;
          }
          if (block instanceof SweetBerryBushBlock) {
            int age = mc.world.getBlockState(blockPos).get(SweetBerryBushBlock.AGE);
            if (age < 3)
              return blockPos;
          }
          if (block instanceof SaplingBlock || block instanceof AzaleaBlock) {
            return blockPos;
          }
        }
      }
    }
    return null;
  }

  @EventHandler
  private void onRender(Render3DEvent event) {
    BlockPos crop = getCrop();
    if (crop == null || !InvUtils.findInHotbar(Items.BONE_MEAL).found())
      return;
    event.renderer.box(crop, sideColor.get(), lineColor.get(), shapeMode.get(), 0);
  }
}
