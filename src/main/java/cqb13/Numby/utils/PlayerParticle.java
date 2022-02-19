package cqb13.Numby.utils;

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
import org.jetbrains.annotations.NotNull;

import java.util.*;

import static meteordevelopment.meteorclient.MeteorClient.mc;

public class PlayerParticle {

    public Map<UUID, ParticleEffect> playerEffects;
    private boolean lightningHasStruck = false;
    private Random random;
    public List<UUID> lightningUUIDs;

    public PlayerParticle() {
        setup();
        MeteorClient.EVENT_BUS.subscribe(this);
    }

    private void setup() {
        this.random = new Random();
        this.playerEffects = new HashMap<>();
        this.lightningUUIDs = new ArrayList<>();
        // --- Lighting ---
        //cqb13
        this.lightningUUIDs.add(UUID.fromString("408fb01f-3ac5-4fa7-aa65-9fba051c9c51"));
        //IcatIcatI
        this.lightningUUIDs.add(UUID.fromString("ff8a62c2-b2d7-4334-8794-e0af3b9ad8c2"));
        // Number81
        this.lightningUUIDs.add(UUID.fromString("bc48b56d-d2e2-4838-ae6d-bd26559c1267"));

        // --- Particle ---
        //cqb13
        this.playerEffects.put(UUID.fromString("408fb01f-3ac5-4fa7-aa65-9fba051c9c51"), ParticleTypes.NAUTILUS);
        //IcatIcatI
        this.playerEffects.put(UUID.fromString("ff8a62c2-b2d7-4334-8794-e0af3b9ad8c2"), ParticleTypes.NAUTILUS);
        // Number81
        this.playerEffects.put(UUID.fromString("bc48b56d-d2e2-4838-ae6d-bd26559c1267"), ParticleTypes.HAPPY_VILLAGER);


    }

    @EventHandler
    private void onPostTickEvent(TickEvent.Post post) {
        if (mc.world == null || mc.player == null) {
            return;
        }

        for (AbstractClientPlayerEntity player : mc.world.getPlayers()) {
            var uuid = player.getUuid();

            if (uuid.equals(mc.player.getUuid()) && mc.options.getPerspective() == Perspective.FIRST_PERSON) {
                continue;
            }

            var effect = this.playerEffects.get(uuid);
            if (effect == null) {
                continue;
            }

            this.displayParticleEffect(player, effect);
        }
    }

    private void displayParticleEffect(PlayerEntity player, ParticleEffect effect) {
        assert mc.player != null;
        assert mc.world != null;

        if (effect == ParticleTypes.SOUL) {
            double x = player.getX() + (this.random.nextDouble() - 0.5D) * (double) player.getWidth();
            double y = player.getY() + 0.1D;
            double z = player.getZ() + (this.random.nextDouble() - 0.5D) * (double) player.getHeight();
            double velocityX = player.getVelocity().getX() * -0.2D;
            double velocityY = 0.1D;
            double velocityZ = player.getVelocity().getZ() * -0.2D;
            mc.world.addParticle(effect, x, y, z, velocityX, velocityY, velocityZ);
        } else if (effect == ParticleTypes.HEART) {
            if (mc.player.age % 2 == 0) {
                var particleX = player.getParticleX(1.0D);
                var particleY = player.getRandomBodyY() + 0.5D;
                var particleZ = player.getParticleZ(1.0D);
                double velocityX = this.random.nextGaussian() * 0.02D;
                double velocityY = this.random.nextGaussian() * 0.02D;
                double velocityZ = this.random.nextGaussian() * 0.02D;
                mc.world.addParticle(effect, particleX, particleY, particleZ, velocityX, velocityY, velocityZ);
            }
        } else {
            for (int i = 0; i < 2; ++i) {
                double particleX = player.getParticleX(0.5D);
                double particleY = player.getRandomBodyY() - 0.25D;
                double particleZ = player.getParticleZ(0.5D);
                double velocityX = (this.random.nextDouble() - 0.5D) * 2.0D;
                double velocityY = -this.random.nextDouble();
                double velocityZ = (this.random.nextDouble() - 0.5D) * 2.0D;
                mc.world.addParticle(effect, particleX, particleY, particleZ, velocityX, velocityY, velocityZ);
            }
        }
    }

    @EventHandler
    private void onEntityAdded(@NotNull EntityAddedEvent event) {
        assert mc.world != null;

        if (!this.lightningUUIDs.contains(event.entity.getUuid())) {
            return;
        }

        double x = event.entity.getX();
        double y = event.entity.getY();
        double z = event.entity.getZ();

        var effect = new LightningEntity(EntityType.LIGHTNING_BOLT, mc.world);
        effect.setPosition(x, y, z);
        effect.refreshPositionAfterTeleport(x, y, z);

        mc.world.addEntity(effect.getId(), effect);

        if (!this.lightningHasStruck) {
            mc.world.playSound(mc.player, x, y, z, SoundEvents.ENTITY_LIGHTNING_BOLT_THUNDER, SoundCategory.WEATHER, 10000.0F, 0.16000001F);
            mc.world.playSound(mc.player, x, y, z, SoundEvents.ENTITY_LIGHTNING_BOLT_IMPACT, SoundCategory.WEATHER, 2.0F, 0.1F);
            this.lightningHasStruck = true;
        }
    }
}
