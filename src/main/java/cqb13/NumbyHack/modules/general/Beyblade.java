package cqb13.NumbyHack.modules.general;

import cqb13.NumbyHack.NumbyHack;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.settings.BoolSetting;
import meteordevelopment.meteorclient.settings.DoubleSetting;
import meteordevelopment.meteorclient.settings.EnumSetting;
import meteordevelopment.meteorclient.settings.IntSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.systems.modules.Modules;
import meteordevelopment.meteorclient.systems.modules.combat.Quiver;
import meteordevelopment.meteorclient.systems.modules.player.EXPThrower;
import meteordevelopment.meteorclient.utils.player.Rotations;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.item.BowItem;
import net.minecraft.item.EnderPearlItem;
import net.minecraft.item.ExperienceBottleItem;
import net.minecraft.item.Items;

// FloRida from venomhack

public class Beyblade extends Module {
    private final SettingGroup sgGeneral = settings.getDefaultGroup();

    private final Setting<Mode> spinMode = sgGeneral.add(new EnumSetting.Builder<Mode>()
            .name("spin-mode")
            .description("The way in which to spin you.")
            .defaultValue(Mode.Beyblade)
            .build());

    private final Setting<Double> speed = sgGeneral.add(new DoubleSetting.Builder()
            .name("rotation-speed")
            .description("The speed at which you rotate.")
            .defaultValue(20)
            .sliderMin(0.0)
            .sliderMax(50.0)
            .visible(() -> spinMode.get() == Mode.FloRida)
            .build());

    private final Setting<AntiDesyncTrigger> antiDesync = sgGeneral.add(new EnumSetting.Builder<AntiDesyncTrigger>()
            .name("anti-desync")
            .description("Stops spinning on some triggers.")
            .defaultValue(AntiDesyncTrigger.All)
            .visible(() -> spinMode.get() == Mode.Beyblade)
            .build());

    private final Setting<Boolean> yaw = sgGeneral.add(new BoolSetting.Builder()
            .name("yaw")
            .description("Spin around.")
            .defaultValue(true)
            .visible(() -> spinMode.get() == Mode.Beyblade)
            .build());

    private final Setting<Integer> ySpeed = sgGeneral.add(new IntSetting.Builder()
            .name("yaw-speed")
            .description("The speed at which you rotate.")
            .defaultValue(5)
            .range(1, 100)
            .visible(() -> spinMode.get() == Mode.Beyblade && yaw.get())
            .build());

    private final Setting<Boolean> pitch = sgGeneral.add(new BoolSetting.Builder()
            .name("pitch")
            .description("Spin around.")
            .defaultValue(false)
            .visible(() -> spinMode.get() == Mode.Beyblade)
            .build());

    private final Setting<Integer> pSpeed = sgGeneral.add(new IntSetting.Builder()
            .name("rotation-speed")
            .description("The speed at which you rotate.")
            .defaultValue(5)
            .range(1, 100)
            .visible(() -> spinMode.get() == Mode.Beyblade && pitch.get())
            .build());

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

        if (spinMode.get() == Mode.Beyblade) {
            beyblade();
        } else {
            floRida();
        }
    }

    private void beyblade() {
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
                        mc.player.getEquippedStack(EquipmentSlot.CHEST).getItem() == Items.ELYTRA)
                    return;
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
                        mc.player.getOffHandStack().getItem() instanceof BowItem)
                    return;
            }
            case None -> {
            }
        }

        yCount += ySpeed.get();
        if (yCount > 180)
            yCount = -180;

        if (pitch.get()) {
            count++;

            if (count <= pSpeed.get())
                pCount = 90;
            if (count > pSpeed.get())
                pCount = -90;
            if (count >= pSpeed.get() + pSpeed.get())
                count = 0;
        }

        Rotations.rotate(yaw.get() ? yCount : mc.player.getYaw(), yaw.get() ? pCount : mc.player.getPitch());
    }

    private void floRida() {
        Modules modules = Modules.get();
        if (!modules.isActive(EXPThrower.class) && !modules.isActive(Quiver.class)
                && !modules.isActive(EXPThrower.class)) {
            count += speed.get();
            if (count > 180) {
                count -= 360;
            }

            Rotations.rotate(count, 0.0);
        }
    }

    public enum Mode {
        Beyblade,
        FloRida,
    }

    public enum AntiDesyncTrigger {
        All, ExceptElytra, None
    }
}
