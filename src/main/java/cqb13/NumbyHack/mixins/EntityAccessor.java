package cqb13.NumbyHack.mixins;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

import net.minecraft.entity.Entity;

@Mixin(Entity.class)
public interface EntityAccessor {
    @Invoker("setFlag")
    void invokeSetFlag(int index, boolean value);
}
