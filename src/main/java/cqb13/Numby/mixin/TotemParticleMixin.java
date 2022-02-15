package cqb13.Numby.mixin;

import meteordevelopment.meteorclient.systems.modules.Modules;
import net.minecraft.client.particle.AnimatedParticle;
import net.minecraft.client.particle.SpriteProvider;
import net.minecraft.client.particle.TotemParticle;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.util.math.Vec3d;
import cqb13.Numby.modules.Confetti;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(TotemParticle.class)
public abstract class TotemParticleMixin extends AnimatedParticle {

    protected TotemParticleMixin(ClientWorld world, double x, double y, double z, SpriteProvider spriteProvider, float upwardsAcceleration) {
        super(world, x, y, z, spriteProvider, upwardsAcceleration);
    }

    @Inject(method = "<init>", at = @At("TAIL"))
    private void onConfettiConstructor(ClientWorld world, double x, double y, double z, double velocityX, double velocityY, double velocityZ, SpriteProvider spriteProvider, CallbackInfo ci) {
        Confetti confetti = Modules.get().get(Confetti.class);
        TotemParticle totemParticle = ((TotemParticle)(Object) this);
        if(confetti.isActive()) {
            Vec3d colorOne = confetti.getColorOne();
            Vec3d colorTwo = confetti.getColorTwo();
            if (this.random.nextInt(4) == 0) {
                totemParticle.setColor((float) colorOne.x, (float) colorOne.y, (float) colorOne.z);
            } else {
                totemParticle.setColor((float) colorTwo.x, (float) colorTwo.y, (float) colorTwo.z);
            }
        }
    }

}
