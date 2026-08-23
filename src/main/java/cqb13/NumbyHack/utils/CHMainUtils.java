package cqb13.NumbyHack.utils;

import static meteordevelopment.meteorclient.MeteorClient.mc;

import meteordevelopment.meteorclient.events.packets.PacketEvent;
import net.minecraft.core.Holder;
import net.minecraft.network.protocol.game.ClientboundEntityEventPacket;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;

public class CHMainUtils {
    public static Integer lavaIsWithinRange(int range) {
        for (int i = 0; i < range; i++) {
            if (mc.level.getBlockState(mc.player.blockPosition().below(i)).getBlock().getDescriptionId()
                    .contains("lava")) {
                return i;
            }
        }
        return -1;
    }

    public static String apply(String msg) {
        if (msg.contains(":smile:"))
            msg = msg.replace(":smile:", "☺");
        if (msg.contains(":sad:"))
            msg = msg.replace(":sad:", "☹");
        if (msg.contains(":heart:"))
            msg = msg.replace(":heart:", "❤");
        if (msg.contains(":skull:"))
            msg = msg.replace(":skull:", "☠");
        if (msg.contains(":star:"))
            msg = msg.replace(":star:", "★");
        if (msg.contains(":flower:"))
            msg = msg.replace(":flower:", "❀");
        if (msg.contains(":pick:"))
            msg = msg.replace(":pick:", "⛏");
        if (msg.contains(":wheelchair:"))
            msg = msg.replace(":wheelchair:", "♿");
        if (msg.contains(":lightning:"))
            msg = msg.replace(":lightning:", "⚡");
        if (msg.contains(":rod:"))
            msg = msg.replace(":rod:", "🎣");
        if (msg.contains(":potion:"))
            msg = msg.replace(":potion:", "🧪");
        if (msg.contains(":fire:"))
            msg = msg.replace(":fire:", "🔥");
        if (msg.contains(":shears:"))
            msg = msg.replace(":shears:", "✂");
        if (msg.contains(":bell:"))
            msg = msg.replace(":bell:", "🔔");
        if (msg.contains(":bow:"))
            msg = msg.replace(":bow:", "🏹");
        if (msg.contains(":trident:"))
            msg = msg.replace(":trident:", "🔱");
        if (msg.contains(":cloud:"))
            msg = msg.replace(":cloud:", "☁");
        if (msg.contains(":cat:"))
            msg = msg.replace(":cat:", "ᓚᘏᗢ");

        return msg;
    }

    public static Entity deadEntity;

    public static boolean isDeathPacket(PacketEvent.Receive event) {
        if (event.packet instanceof ClientboundEntityEventPacket packet && mc.level != null) {
            if (packet.getEventId() == 3) {
                deadEntity = packet.getEntity(mc.level);
                return deadEntity instanceof Player;
            }
        }
        return false;
    }

    public static int getEnchantmentLevel(ItemStack stack, ResourceKey<Enchantment> enchantment) {
        for (Holder<Enchantment> enchantments : stack.getEnchantments().keySet()) {
            if (enchantments.toString().contains(enchantment.identifier().toString())) {
                return stack.getEnchantments().getLevel(enchantments);
            }
        }
        return 0;
    }
}
