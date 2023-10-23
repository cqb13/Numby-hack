package cqb13.NumbyHack.modules.general;

import cqb13.NumbyHack.NumbyHack;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.settings.*;
import meteordevelopment.meteorclient.systems.friends.Friends;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.systems.modules.Modules;
import meteordevelopment.meteorclient.systems.modules.misc.AutoReconnect;
import meteordevelopment.meteorclient.utils.player.PlayerUtils;
import meteordevelopment.meteorclient.utils.world.Dimension;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.packet.s2c.common.DisconnectS2CPacket;
import net.minecraft.text.Text;
import net.minecraft.client.network.PlayerListEntry;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
/**
 * made by cqb13
 */
public class AutoLogPlus extends Module {
    private final SettingGroup sgTimeLog = settings.createGroup("Time Log");
    private final SettingGroup sgLocationLog = settings.createGroup("Location Log");
    private final SettingGroup sgPingLog = settings.createGroup("Ping Log");
    private final SettingGroup sgGeneral = settings.getDefaultGroup();

    // time log
    private final Setting<Boolean> timeLog = sgTimeLog.add(new BoolSetting.Builder()
            .name("time-log")
            .description("Logs you out after a certain amount of time.")
            .defaultValue(false)
            .build()
    );

    private final Setting<String> logTime = sgTimeLog.add(new StringSetting.Builder()
            .name("time")
            .description("The time to log you out (uses 24 hour time).")
            .defaultValue("12:00")
            .visible(timeLog::get)
            .build()
    );

    // location log
    private final Setting<Boolean> locationLog = sgLocationLog.add(new BoolSetting.Builder()
            .name("location-log")
            .description("Disconnects when a you reach set coordinates.")
            .defaultValue(false)
            .build()
    );

    private final Setting<Boolean> oneAxis = sgLocationLog.add(new BoolSetting.Builder()
            .name("one-axis-log")
            .description("Disconnects when a you reach set coordinates on a specific axis.")
            .defaultValue(false)
            .visible(locationLog::get)
            .build()
    );

    private final Setting<axisOptions> selectAxis = sgLocationLog.add(new EnumSetting.Builder<axisOptions>()
            .name("select-axis")
            .description("The axis with the exact log coords.")
            .defaultValue(axisOptions.X)
            .visible(oneAxis::get)
            .build()
    );

    private final Setting<Dimension> dimension = sgLocationLog.add(new EnumSetting.Builder<Dimension>()
            .name("dimension")
            .description("Dimension for the coords.")
            .defaultValue(Dimension.Nether)
            .visible(locationLog::get)
            .build());

    private final Setting<Integer> xCoords = sgLocationLog.add(new IntSetting.Builder()
            .name("x-coords")
            .description("The X coords it should log you out.")
            .defaultValue(0)
            .range(-2147483648, 2147483647)
            .sliderRange(-2147483648, 2147483647)
            .visible(locationLog::get)
            .build());

    private final Setting<Integer> zCoords = sgLocationLog.add(new IntSetting.Builder()
            .name("z-coords")
            .description("The Z coords it should log you out.")
            .defaultValue(-1000)
            .range(-2147483648, 2147483647)
            .sliderRange(-2147483648, 2147483647)
            .visible(locationLog::get)
            .build());

    private final Setting<Integer> radius = sgLocationLog.add(new IntSetting.Builder()
            .name("radius")
            .description("The radius of coords from the exact coords it will log you out.")
            .defaultValue(64)
            .min(0)
            .sliderRange(0, 256)
            .visible(locationLog::get)
            .build());

    // ping log
    private final Setting<Boolean> pingLog = sgPingLog.add(new BoolSetting.Builder()
            .name("ping-log")
            .description("Disconnects when your ping is above a certain value.")
            .defaultValue(false)
            .build()
    );

    private final Setting<Integer> pingValue = sgPingLog.add(new IntSetting.Builder()
            .name("ping-value")
            .defaultValue(1000)
            .range(0, 10000)
            .sliderRange(0, 10000)
            .visible(pingLog::get)
            .build()
    );

