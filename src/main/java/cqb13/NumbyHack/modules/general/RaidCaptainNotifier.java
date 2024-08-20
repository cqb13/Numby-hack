package cqb13.NumbyHack.modules.general;

import cqb13.NumbyHack.NumbyHack;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.settings.BoolSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.utils.player.ChatUtils;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.entity.Entity;
import net.minecraft.entity.mob.PillagerEntity;
import net.minecraft.nbt.NbtCompound;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class RaidCaptainNotifier extends Module {
    private final SettingGroup sgGeneral = settings.getDefaultGroup();

    private final Setting<Boolean> location = sgGeneral.add(new BoolSetting.Builder()
            .name("location")
            .description("Includes coordinates in the notification.")
            .defaultValue(false)
            .build()
    );

    private final Set<UUID> checkedPillagers = new HashSet<>();

    public RaidCaptainNotifier() {
        super(NumbyHack.CATEGORY, "Raid Captain Notifier", "Notifies you when a raid captain spawns, good for waiting in outposts on bad servers.");
    }

    @EventHandler
    private void onTick(TickEvent.Pre event) {
        if (mc.world == null) return;

        for (Entity entity : mc.world.getEntities()) {
            if (entity instanceof PillagerEntity) {
                PillagerEntity pillager = (PillagerEntity) entity;
                UUID pillagerId = pillager.getUuid();

                if (checkedPillagers.contains(pillagerId)) continue;

                NbtCompound nbtData = pillager.writeNbt(new NbtCompound());
                String nbtString = nbtData.asString();

                //TODO: Improve this when not lazy
                if (nbtString.contains("translate\":\"block.minecraft.ominous_banner")) {
                    System.out.println(nbtData);
                    if (location.get()) {
                        ChatUtils.info("Raid Captain Spawned at " + pillager.getBlockX() + ", " + pillager.getBlockY() + ", " + pillager.getBlockZ() + "!");
                    } else {
                        ChatUtils.info("Raid Captain Spawned!");
                    }

                    checkedPillagers.add(pillagerId);
                }
            }
        }
    }
}
