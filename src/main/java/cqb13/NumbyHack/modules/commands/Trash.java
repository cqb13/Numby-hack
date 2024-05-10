package cqb13.NumbyHack.modules.commands;

import com.google.common.collect.Lists;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import meteordevelopment.meteorclient.commands.Command;
import net.minecraft.command.CommandSource;
import net.minecraft.item.ItemStack;
import net.minecraft.network.packet.c2s.play.ClickSlotC2SPacket;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.slot.Slot;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.util.collection.DefaultedList;

import java.util.List;

public class Trash extends Command {
    public Trash() {
        super("trashcan", "Allows your to silently destroy any item in your hand.", "trash");
    }

    @Override
    public void build(LiteralArgumentBuilder<CommandSource> builder) {
        builder.executes(context -> {
            int id = 36 + mc.player.getInventory().selectedSlot;
            int button = 50;

            ScreenHandler handler = mc.player.currentScreenHandler;

            DefaultedList<Slot> slots = handler.slots;
            int i = slots.size();
            List<ItemStack> list = Lists.newArrayListWithCapacity(i);

            for (Slot slot : slots) list.add(slot.getStack().copy());

            handler.onSlotClick(id, 50, SlotActionType.SWAP, mc.player);
            Int2ObjectMap<ItemStack> stacks = new Int2ObjectOpenHashMap();

            for (int slot = 0; slot < i; slot++) {
                ItemStack stack1 = list.get(slot);
                ItemStack stack2 = slots.get(slot).getStack();

                if (!ItemStack.areEqual(stack1, stack2)) stacks.put(slot, stack2.copy());
            }

            mc.getNetworkHandler().sendPacket(new ClickSlotC2SPacket(0, handler.getRevision(), id, button, SlotActionType.SWAP, handler.getCursorStack().copy(), stacks));

            return SINGLE_SUCCESS;
        });
    }
}
