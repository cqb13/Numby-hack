package cqb13.NumbyHack.mixins;

import net.minecraft.client.render.entity.TntEntityRenderer;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

import cqb13.NumbyHack.modules.general.TntFuseEsp;
import meteordevelopment.meteorclient.systems.modules.Modules;

@Mixin(TntEntityRenderer.class)
public class TntEntityRendererMixin {
    @ModifyArg(method = "render(Lnet/minecraft/client/render/entity/state/TntEntityRenderState;Lnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/command/OrderedRenderCommandQueue;Lnet/minecraft/client/render/state/CameraRenderState;)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/render/entity/TntMinecartEntityRenderer;renderFlashingBlock(Lnet/minecraft/block/BlockState;Lnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/command/OrderedRenderCommandQueue;IZI)V"), index = 4)
    private boolean numbyhack$disableTntFlash(boolean flashing) {
        TntFuseEsp tntFuseEsp = Modules.get().get(TntFuseEsp.class);
        if (tntFuseEsp != null && tntFuseEsp.shouldHideFlashing()) {
            return false;
        }
        return flashing;
    }
}
