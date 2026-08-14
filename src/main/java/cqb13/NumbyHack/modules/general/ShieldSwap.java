package cqb13.NumbyHack.modules.general;

import cqb13.NumbyHack.NumbyHack;
import meteordevelopment.meteorclient.settings.BoolSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.utils.player.InvUtils;
import net.minecraft.network.protocol.game.ServerboundContainerClosePacket;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

/**
 * made by cqb13
 */
public class ShieldSwap extends Module {
    private final SettingGroup sgGeneral = settings.getDefaultGroup();

    private final Setting<Boolean> swapBack = sgGeneral.add(new BoolSetting.Builder()
            .name("swap-back")
            .description("Prevents you from walking into fire.")
            .defaultValue(true)
            .build());

    private final Setting<Boolean> closeInventory = sgGeneral.add(new BoolSetting.Builder()
            .name("close-inventory")
            .description("Sends inventory close after swap.")
            .defaultValue(true)
            .build());

    private int originalSlot;
    private boolean found;

    public ShieldSwap() {
        super(NumbyHack.CATEGORY, "shield-swap", "Automatically swaps a shield into your offhand.");
    }

    @Override
    public void onActivate() {
        equipShield();
        if (!swapBack.get()) {
            toggle();
        }
    }

    @Override
    public void onDeactivate() {
        if (!swapBack.get()) {
            return;
        }

        swapBack();
    }

    private void equipShield() {
        ItemStack currentItem = mc.player.getOffhandItem();

        if (currentItem.getItem() == Items.SHIELD) {
            return;
        }

        var inventory = mc.player.getInventory().getNonEquipmentItems();

        for (int i = 0; i < inventory.size(); i++) {
            Item item = inventory.get(i).getItem();

            if (item == Items.SHIELD) {
                originalSlot = i;
                found = true;
                break;
            }
        }

        if (!found) {
            return;
        }

        InvUtils.move().from(originalSlot).toOffhand();

        if (closeInventory.get()) {
            mc.getConnection().send(new ServerboundContainerClosePacket(0));
        }
    }

    public void swapBack() {
        if (!found) {
            return;
        }

        InvUtils.move().fromOffhand().to(originalSlot);

        if (closeInventory.get()) {
            mc.getConnection().send(new ServerboundContainerClosePacket(0));
        }
    }
}
