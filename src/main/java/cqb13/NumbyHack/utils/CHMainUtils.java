package cqb13.NumbyHack.utils;

import meteordevelopment.meteorclient.events.packets.PacketEvent;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.network.packet.s2c.play.EntityStatusS2CPacket;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.entry.RegistryEntry;

import static meteordevelopment.meteorclient.MeteorClient.mc;

public class CHMainUtils {
  public static Integer lavaIsWithinRange(int range) {
    for (int i = 0; i < range; i++) {
      if (mc.world.getBlockState(mc.player.getBlockPos().down(i)).getBlock().getTranslationKey().contains("lava")) {
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
    if (event.packet instanceof EntityStatusS2CPacket packet) {
      if (packet.getStatus() == 3) {
        deadEntity = packet.getEntity(mc.world);
        return deadEntity instanceof PlayerEntity;
      }
    }
    return false;
  }

  public static int getEnchantmentLevel(ItemStack stack, RegistryKey<Enchantment> enchantment) {
    for (RegistryEntry<Enchantment> enchantments : stack.getEnchantments().getEnchantments()) {
      if (enchantments.toString().contains(enchantment.getValue().toString())) {
        return stack.getEnchantments().getLevel(enchantments);
      }
    }
    return 0;
  }
}
