package cqb13.NumbyHack.mixins;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import cqb13.NumbyHack.events.CustomPayloadEvent;
import meteordevelopment.meteorclient.MeteorClient;
import net.minecraft.network.listener.ClientCommonPacketListener;
import net.minecraft.network.packet.s2c.common.CustomPayloadS2CPacket;

@Mixin(CustomPayloadS2CPacket.class)
public class CustomPayloadS2CPacketMixin {
    @Inject(method = "apply(Lnet/minecraft/network/listener/ClientCommonPacketListener;)V", at = @At(value = "HEAD"), cancellable = true)
    private void onApply(ClientCommonPacketListener clientCommonPacketListener, CallbackInfo info) {
        CustomPayloadS2CPacket packet = (CustomPayloadS2CPacket) (Object) this;
        CustomPayloadEvent event = MeteorClient.EVENT_BUS.post(CustomPayloadEvent.get(packet));
        if (event.isCancelled()) {
            info.cancel();
        }
    }
}
