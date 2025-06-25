package cqb13.NumbyHack.events;

import meteordevelopment.meteorclient.events.Cancellable;
import net.minecraft.network.packet.s2c.common.CustomPayloadS2CPacket;

public class CustomPayloadEvent extends Cancellable {
  private static final CustomPayloadEvent INSTANCE = new CustomPayloadEvent();

  public CustomPayloadS2CPacket packet;

  public static CustomPayloadEvent get(CustomPayloadS2CPacket packet) {
    INSTANCE.setCancelled(false);
    INSTANCE.packet = packet;
    return INSTANCE;
  }
}
