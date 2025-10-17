package cqb13.NumbyHack.mixins;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import cqb13.NumbyHack.modules.general.CarpetPlacer;
import meteordevelopment.meteorclient.systems.modules.Modules;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.CarpetBlock;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.network.ClientPlayerInteractionManager;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

@Mixin(ClientPlayerInteractionManager.class)
public class BlockPlacementMixin {

    @Inject(method = "interactBlock", at = @At("HEAD"), cancellable = true)
    private void preventBlockPlacement(ClientPlayerEntity player, Hand hand, BlockHitResult hitResult,
            CallbackInfoReturnable<ActionResult> cir) {
        CarpetPlacer carpetPlacer = Modules.get().get(CarpetPlacer.class);

        if (carpetPlacer == null || !carpetPlacer.isActive() || !carpetPlacer.isAntiStackEnabled()) {
            return;
        }

        World world = player.getEntityWorld();
        BlockPos hitPos = hitResult.getBlockPos();
        ItemStack itemStack = player.getStackInHand(hand);

        if (itemStack.getItem() instanceof BlockItem) {
            BlockPos placePos = hitPos.offset(hitResult.getSide());
            BlockPos belowPlacePos = placePos.down();
            BlockState blockBelow = world.getBlockState(belowPlacePos);

            if (shouldPreventPlacement(itemStack, blockBelow, carpetPlacer)) {
                cir.setReturnValue(ActionResult.FAIL);
            }
        }
    }

    private boolean shouldPreventPlacement(ItemStack itemStack, BlockState blockBelow, CarpetPlacer carpetPlacer) {
        Block itemBlock = ((BlockItem) itemStack.getItem()).getBlock();
        Block belowBlock = blockBelow.getBlock();

        if (itemBlock instanceof CarpetBlock && belowBlock instanceof CarpetBlock) {
            return true;
        }

        return false;
    }
}
