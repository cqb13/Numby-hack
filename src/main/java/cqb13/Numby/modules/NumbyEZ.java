package cqb13.Numby.modules;

import cqb13.Numby.Numby;
import cqb13.Numby.utils.CatHackEntityUtils;
import it.unimi.dsi.fastutil.chars.Char2CharArrayMap;
import it.unimi.dsi.fastutil.chars.Char2CharMap;
import meteordevelopment.meteorclient.events.packets.PacketEvent;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.settings.*;
import meteordevelopment.meteorclient.systems.friends.Friends;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.systems.modules.Modules;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.packet.c2s.play.ChatMessageC2SPacket;
import net.minecraft.network.packet.s2c.play.EntityStatusS2CPacket;

import java.util.*;

public class NumbyEZ extends Module {
    private final SettingGroup sgGeneral = settings.getDefaultGroup();
    private final SettingGroup sgPops = settings.createGroup("Pops");
    private final SettingGroup sgKills = settings.createGroup("Kills");
    private final SettingGroup sgTargeting = settings.createGroup("Targeting");


    // General


    private final Setting<Boolean> clearOnDeath = sgGeneral.add(new BoolSetting.Builder()
            .name("clear-on-death")
            .description("Resets your scores on death.")
            .defaultValue(true)
            .build()
    );

    private final Setting<Boolean> message = sgGeneral.add(new BoolSetting.Builder()
            .name("message")
            .description("Sends messages in the chat when you kill or pop players.")
            .defaultValue(true)
            .build()
    );

    private final Setting<Boolean> notify = sgGeneral.add(new BoolSetting.Builder()
            .name("notify")
            .description("Sends client-side messages with your kill and pop streak after you kill players.")
            .defaultValue(false)
            .build()
    );

    private final Setting<Boolean> smallCaps = sgGeneral.add(new BoolSetting.Builder()
            .name("small-caps")
            .description("Sends all messages with small caps.")
            .defaultValue(false)
            .build()
    );

    private final Setting<Boolean> randomMsg = sgGeneral.add(new BoolSetting.Builder()
            .name("random")
            .description("Sends random messages every kill or pop.")
            .defaultValue(true)
            .build()
    );


    // Pops


    private final Setting<Boolean> popMsg = sgPops.add(new BoolSetting.Builder()
            .name("pop")
            .description("Sends a messages everytime you pop a player.")
            .defaultValue(true)
            .build()
    );

    private final Setting<String> popString = sgPops.add(new StringSetting.Builder()
            .name("pop-message")
            .description("The message to send when you popped a player.")
            .defaultValue("{player} was popped by Numby hack")
            .visible(() -> popMsg.get() && !randomMsg.get())
            .build()
    );

    private final Setting<List<String>> popMessages = sgPops.add(new StringListSetting.Builder()
            .name("pop-messages")
            .description("The random messages to send when you popped a player.")
            .defaultValue(List.of("Number81 hack on top", "ez pop {player}", "popped {player} with Numby hack", "{pops} pops on {player} already", "81 on top"))
            .visible(() -> popMsg.get() && randomMsg.get())
            .build()
    );

    private final Setting<Integer> popDelay = sgPops.add(new IntSetting.Builder()
            .name("pop-delay")
            .description("How long to wait in ticks before sending a pop message again.")
            .defaultValue(15)
            .min(0)
            .visible(popMsg::get)
            .build()
    );


    // Kills


    private final Setting<Boolean> killMsg = sgKills.add(new BoolSetting.Builder()
            .name("kill")
            .description("Sends a messages everytime you kill a player.")
            .defaultValue(true)
            .build()
    );

    private final Setting<String> killString = sgKills.add(new StringSetting.Builder()
            .name("kill-message")
            .description("The message to send when you killed someone.")
            .defaultValue("killed {player} with Numby hack")
            .visible(() -> killMsg.get() && !randomMsg.get())
            .build()
    );

    private final Setting<List<String>> killMessages = sgKills.add(new StringListSetting.Builder()
            .name("kill-messages")
            .description("The random messages to send when you kill someone.")
            .defaultValue(List.of("ez {player}", "killed {player} with Numby hack", "currently at {kills} kill streak", "{playerkills} kills on {player} already", "{player} could not handle the power of Number81!", "Numby hack was too strong for {player}"))
            .visible(() -> killMsg.get() && randomMsg.get())
            .build()
    );

    private final Setting<Integer> killDelay = sgKills.add(new IntSetting.Builder()
            .name("kill-delay")
            .description("How long to wait in ticks before sending a kill message again.")
            .defaultValue(5)
            .min(0)
            .visible(killMsg::get)
            .build()
    );


    // Targeting


    private final Setting<Boolean> ignoreFriends = sgTargeting.add(new BoolSetting.Builder()
            .name("ignore-friends")
            .description("Ignores friended players.")
            .defaultValue(true)
            .build()
    );

    private final Setting<Boolean> checkTargets = sgTargeting.add(new BoolSetting.Builder()
            .name("check-targets")
            .description("Checks if the player is targeted in any module.")
            .defaultValue(true)
            .build()
    );

    private final Setting<Double> range = sgTargeting.add(new DoubleSetting.Builder()
            .name("range")
            .description("The range a player has to be in with to detect a pop.")
            .defaultValue(7)
            .min(0)
            .max(50)
            .build()
    );

    private final Char2CharMap SMALL_CAPS = new Char2CharArrayMap();
    private final Random random = new Random();

