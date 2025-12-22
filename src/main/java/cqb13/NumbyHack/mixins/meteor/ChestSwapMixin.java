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
import meteordevelopment.meteorclient.systems.modules.player.ChestSwap.Chestplate;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;

@Mixin(value = ChestSwap.class, remap = false)
public abstract class ChestSwapMixin extends Module {
    private int numby$bestElytraScore = 5;
    private SettingGroup numby$sgCustom;
    private Setting<Boolean> preferEnchanted;

    public ChestSwapMixin(Category category, String name, String description) {
        super(category, name, description);
    }

    @Shadow
    private void equip(int slot) {
    }

    @Shadow
    private Setting<Chestplate> chestplate;

    @Inject(method = "<init>", at = @At("TAIL"))
    private void constructor(CallbackInfo ci) {
        numby$sgCustom = this.settings.createGroup("Customize");
        preferEnchanted = numby$sgCustom.add(new BoolSetting.Builder()
                .name("perfer-enchanted")
                .description("Prefers enchanted equipment when swapping.")
                .defaultValue(false)
                .build());
    }

    /**
     * @author cqb13
     * @reason Replace default equipChestplate logic have logic to prefer enchanted
     *         equipment
     */
    @Overwrite
    private boolean equipChestplate() {
        assert mc.player != null;
        int bestSlot = -1;
        int bestScore = -1;
        boolean foundPreferred = false;

        for (int i = 0; i < mc.player.getInventory().getMainStacks().size(); i++) {
            ItemStack itemStack = mc.player.getInventory().getMainStacks().get(i);
            Item item = itemStack.getItem();

            if (!(item == Items.DIAMOND_CHESTPLATE || item == Items.NETHERITE_CHESTPLATE)) {
                continue;
            }

            if (!preferEnchanted.get()) {
                switch (chestplate.get()) {
                    case Diamond:
                        if (item == Items.DIAMOND_CHESTPLATE) {
                            equip(i);
                            return true;
                        }
                        break;
                    case Netherite:
                        if (item == Items.NETHERITE_CHESTPLATE) {
                            equip(i);
                            return true;
                        }
                        break;
                    case PreferDiamond:
                        if (item == Items.DIAMOND_CHESTPLATE) {
                            equip(i);
                            return true;
                        } else if (bestSlot == -1) {
                            bestSlot = i;
                        }
                        break;
                    case PreferNetherite:
                        if (item == Items.NETHERITE_CHESTPLATE) {
                            equip(i);
                            return true;
                        } else if (bestSlot == -1) {
                            bestSlot = i;
                        }
                        break;
                }
                continue;
            }

            boolean isPreferred = (chestplate.get() == Chestplate.Diamond && item == Items.DIAMOND_CHESTPLATE) ||
                    (chestplate.get() == Chestplate.Netherite && item == Items.NETHERITE_CHESTPLATE) ||
                    (chestplate.get() == Chestplate.PreferDiamond && item == Items.DIAMOND_CHESTPLATE) ||
                    (chestplate.get() == Chestplate.PreferNetherite && item == Items.NETHERITE_CHESTPLATE);

            int score = rateArmorEnchantments(itemStack);

            if (isPreferred) {
                // If there is a preferred chestplate ignore all not preferred chesplates
                if (!foundPreferred || score > bestScore) {
                    bestScore = score;
                    bestSlot = i;
                    foundPreferred = true;
                }
            } else if (!foundPreferred) {
                if (score > bestScore) {
                    bestScore = score;
                    bestSlot = i;
                }
            }
        }

        if (bestSlot != -1) {
            equip(bestSlot);
        }
        return bestSlot != -1;
    }

    // 2 points for mending, 1 points for each level of unbreaking and any
    // protection enchant, -2 points for curse of binding
    private int rateArmorEnchantments(ItemStack item) {
        int score = 0;

        if (CHMainUtils.getEnchantmentLevel(item, Enchantments.MENDING) > 0) {
            score += 2;
        }

        score += CHMainUtils.getEnchantmentLevel(item, Enchantments.UNBREAKING);

        int prot = CHMainUtils.getEnchantmentLevel(item, Enchantments.PROTECTION);

        if (prot == 0) {
            prot = CHMainUtils.getEnchantmentLevel(item, Enchantments.BLAST_PROTECTION);
        }

        if (prot == 0) {
            prot = CHMainUtils.getEnchantmentLevel(item, Enchantments.FIRE_PROTECTION);
        }

        if (prot == 0) {
            prot = CHMainUtils.getEnchantmentLevel(item, Enchantments.PROJECTILE_PROTECTION);
        }

        if (CHMainUtils.getEnchantmentLevel(item, Enchantments.BINDING_CURSE) > 0) {
            score -= 2;
        }

        score += prot;

        return score;
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

            if (!preferEnchanted.get()) {
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
    // -2 points for curse of binding
    private int rateElytraEnchantments(ItemStack stack) {
        int score = 0;

        if (CHMainUtils.getEnchantmentLevel(stack, Enchantments.MENDING) > 0) {
            score += 2;
        }

        score += CHMainUtils.getEnchantmentLevel(stack, Enchantments.UNBREAKING);

        if (CHMainUtils.getEnchantmentLevel(stack, Enchantments.BINDING_CURSE) > 0) {
            score -= 2;
        }

        return score;
    }
}
