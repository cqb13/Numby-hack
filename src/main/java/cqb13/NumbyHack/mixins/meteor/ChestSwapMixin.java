package cqb13.NumbyHack.mixins.meteor;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import cqb13.NumbyHack.utils.CHMainUtils;
import meteordevelopment.meteorclient.settings.BoolSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.systems.modules.Category;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.systems.modules.player.ChestSwap;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.item.ItemStack;

@Mixin(value = ChestSwap.class, remap = false)
public abstract class ChestSwapMixin extends Module {
    private int numby$bestElytraScore = 5;
    private SettingGroup numby$sgCustom;
    private Setting<Boolean> numby$preferEnchantedElytra;

    public ChestSwapMixin(Category category, String name, String description) {
        super(category, name, description);
    }

    @Shadow
    private void equip(int slot) {
    }

    @Inject(method = "<init>", at = @At("TAIL"))
    private void constructor(CallbackInfo ci) {
        numby$sgCustom = this.settings.createGroup("Customize");
        numby$preferEnchantedElytra = numby$sgCustom.add(new BoolSetting.Builder()
                .name("perfer-enchanted-elytra")
                .description("Prefers enchanted elytra.")
                .defaultValue(false)
                .build());
    }

    /**
     * @author cqb13
     * @reason Replace default equipElytra logic to prefer enchanted Elytras.
     */
    @Overwrite
    private void equipElytra() {
        assert mc.player != null;
        int bestSlot = -1;
        int bestScore = -1;

        for (int i = 0; i < mc.player.getInventory().getMainStacks().size(); i++) {
            ItemStack item = mc.player.getInventory().getMainStacks().get(i);

            if (!item.contains(DataComponentTypes.GLIDER)) {
                continue;
            }

            if (!numby$preferEnchantedElytra.get()) {
                bestSlot = i;
                break;
            }

            int score = rateElytraEnchantments(item);

            if (score == numby$bestElytraScore) {
                bestSlot = i;
                break;
            }

            if (score >= bestScore) {
                bestSlot = i;
                bestScore = score;
            }
        }

        if (bestSlot != -1) {
            equip(bestSlot);
        }
    }

    // 2 points for mending, 1 point for each level of unbreaking
    private int rateElytraEnchantments(ItemStack stack) {
        int score = 0;

        if (CHMainUtils.getEnchantmentLevel(stack, Enchantments.MENDING) > 0) {
            score += 2;
        }

        score += CHMainUtils.getEnchantmentLevel(stack, Enchantments.UNBREAKING);

        return score;
    }
}
