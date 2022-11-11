package cqb13.NumbyHack.modules.general;

import cqb13.NumbyHack.NumbyHack;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.settings.*;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.systems.modules.Modules;
import meteordevelopment.meteorclient.systems.modules.player.EXPThrower;
import meteordevelopment.meteorclient.utils.player.Rotations;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.item.BowItem;
import net.minecraft.item.EnderPearlItem;
import net.minecraft.item.ExperienceBottleItem;
import net.minecraft.item.Items;

public class Beyblade extends Module {
    private final SettingGroup sgDefault = settings.getDefaultGroup();

    private final Setting<Mode> antiDesync = sgDefault.add(new EnumSetting.Builder<Mode>()
            .name("anti-desync")
            .description("Stops spinning on some triggers.")
            .defaultValue(Mode.All)
            .build()
    );

    private final Setting<Boolean> yaw = sgDefault.add(new BoolSetting.Builder()
            .name("yaw")
            .description("Spin around.")
            .defaultValue(true)
            .build()
    );

    private final Setting<Integer> ySpeed = sgDefault.add(new IntSetting.Builder()
            .name("yaw-speed")
            .description("The speed at which you rotate.")
            .defaultValue(5)
            .range(1, 100)
            .visible(yaw::get)
            .build()
    );

    private final Setting<Boolean> pitch = sgDefault.add(new BoolSetting.Builder()
            .name("pitch")
            .description("Spin around.")
            .defaultValue(false)
            .build()
    );

    private final Setting<Integer> pSpeed = sgDefault.add(new IntSetting.Builder()
            .name("speed")
            .description("The speed at which you rotate.")
            .defaultValue(5)
            .range(1, 100)
            .visible(pitch::get)
            .build()
    );

    public Beyblade() {
        super(NumbyHack.CATEGORY, "Beyblade", "Tries to rotate you.");
    }

    private short count = 0;
    private short yCount = 0;
    private short pCount = 0;

    @Override
    public void onActivate() {
        count = 0;
    }

    @EventHandler
    public void onTick(TickEvent.Post event) {
        assert mc.player != null;

        switch (antiDesync.get()) {
            case All -> {
                if (Modules.get().isActive(EXPThrower.class) ||
                        Modules.get().isActive(meteordevelopment.meteorclient.systems.modules.combat.BedAura.class) ||
                        mc.player.getMainHandStack().getItem() instanceof ExperienceBottleItem ||
                        mc.player.getOffHandStack().getItem() instanceof ExperienceBottleItem ||
                        mc.player.getMainHandStack().getItem() instanceof EnderPearlItem ||
                        mc.player.getOffHandStack().getItem() instanceof EnderPearlItem ||
                        mc.player.getMainHandStack().getItem() instanceof ExperienceBottleItem ||
                        mc.player.getOffHandStack().getItem() instanceof ExperienceBottleItem ||
                        mc.player.getMainHandStack().getItem() instanceof BowItem ||
                        mc.player.getOffHandStack().getItem() instanceof BowItem ||
                        mc.player.getInventory().getArmorStack(2).getItem() == Items.ELYTRA) return;
            }
            case ExceptElytra -> {
                if (Modules.get().isActive(EXPThrower.class) ||
                        Modules.get().isActive(meteordevelopment.meteorclient.systems.modules.combat.BedAura.class) ||
                        mc.player.getMainHandStack().getItem() instanceof ExperienceBottleItem ||
                        mc.player.getOffHandStack().getItem() instanceof ExperienceBottleItem ||
                        mc.player.getMainHandStack().getItem() instanceof EnderPearlItem ||
                        mc.player.getOffHandStack().getItem() instanceof EnderPearlItem ||
                        mc.player.getMainHandStack().getItem() instanceof ExperienceBottleItem ||
                        mc.player.getOffHandStack().getItem() instanceof ExperienceBottleItem ||
                        mc.player.getMainHandStack().getItem() instanceof BowItem ||
                        mc.player.getOffHandStack().getItem() instanceof BowItem) return;
            }
        }

        yCount += ySpeed.get();
        if (yCount > 180) yCount = -180;

        if (pitch.get()) {
            count++;

            if (count <= pSpeed.get()) pCount = 90;
            if (count > pSpeed.get()) pCount = -90;
            if (count >= pSpeed.get() + pSpeed.get()) count = 0;
        }

        Rotations.rotate(yaw.get() ? yCount : mc.player.getYaw(), yaw.get() ? pCount : mc.player.getPitch());
    }

    public enum Mode {
        All, ExceptElytra, None
    }
}