    private HashMap<UUID, Integer> kills;
    private HashMap<UUID, Integer> pops;
    private int allKills;
    private int killTimer;
    private int popTimer;

    public NumbyEZ() {
        super(Numby.CATEGORY, "numby-ez", "Send a chat message after killing or popping a player.");

        String[] a = "abcdefghijklmnopqrstuvwxyz".split("");
        String[] b = "ᴀʙᴄᴅᴇꜰɢʜɪᴊᴋʟᴍɴᴏᴘqʀꜱᴛᴜᴠᴡxʏᴢ".split("");
        for (int i = 0; i < a.length; i++) SMALL_CAPS.put(a[i].charAt(0), b[i].charAt(0));
    }

    @Override
    public void onActivate() {
        kills = new HashMap<>();
        pops = new HashMap<>();

        allKills = 0;
        killTimer = 0;
        popTimer = 0;
    }

    // Pops & Kills

    @EventHandler
    private void onReceivePacket(PacketEvent.Receive event) {
        if (event.packet instanceof EntityStatusS2CPacket packet) {
            if (packet.getStatus() != 35 && packet.getStatus() != 3) return;
            if (packet.getStatus() == 3 && packet.getEntity(mc.world) == mc.player && clearOnDeath.get()) onActivate();

            Entity entity = packet.getEntity(mc.world);

            if (!check(entity, range.get(), true)) return;

            if (packet.getStatus() == 35) {
                pops.putIfAbsent(entity.getUuid(), 0);
                pops.replace(entity.getUuid(), pops.get(entity.getUuid()) + 1);

                if (popMsg.get() && (popTimer >= popDelay.get() || popDelay.get() == 0)) {
                    sendPopMsg((PlayerEntity) entity);
                    popTimer = 0;
                    return;
                }
            }

            if (packet.getStatus() == 3) {
                kills.putIfAbsent(entity.getUuid(), 0);
                kills.replace(entity.getUuid(), kills.get(entity.getUuid()) + 1);

                allKills++;

                if (killMsg.get() && (killTimer >= killDelay.get() || killDelay.get() == 0)) {
                    sendKillMsg((PlayerEntity) entity);
                    pops.replace(entity.getUuid(), 0);
                    killTimer = 0;
                }
            }
        }
    }

    // Ticking Timers & updating Pops

    @EventHandler
    private void onPostTick(TickEvent.Post event) {
        killTimer++;
        popTimer++;

        if (mc != null && mc.world != null) for (UUID uuid : new HashSet<>(pops.keySet())) if (mc.world.getPlayerByUuid(uuid) == null) pops.replace(uuid, 0);
    }

    // Messaging

    private void sendPopMsg(PlayerEntity player) {
        sendMsg(apply(player, randomMsg.get() && !popMessages.get().isEmpty() ? (popMessages.get().size() > 1 ? popMessages.get().get(random.nextInt(popMessages.get().size())) : popMessages.get().get(0)) : popString.get()));
    }

    private void sendKillMsg(PlayerEntity player) {
        if (message.get()) {
            sendMsg(apply(player, randomMsg.get() && !killMessages.get().isEmpty() ? (killMessages.get().size() > 1 ? killMessages.get().get(random.nextInt(killMessages.get().size())) : killMessages.get().get(0)) : killString.get()));
        }

        int pop = pops.get(player.getUuid()) == null ? 0 : pops.get(player.getUuid());
        int kill = kills.get(player.getUuid()) == null ? 0 : kills.get(player.getUuid());
        if (notify.get()) info("Popped " + player.getGameProfile().getName() + " " + pop + (pop == 1 ? " time" : " times") + " and killed him " + kill + (kill == 1 ? " time." : " times."));
    }

    private void sendMsg(String string) {
        if (string != null) {
            StringBuilder builder = new StringBuilder();

            if (smallCaps.get()) {
                for (char ch : string.toCharArray()) {
                    if (SMALL_CAPS.containsKey(ch)) builder.append(SMALL_CAPS.get(ch));
                    else builder.append(ch);
                }
            }

            if (message.get()) {
                mc.getNetworkHandler().sendPacket(new ChatMessageC2SPacket(smallCaps.get() ? builder.toString() : string));
            } else {
                info(smallCaps.get() ? builder.toString() : string);
            }
        }
    }

    // Utils

    private String apply(PlayerEntity player, String message) {
        String string = message.replace("{player}", player.getGameProfile().getName());

        string = string.replace("{pops}", String.valueOf(pops.get(player.getUuid())));
        string = string.replace("{playerkills}", String.valueOf(kills.get(player.getUuid())));
        string = string.replace("{kills}", String.valueOf(allKills));

        return string;
    }

    private boolean check(Entity entity, double range, boolean shouldCheckTargets) {
        if (!(entity instanceof PlayerEntity) || entity == mc.player
                || (Friends.get().isFriend((PlayerEntity) entity) && ignoreFriends.get())
                || CatHackEntityUtils.distance(mc.player.getPos(), entity.getPos()) > range) return false;

        if (checkTargets.get() && shouldCheckTargets) {
            boolean target = true;

            for (Module module : Modules.get().getAll()) {
                if (module.getInfoString() != null && module.getInfoString().contains(((PlayerEntity) entity).getGameProfile().getName())) {
                    target = false;
                    break;
                }
            }

            return !target;
        }

        return true;
    }
}

