package cqb13.Numby.modules;

import cqb13.Numby.Numby;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.settings.BoolSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.settings.StringSetting;
import meteordevelopment.meteorclient.systems.friends.Friends;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.packet.s2c.play.DisconnectS2CPacket;
import net.minecraft.text.LiteralText;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
/**
 * made by cqb13
 */
public class TimeLog extends Module {
    private final SettingGroup sgGeneral = settings.getDefaultGroup();

    private final Setting<String> logTime = sgGeneral.add(new StringSetting.Builder()
            .name("time")
            .description("The time to log you out (uses 24 hour time).")
            .defaultValue("12:00")
            .build()
    );

    private final Setting<Boolean> onlyTrusted = sgGeneral.add(new BoolSetting.Builder()
            .name("only-trusted")
            .description("Disconnects when a player not on your friends list appears in render distance.")
            .defaultValue(false)
            .build()
    );

    private final Setting<Boolean> smartToggle = sgGeneral.add(new BoolSetting.Builder()
            .name("smart-toggle")
            .description("Disables Time Log after a low-health logout. WILL re-enable once you heal.")
            .defaultValue(false)
            .build()
    );

    private final Setting<Boolean> toggleOff = sgGeneral.add(new BoolSetting.Builder()
            .name("toggle-off")
            .description("Disables Time Log after usage.")
            .defaultValue(true)
            .build()
    );

    public TimeLog() {
        super(Numby.CATEGORY, "time-log", "Automatically disconnects you at a certain time.");
    }

    @EventHandler
    private void onTick(TickEvent.Post event) {
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("HH:mm");
        LocalDateTime now = LocalDateTime.now();
        if (mc.player.getHealth() <= 0) {
            this.toggle();
            return;
        }
        if (dtf.format(now).equals(logTime.get())) {
            mc.player.networkHandler.onDisconnect(new DisconnectS2CPacket(new LiteralText("[TimeLog] log time has been reached " + logTime.get() + ".")));
            if (toggleOff.get()) this.toggle();
        }

        for (Entity entity : mc.world.getEntities()) {
            if (entity instanceof PlayerEntity && entity.getUuid() != mc.player.getUuid()) {
                if (onlyTrusted.get() && entity != mc.player && !Friends.get().isFriend((PlayerEntity) entity)) {
                    mc.player.networkHandler.onDisconnect(new DisconnectS2CPacket(new LiteralText("[AutoLog] A non-trusted player appeared in your render distance.")));
                    if (toggleOff.get()) this.toggle();
                    break;
                }
            }

        }
    }

}
