package cqb13.NumbyHack.utils;

import com.google.common.collect.Lists;
import meteordevelopment.meteorclient.MeteorClient;
import meteordevelopment.meteorclient.events.entity.EntityAddedEvent;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.option.Perspective;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LightningEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.particle.ParticleEffect;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.math.random.LocalRandom;
import net.minecraft.util.math.random.Random;
import net.minecraft.util.math.random.RandomSeed;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static meteordevelopment.meteorclient.MeteorClient.mc;

/**
 * super-duper top secret file with hidden fun effects hihi :P
 * provided by Number81 originally from Tanuki
 */
public class PlayerParticle {
  private static final UUID cqb13 = UUID.fromString("408fb01f-3ac5-4fa7-aa65-9fba051c9c51");
  private static final UUID IcatIcatI = UUID.fromString("ff8a62c2-b2d7-4334-8794-e0af3b9ad8c2");
  private static final UUID ThetaPride = UUID.fromString("a404936d-0e36-4185-9a16-1214e7e6d562");
  private static final UUID Number81 = UUID.fromString("bc48b56d-d2e2-4838-ae6d-bd26559c1267");

  private static final Map<UUID, ParticleEffect> PLAYER_EFFECTS = new HashMap<>();
  private static boolean LIGHTNING_HAS_STRUCK = false;
  private static final Random RANDOM = new LocalRandom(RandomSeed.getSeed());
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
    if (mc.world == null || mc.player == null) {
      return;
    }

    for (AbstractClientPlayerEntity player : mc.world.getPlayers()) {
      var uuid = player.getUuid();

      if (uuid.equals(mc.player.getUuid()) && mc.options.getPerspective() == Perspective.FIRST_PERSON) {
        continue;
      }

      var effect = PLAYER_EFFECTS.get(uuid);
      if (effect == null) {
        continue;
      }

      displayParticleEffect(player, effect);
    }
  }

  private static void displayParticleEffect(PlayerEntity player, ParticleEffect effect) {
    assert mc.player != null;
    assert mc.world != null;

    if (effect == ParticleTypes.SOUL) {
      double x = player.getX() + (RANDOM.nextDouble() - 0.5D) * (double) player.getWidth();
      double y = player.getY() + 0.1D;
      double z = player.getZ() + (RANDOM.nextDouble() - 0.5D) * (double) player.getHeight();
      double velocityX = player.getVelocity().getX() * -0.2D;
      double velocityY = 0.1D;
      double velocityZ = player.getVelocity().getZ() * -0.2D;
      mc.world.addParticleClient(effect, x, y, z, velocityX, velocityY, velocityZ);
    } else if (effect == ParticleTypes.HEART) {
      if (mc.player.age % 2 == 0) {
        var particleX = player.getParticleX(1.0D);
        var particleY = player.getRandomBodyY() + 0.5D;
        var particleZ = player.getParticleZ(1.0D);
        double velocityX = RANDOM.nextGaussian() * 0.02D;
        double velocityY = RANDOM.nextGaussian() * 0.02D;
        double velocityZ = RANDOM.nextGaussian() * 0.02D;
        mc.world.addParticleClient(effect, particleX, particleY, particleZ, velocityX, velocityY, velocityZ);
      }
    } else {
      for (int i = 0; i < 2; ++i) {
        double particleX = player.getParticleX(0.5D);
        double particleY = player.getRandomBodyY() - 0.25D;
        double particleZ = player.getParticleZ(0.5D);
        double velocityX = (RANDOM.nextDouble() - 0.5D) * 2.0D;
        double velocityY = -RANDOM.nextDouble();
        double velocityZ = (RANDOM.nextDouble() - 0.5D) * 2.0D;
        mc.world.addParticleClient(effect, particleX, particleY, particleZ, velocityX, velocityY, velocityZ);
      }
    }
  }

  @EventHandler
  private static void onEntityAdded(@NotNull EntityAddedEvent event) {
    assert mc.world != null;

    if (!LIGHTNING_UUIDS.contains(event.entity.getUuid())) {
      return;
    }

    double x = event.entity.getX();
    double y = event.entity.getY();
    double z = event.entity.getZ();

    var effect = new LightningEntity(EntityType.LIGHTNING_BOLT, mc.world);
    effect.setPosition(x, y, z);
    effect.refreshPositionAfterTeleport(x, y, z);

    mc.world.addEntity(effect);

    if (!LIGHTNING_HAS_STRUCK) {
      mc.world.playSound(mc.player, x, y, z, SoundEvents.ENTITY_LIGHTNING_BOLT_THUNDER, SoundCategory.WEATHER, 10000.0F,
          0.16000001F);
      mc.world.playSound(mc.player, x, y, z, SoundEvents.ENTITY_LIGHTNING_BOLT_IMPACT, SoundCategory.WEATHER, 2.0F,
          0.1F);
      LIGHTNING_HAS_STRUCK = true;
    }
  }
}
