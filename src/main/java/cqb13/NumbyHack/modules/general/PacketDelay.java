package cqb13.NumbyHack.modules.general;

import java.util.ArrayList;
import java.util.Set;

import cqb13.NumbyHack.NumbyHack;
import meteordevelopment.meteorclient.events.packets.PacketEvent;
import meteordevelopment.meteorclient.settings.PacketListSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.orbit.EventHandler;
import meteordevelopment.orbit.EventPriority;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.PacketType;
import org.jetbrains.annotations.NotNull;

/**
 * made by cqb13
 */
public class PacketDelay extends Module {
    private final SettingGroup sgGeneral = settings.getDefaultGroup();

    private final Setting<Set<PacketType<? extends @NotNull Packet<?>>>> c2sPackets = sgGeneral.add(new PacketListSetting.Builder()
            .name("C2S-packets")
            .description("Client-to-server packets to delay.")
            .serverbound()
            .build());

    private static ArrayList<Packet<?>> delayedPackets = new ArrayList<>();

    public PacketDelay() {
        super(NumbyHack.CATEGORY, "packet-delay", "Allows you to delay the packets you send to a server.");
        runInMainMenu = true;
    }

    @EventHandler(priority = EventPriority.HIGHEST + 1)
    private void onSendPacket(PacketEvent.Send event) {
        if (c2sPackets.get().contains(event.packet.type())) {
            delayedPackets.add(event.packet);
            event.cancel();
        }
    }

    @Override
    public void onDeactivate() {
        for (Packet<?> packet : delayedPackets) {
            mc.getConnection().send(packet);
        }
        delayedPackets.clear();
    }
}
