package cqb13.NumbyHack.mixins.meteor;

import meteordevelopment.meteorclient.systems.modules.combat.CrystalAura;
import net.minecraft.entity.player.PlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(CrystalAura.class)
public interface CrystalAuraAccessor {
    @Accessor("bestTarget")
    PlayerEntity bestTarget();

    @Accessor(value = "bestTargetDamage", remap = false)
    double bestTargetDamage();
}