    // normal log
    private final Setting<Boolean> onlyTrusted = sgGeneral.add(new BoolSetting.Builder()
            .name("enemy")
            .description("Disconnects when a player not on your friends list appears in render distance.")
            .defaultValue(false)
            .build()
    );

    private final Setting<Boolean> toggleAutoReconnect = sgGeneral.add(new BoolSetting.Builder()
            .name("toggle-auto-reconnect")
            .description("Turns off auto reconnect when disconnecting.")
            .defaultValue(true)
            .build()
    );

    private final Setting<Boolean> toggleOff = sgGeneral.add(new BoolSetting.Builder()
            .name("toggle-off")
            .description("Disables Time Log after usage.")
            .defaultValue(true)
            .build()
    );

    public AutoLogPlus() {
        super(NumbyHack.CATEGORY, "auto-log+", "Disconnects you when a specific condition is reached.");
    }

    @EventHandler
    private void onTick(TickEvent.Post event) {
        if (mc == null || mc.world == null || mc.player == null) return;

        playerLog();
        disconnectOnHighPing();
        timeLog();
        locationLog();
    }

    // bad player log
    private void playerLog() {
        for (Entity entity : mc.world.getEntities()) {
            if (entity instanceof PlayerEntity && entity.getUuid() != mc.player.getUuid()) {
                if (onlyTrusted.get() && entity != mc.player && !Friends.get().isFriend((PlayerEntity) entity)) {
                    disconnect(Text.of("[Auto Log+] A non trusted player ["+ entity.getEntityName() +"] has entered your render distance."));
                }
            }

        }
    }

    // time log
    private void timeLog() {
        if (timeLog.get()) {
            DateTimeFormatter dtf = DateTimeFormatter.ofPattern("HH:mm");
            LocalDateTime now = LocalDateTime.now();
            if (dtf.format(now).equals(logTime.get())) {
                disconnect(Text.of("[Auto Log+] Log time has been reached " + logTime.get() + "."));
            }
        }
    }

    // location log
    private void locationLog() {
        if (locationLog.get() && PlayerUtils.getDimension() == dimension.get()) {
            if (xCoordsMatch() && zCoordsMatch()) {
                disconnect(Text.of("[Auto Log+] You have reached your destination."));
            } else if (oneAxis.get()) {
                if (selectAxis.get() == axisOptions.X && xCoordsMatch()) {
                    disconnect(Text.of("[Auto Log+] You have reached your destination."));
                } else if (selectAxis.get() == axisOptions.Z && zCoordsMatch()) {
                    disconnect(Text.of("[Auto Log+] You have reached your destination."));
                }
            }
        }
    }

    // ping log
    private void disconnectOnHighPing() {
        if (!pingLog.get()) return;
        if (mc.getNetworkHandler() == null || mc.player == null) return;
        PlayerListEntry playerListEntry = mc.getNetworkHandler().getPlayerListEntry(mc.player.getUuid());

        int ping = playerListEntry.getLatency();

        if (ping >= pingValue.get()) disconnect(Text.of("[Auto Log+] High ping [" + ping + "]"));
    }

    private boolean xCoordsMatch() {
        return (mc.player.getX() <= xCoords.get() + radius.get() && mc.player.getX() >= xCoords.get() - radius.get());
    }

    private boolean zCoordsMatch() {
        return (mc.player.getZ() <= zCoords.get() + radius.get() && mc.player.getZ() >= zCoords.get() - radius.get());
    }

    private void disconnect(Text text){
        if (mc.getNetworkHandler() == null) return;
        mc.player.networkHandler.onDisconnect(new DisconnectS2CPacket(text));

        if (toggleOff.get()) toggle();

        if (toggleAutoReconnect.get() && Modules.get().isActive(AutoReconnect.class))
            Modules.get().get(AutoReconnect.class).toggle();
    }

    public enum axisOptions {
        X,
        Z,
    }
}