package cqb13.NumbyHack.mixins;

import cqb13.NumbyHack.modules.general.GameSettings;
import meteordevelopment.meteorclient.systems.modules.Modules;
import net.minecraft.client.gui.screen.DeathScreen;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/*
* inspired by:
* https://github.com/Ecorous/NoMoreScore/blob/main/src/main/java/com/frogastudios/nomorescore/mixin/MixinDeathScreen.java
* */
@Mixin(DeathScreen.class)
public abstract class DeathScreenMixin {
    @Shadow
    private Text scoreText;

    @Inject(method = "init", at = @At("TAIL"))
    private void removeScore(CallbackInfo ci) {
        if (Modules.get().get(GameSettings.class).hideTheScore())
            scoreText = Text.empty();
    }
}
