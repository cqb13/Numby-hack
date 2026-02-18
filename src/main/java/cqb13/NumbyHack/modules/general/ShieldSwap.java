package cqb13.NumbyHack.modules.general;

import cqb13.NumbyHack.NumbyHack;
import meteordevelopment.meteorclient.settings.BoolSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.utils.player.InvUtils;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.network.packet.c2s.play.CloseHandledScreenC2SPacket;

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
        ItemStack currentItem = mc.player.getOffHandStack();

        if (currentItem.getItem() == Items.SHIELD) {
            return;
        }

        var inventory = mc.player.getInventory().getMainStacks();

        for (int i = 0; i < inventory.size(); i++) {
            Item item = inventory.get(i).getItem();

            if (item == Items.SHIELD) {
                originalSlot = i;
                break;
            }
        }

        InvUtils.move().from(originalSlot).toOffhand();

        if (closeInventory.get()) {
            mc.getNetworkHandler().sendPacket(new CloseHandledScreenC2SPacket(0));
        }
    }

    public void swapBack() {
        InvUtils.move().fromOffhand().to(originalSlot);

        if (closeInventory.get()) {
            mc.getNetworkHandler().sendPacket(new CloseHandledScreenC2SPacket(0));
        }
    }
}
