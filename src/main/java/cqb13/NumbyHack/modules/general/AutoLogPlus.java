package cqb13.NumbyHack.modules.general;

import cqb13.NumbyHack.NumbyHack;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.settings.*;
import meteordevelopment.meteorclient.systems.friends.Friends;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.systems.modules.Modules;
import meteordevelopment.meteorclient.systems.modules.misc.AutoReconnect;
import meteordevelopment.meteorclient.utils.player.ChatUtils;
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
  private final SettingGroup sgHungerLog = settings.createGroup("Hunger Log");
  private final SettingGroup sgGeneral = settings.getDefaultGroup();

  // time log
  private final Setting<Boolean> timeLog = sgTimeLog.add(new BoolSetting.Builder()
      .name("time-log")
      .description("Logs you out after at a set time.")
      .defaultValue(false)
      .build());

  private final Setting<String> logTime = sgTimeLog.add(new StringSetting.Builder()
      .name("time")
      .description("The time to log you out (uses 24 hour time).")
      .defaultValue("12:00")
      .visible(timeLog::get)
      .build());

  // location log
  private final Setting<Boolean> locationLog = sgLocationLog.add(new BoolSetting.Builder()
      .name("location-log")
      .description("Disconnects when you reach set coordinates.")
      .defaultValue(false)
      .build());

  private final Setting<Boolean> oneAxis = sgLocationLog.add(new BoolSetting.Builder()
      .name("one-axis-log")
      .description("Disconnects when you reach set coordinates on a specific axis.")
      .defaultValue(false)
      .visible(locationLog::get)
      .build());

  private final Setting<AxisOptions> selectAxis = sgLocationLog.add(new EnumSetting.Builder<AxisOptions>()
      .name("select-axis")
      .description("The axis with the exact log coords.")
      .defaultValue(AxisOptions.X)
      .visible(oneAxis::get)
      .build());

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
      .description("The distance around the destination to log out at.")
      .defaultValue(64)
      .min(0)
      .sliderRange(0, 256)
      .visible(locationLog::get)
      .build());

  // y log
  private final Setting<Boolean> yLog = sgLocationLog.add(new BoolSetting.Builder()
      .name("y-log")
      .description("Disconnects if you go above or bellow a set Y level")
      .defaultValue(false)
      .build());

  private final Setting<YLogMode> yLogMode = sgLocationLog.add(new EnumSetting.Builder<YLogMode>()
      .name("y-log-mode")
      .description("Log out if you are above or bellow the set level")
      .defaultValue(YLogMode.Bellow)
      .visible(yLog::get)
      .build());

  private final Setting<Integer> yLogLimit = sgLocationLog.add(new IntSetting.Builder()
      .name("y-log-limit")
      .description("The y level at which the log out will trigger")
      .sliderRange(-64, 320)
      .defaultValue(81)
      .visible(yLog::get)
      .build());

  // ping log
  private final Setting<Boolean> pingLog = sgPingLog.add(new BoolSetting.Builder()
      .name("ping-log")
      .description("Disconnects when your ping is above a certain value.")
      .defaultValue(false)
      .build());

  private final Setting<Integer> pingValue = sgPingLog.add(new IntSetting.Builder()
      .name("ping-value")
      .defaultValue(1000)
      .range(0, 10000)
      .sliderRange(0, 10000)
      .visible(pingLog::get)
      .build());

  // hunger log
  private final Setting<Boolean> hungerLog = sgHungerLog.add(new BoolSetting.Builder()
      .name("hunger-log")
      .description("Disconnects when your hunger falls bellow a certain value.")
      .defaultValue(false)
      .build());

  private final Setting<Integer> hungerThreshold = sgHungerLog.add(new IntSetting.Builder()
      .name("hunger-threshold")
      .defaultValue(6)
      .range(0, 20)
      .sliderRange(0, 20)
      .visible(hungerLog::get)
      .build());

  // normal log
  private final Setting<Boolean> onlyTrusted = sgGeneral.add(new BoolSetting.Builder()
      .name("enemy")
      .description("Disconnects when a player not on your friends list appears in render distance.")
      .defaultValue(false)
      .build());

  private final Setting<Boolean> toggleAutoReconnect = sgGeneral.add(new BoolSetting.Builder()
      .name("toggle-auto-reconnect")
      .description("Turns off auto reconnect when disconnecting.")
      .defaultValue(true)
      .build());

  private final Setting<Boolean> toggleOff = sgGeneral.add(new BoolSetting.Builder()
      .name("toggle-off")
      .description("Disables Time Log after usage.")
      .defaultValue(true)
      .build());

  public AutoLogPlus() {
    super(NumbyHack.CATEGORY, "auto-log+", "Disconnects you when a specific condition is reached.");
  }

  @EventHandler
  private void onTick(TickEvent.Post event) {
    if (mc == null || mc.world == null || mc.player == null)
      return;

    if (onlyTrusted.get()) {
      playerLog();
    }

    if (hungerLog.get()) {
      hungerLog();
    }

    if (pingLog.get()) {
      highPingCheck();
    }

    if (timeLog.get()) {
      timeLog();
    }

    if (locationLog.get()) {
      locationLog();
    }

    if (yLog.get()) {
      yLog();
    }
  }

  // bad player log
  private void playerLog() {
    for (Entity entity : mc.world.getEntities()) {
      if (entity instanceof PlayerEntity && entity.getUuid() != mc.player.getUuid()) {
        if (entity != mc.player && !Friends.get().isFriend((PlayerEntity) entity)) {
          disconnect("A non trusted player [" + entity.getName().getString()
              + "] has entered your render distance.");
        }
      }

    }
  }

  private void hungerLog() {
    if (mc.player.getHungerManager().getFoodLevel() < hungerThreshold.get()) {
      disconnect("Your hunger level fell bellow " + hungerThreshold.get());
    }
  }

  private void timeLog() {
    DateTimeFormatter dtf = DateTimeFormatter.ofPattern("HH:mm");
    LocalDateTime now = LocalDateTime.now();
    if (dtf.format(now).equals(logTime.get())) {
      disconnect("Log time has been reached " + logTime.get() + ".");
    }
  }

  private void locationLog() {
    if (PlayerUtils.getDimension() == dimension.get()) {
      if (xCoordsMatch() && zCoordsMatch()) {
        disconnect("You have reached your destination");
      } else if (oneAxis.get()) {
        if (selectAxis.get() == AxisOptions.X && xCoordsMatch()) {
          disconnect("You have reached your destination");
        } else if (selectAxis.get() == AxisOptions.Z && zCoordsMatch()) {
          disconnect("You have reached your destination");
        }
      }
    }
  }

  private void yLog() {
    double playerY = mc.player.getY();

    if (yLogMode.get() == YLogMode.Above) {
      if (playerY > yLogLimit.get()) {
        disconnect("You have crossed above " + yLogLimit.get());
      }
    } else {
      if (playerY < yLogLimit.get()) {
        disconnect("You have crossed bellow " + yLogLimit.get());
      }
    }
  }

  private void highPingCheck() {
    if (mc.getNetworkHandler() == null || mc.player == null)
      return;
    PlayerListEntry playerListEntry = mc.getNetworkHandler().getPlayerListEntry(mc.player.getUuid());

    int ping = playerListEntry.getLatency();

    if (ping >= pingValue.get())
      disconnect("High ping [" + ping + "]");
  }

  private boolean xCoordsMatch() {
    return (mc.player.getX() <= xCoords.get() + radius.get() && mc.player.getX() >= xCoords.get() - radius.get());
  }

  private boolean zCoordsMatch() {
    return (mc.player.getZ() <= zCoords.get() + radius.get() && mc.player.getZ() >= zCoords.get() - radius.get());
  }

  private void disconnect(String text) {
    if (mc.getNetworkHandler() == null)
      return;
    mc.player.networkHandler.onDisconnect(new DisconnectS2CPacket(Text.of("[Auto Log+] " + text)));

    if (toggleOff.get())
      toggle();

    if (toggleAutoReconnect.get() && Modules.get().isActive(AutoReconnect.class))
      Modules.get().get(AutoReconnect.class).toggle();
  }

  public enum AxisOptions {
    X,
    Z,
  }

  public enum YLogMode {
    Above,
    Bellow,
  }
}
