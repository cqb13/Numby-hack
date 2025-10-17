package cqb13.NumbyHack.mixins.meteor;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import meteordevelopment.meteorclient.systems.modules.combat.CrystalAura;
import net.minecraft.entity.LivingEntity;

@Mixin(CrystalAura.class)
public interface CrystalAuraAccessor {
    @Accessor("bestTarget")
    LivingEntity bestTarget();

    @Accessor(value = "bestTargetDamage", remap = false)
    double bestTargetDamage();
}
