package cqb13.NumbyHack.modules.general;

import cqb13.NumbyHack.NumbyHack;
import meteordevelopment.meteorclient.events.render.Render3DEvent;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.renderer.ShapeMode;
import meteordevelopment.meteorclient.settings.ColorSetting;
import meteordevelopment.meteorclient.settings.EnumSetting;
import meteordevelopment.meteorclient.settings.IntSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.utils.Utils;
import meteordevelopment.meteorclient.utils.player.FindItemResult;
import meteordevelopment.meteorclient.utils.player.InvUtils;
import meteordevelopment.meteorclient.utils.player.Rotations;
import meteordevelopment.meteorclient.utils.render.color.SettingColor;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.protocol.game.ServerboundUseItemOnPacket;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.SwingAnimation;
import net.minecraft.world.level.block.AzaleaBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.CocoaBlock;
import net.minecraft.world.level.block.CropBlock;
import net.minecraft.world.level.block.MushroomBlock;
import net.minecraft.world.level.block.SaplingBlock;
import net.minecraft.world.level.block.StemBlock;
import net.minecraft.world.level.block.SweetBerryBushBlock;
import net.minecraft.world.phys.BlockHitResult;

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
        super(NumbyHack.CATEGORY, "bonemeal-aura", "Automatically bonemeal crops around the player.");
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
            mc.player.connection.send(new ServerboundUseItemOnPacket(InteractionHand.MAIN_HAND,
                    new BlockHitResult(Utils.vec3(crop), Direction.UP, crop, false), 0));
            mc.player.swing(InteractionHand.MAIN_HAND, SwingAnimation.DEFAULT, false);
        });
    }

    private BlockPos getCrop() {
        for (int x = -horizontalRange.get(); x < horizontalRange.get(); x++) {
            for (int y = -verticalRange.get(); y < verticalRange.get(); y++) {
                for (int z = -horizontalRange.get(); z < horizontalRange.get(); z++) {
                    BlockPos blockPos = mc.player.blockPosition().offset(x, y, z);
                    Block block = mc.level.getBlockState(blockPos).getBlock();
                    if (block instanceof CropBlock cropBlock) {
                        int age = cropBlock.getAge(mc.level.getBlockState(blockPos));
                        if (age < cropBlock.getMaxAge())
                            return blockPos;
                    }
                    if (block instanceof CocoaBlock) {
                        int age = mc.level.getBlockState(blockPos).getValue(CocoaBlock.AGE);
                        if (age < 2)
                            return blockPos;
                    }
                    if (block instanceof StemBlock) {
                        int age = mc.level.getBlockState(blockPos).getValue(StemBlock.AGE);
                        if (age < StemBlock.MAX_AGE)
                            return blockPos;
                    }
                    if (block instanceof MushroomBlock) {
                        return blockPos;
                    }
                    if (block instanceof SweetBerryBushBlock) {
                        int age = mc.level.getBlockState(blockPos).getValue(SweetBerryBushBlock.AGE);
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
