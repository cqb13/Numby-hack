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
import net.minecraft.network.packet.s2c.play.DisconnectS2CPacket;
import net.minecraft.text.Text;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
/**
 * made by cqb13
 */
public class AutoLogPlus extends Module {
    private final SettingGroup sgTimeLog = settings.createGroup("Time Log");
    private final SettingGroup sgLocationLog = settings.createGroup("Location Log");
    private final SettingGroup sgGeneral = settings.getDefaultGroup();

    // time log
    private final Setting<String> logTime = sgTimeLog.add(new StringSetting.Builder()
            .name("time")
            .description("The time to log you out (uses 24 hour time).")
            .defaultValue("12:00")
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
        super(NumbyHack.CATEGORY, "auto-log+", "Automatically disconnects you at a certain time.");
    }

    @EventHandler
    private void onTick(TickEvent.Post event) {
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("HH:mm");
        LocalDateTime now = LocalDateTime.now();

        // health log
        if (mc.player.getHealth() <= 0) {
            this.toggle();
            return;
        }

        // bad player log
        for (Entity entity : mc.world.getEntities()) {
            if (entity instanceof PlayerEntity && entity.getUuid() != mc.player.getUuid()) {
                if (onlyTrusted.get() && entity != mc.player && !Friends.get().isFriend((PlayerEntity) entity)) {
                    mc.player.networkHandler.onDisconnect(new DisconnectS2CPacket(Text.literal("[Auto Log+] A non-trusted player appeared in your render distance.")));
                    if (toggleOff.get()) this.toggle();
                    break;
                }
            }

        }

        // time log
        if (dtf.format(now).equals(logTime.get())) {
            mc.player.networkHandler.onDisconnect(new DisconnectS2CPacket(Text.literal("[Auto Log+] log time has been reached " + logTime.get() + ".")));
            if (toggleAutoReconnect.get() && Modules.get().isActive(AutoReconnect.class)) Modules.get().get(AutoReconnect.class).toggle();
            if (toggleOff.get()) toggle();
        }

        // location log
        if (locationLog.get() && PlayerUtils.getDimension() == dimension.get()){
            if (xCoordsMatch() && zCoordsMatch()) {
                locationLogOff();
            } else if (oneAxis.get()) {
                if (selectAxis.get() == axisOptions.X && xCoordsMatch()){
                    locationLogOff();
                } else if (selectAxis.get() == axisOptions.Z && zCoordsMatch()){
                    locationLogOff();
                }
            }
        }
    }

    public void locationLogOff(){
        if (toggleAutoReconnect.get() && Modules.get().isActive(AutoReconnect.class)) Modules.get().get(AutoReconnect.class).toggle();
        if (toggleOff.get()) toggle();

        assert mc.player != null;
        mc.player.networkHandler.onDisconnect(new DisconnectS2CPacket(Text.literal("[Auto Log+] Arrived at destination.")));
    }

    private boolean xCoordsMatch() {
        return (mc.player.getX() <= xCoords.get() + radius.get() && mc.player.getX() >= xCoords.get() - radius.get());
    }

    private boolean zCoordsMatch() {
        return (mc.player.getZ() <= zCoords.get() + radius.get() && mc.player.getZ() >= zCoords.get() - radius.get());
    }
    public enum axisOptions {
        X,
        Z,
    }
}