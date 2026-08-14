package cqb13.NumbyHack.utils;

import static meteordevelopment.meteorclient.MeteorClient.mc;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.jetbrains.annotations.NotNull;

import com.google.common.collect.Lists;

import meteordevelopment.meteorclient.MeteorClient;
import meteordevelopment.meteorclient.events.entity.EntityAddedEvent;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.client.CameraType;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.LightningBolt;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.levelgen.RandomSupport;
import net.minecraft.world.level.levelgen.SingleThreadedRandomSource;

/**
 * super-duper top secret file with hidden fun effects hihi :P
 * provided by Number81 originally from Tanuki
 */
public class PlayerParticle {
    private static final UUID cqb13 = UUID.fromString("408fb01f-3ac5-4fa7-aa65-9fba051c9c51");
    private static final UUID IcatIcatI = UUID.fromString("ff8a62c2-b2d7-4334-8794-e0af3b9ad8c2");
    private static final UUID ThetaPride = UUID.fromString("a404936d-0e36-4185-9a16-1214e7e6d562");
    private static final UUID Number81 = UUID.fromString("bc48b56d-d2e2-4838-ae6d-bd26559c1267");

    private static final Map<UUID, ParticleOptions> PLAYER_EFFECTS = new HashMap<>();
    private static boolean LIGHTNING_HAS_STRUCK = false;
    private static final RandomSource RANDOM = new SingleThreadedRandomSource(RandomSupport.generateUniqueSeed());
    private static final List<UUID> LIGHTNING_UUIDS = Lists.newArrayList(cqb13, IcatIcatI, ThetaPride, Number81);

    /**
     * Set up all the unique player effects.
     *
     * @link https://minecraft.fandom.com/wiki/Particles#Particle_textures
     */
    public static void init() {
        MeteorClient.EVENT_BUS.subscribe(PlayerParticle.class);
        // --- Particle ---
        PLAYER_EFFECTS.put(cqb13, ParticleTypes.NAUTILUS);
        PLAYER_EFFECTS.put(IcatIcatI, ParticleTypes.NAUTILUS);
        PLAYER_EFFECTS.put(ThetaPride, ParticleTypes.NAUTILUS);
        PLAYER_EFFECTS.put(Number81, ParticleTypes.HAPPY_VILLAGER);
    }

    @EventHandler
    private static void onPostTickEvent(TickEvent.Post post) {
        if (mc.level == null || mc.player == null) {
            return;
        }

        for (AbstractClientPlayer player : mc.level.players()) {
            var uuid = player.getUUID();

            if (uuid.equals(mc.player.getUUID()) && mc.options.getCameraType() == CameraType.FIRST_PERSON) {
                continue;
            }

            var effect = PLAYER_EFFECTS.get(uuid);
            if (effect == null) {
                continue;
            }

            displayParticleEffect(player, effect);
        }
    }

    private static void displayParticleEffect(Player player, ParticleOptions effect) {
        if (mc.level == null || mc.player == null) {
            return;
        }

        if (effect == ParticleTypes.SOUL) {
            double x = player.getX() + (RANDOM.nextDouble() - 0.5D) * (double) player.getBbWidth();
            double y = player.getY() + 0.1D;
            double z = player.getZ() + (RANDOM.nextDouble() - 0.5D) * (double) player.getBbHeight();
            double velocityX = player.getDeltaMovement().x() * -0.2D;
            double velocityY = 0.1D;
            double velocityZ = player.getDeltaMovement().z() * -0.2D;
            mc.level.addParticle(effect, x, y, z, velocityX, velocityY, velocityZ);
        } else if (effect == ParticleTypes.HEART) {
            if (mc.player.tickCount % 2 == 0) {
                var particleX = player.getRandomX(1.0D);
                var particleY = player.getRandomY() + 0.5D;
                var particleZ = player.getRandomZ(1.0D);
                double velocityX = RANDOM.nextGaussian() * 0.02D;
                double velocityY = RANDOM.nextGaussian() * 0.02D;
                double velocityZ = RANDOM.nextGaussian() * 0.02D;
                mc.level.addParticle(effect, particleX, particleY, particleZ, velocityX, velocityY, velocityZ);
            }
        } else {
            for (int i = 0; i < 2; ++i) {
                double particleX = player.getRandomX(0.5D);
                double particleY = player.getRandomY() - 0.25D;
                double particleZ = player.getRandomZ(0.5D);
                double velocityX = (RANDOM.nextDouble() - 0.5D) * 2.0D;
                double velocityY = -RANDOM.nextDouble();
                double velocityZ = (RANDOM.nextDouble() - 0.5D) * 2.0D;
                mc.level.addParticle(effect, particleX, particleY, particleZ, velocityX, velocityY, velocityZ);
            }
        }
    }

    @EventHandler
    private static void onEntityAdded(@NotNull EntityAddedEvent event) {
        if (mc.level == null || mc.player == null) {
            return;
        }

        if (event.entity.getUUID().equals(mc.player.getUUID())) {
            return;
        }

        if (!LIGHTNING_UUIDS.contains(event.entity.getUUID())) {
            return;
        }

        double x = event.entity.getX();
        double y = event.entity.getY();
        double z = event.entity.getZ();

        var effect = new LightningBolt(EntityTypes.LIGHTNING_BOLT, mc.level);
        effect.setPos(x, y, z);
        effect.snapTo(x, y, z);

        mc.level.addEntity(effect);

        if (!LIGHTNING_HAS_STRUCK) {
            mc.level.playSound(mc.player, x, y, z, SoundEvents.LIGHTNING_BOLT_THUNDER, SoundSource.WEATHER,
                    10000.0F,
                    0.16000001F);
            mc.level.playSound(mc.player, x, y, z, SoundEvents.LIGHTNING_BOLT_IMPACT, SoundSource.WEATHER,
                    2.0F,
                    0.1F);
            LIGHTNING_HAS_STRUCK = true;
        }
    }
}
