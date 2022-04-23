package cqb13.Numby.utils;

import meteordevelopment.meteorclient.utils.Utils;
import meteordevelopment.meteorclient.utils.player.PlayerUtils;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.PlayerListEntry;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.decoration.EndCrystalEntity;
import net.minecraft.entity.effect.StatusEffectUtil;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.fluid.Fluid;
import net.minecraft.fluid.Fluids;
import net.minecraft.tag.FluidTags;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.GameMode;
import net.minecraft.world.RaycastContext;
import net.minecraft.world.World;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class CatHackEntityUtils {

    public static MinecraftClient mc;

    public static double distance(double x1, double y1, double z1, double x2, double y2, double z2) {
        double dX = x2 - x1;
        double dY = y2 - y1;
        double dZ = z2 - z1;

        return Math.sqrt(dX * dX + dY * dY + dZ * dZ);
    }

    public static double distance(Vec3d vec1, Vec3d vec2) {
        double dX = vec2.x - vec1.x;
        double dY = vec2.y - vec1.y;
        double dZ = vec2.z - vec1.z;

        return Math.sqrt(dX * dX + dY * dY + dZ * dZ);
    }

    public static double distance(BlockPos pos1, BlockPos pos2) {
        double dX = pos2.getX() - pos1.getX();
        double dY = pos2.getY() - pos1.getY();
        double dZ = pos2.getZ() - pos1.getZ();

        return Math.sqrt(dX * dX + dY * dY + dZ * dZ);
    }

    public static double distanceXZ(Vec3d pos1, Vec3d pos2) {
        double dX = pos1.getX() - pos2.getX();
        double dZ = pos1.getZ() - pos2.getZ();

        return MathHelper.sqrt((float) (dX * dX + dZ * dZ));
    }

    public static double distanceXZ(double x1, double x2, double z1, double z2) {
        double dX = x1 - x2;
        double dZ = z1 - z2;

        return MathHelper.sqrt((float) (dX * dX + dZ * dZ));
    }

    public static double distanceY(Vec3d pos1, Vec3d pos2) {
        return Math.abs(pos2.y - pos1.y);
    }

    public static double distanceY(double y1, double y2) {
        return Math.abs(y1 - y2);
    }

    public static ArrayList<BlockPos> getPositionsAroundEntity(Entity entity, double range) {
        double pX = entity.getX() - 0.5;
        double pY = entity.getY();
        double pZ = entity.getZ() - 0.5;

        int minX = (int) Math.floor(pX - range);
        int minY = (int) Math.floor(pY - range);
        int minZ = (int) Math.floor(pZ - range);

        int maxX = (int) Math.floor(pX + range);
        int maxY = (int) Math.floor(pY + range);
        int maxZ = (int) Math.floor(pZ + range);

        double rangeSq = Math.pow(range, 2);

        ArrayList<BlockPos> positions = new ArrayList<>();

        for (int y = minY; y <= maxY; y++) {
            for (int x = minX; x <= maxX; x++) {
                for (int z = minZ; z <= maxZ; z++) {
                    if (CatHackEntityUtils.distance(pX, pY, pZ, x, y, z) <= rangeSq) {
                        BlockPos pos = new BlockPos(x, y, z);

                        if (World.isValid(pos)) positions.add(pos);
                    }
                }
            }
        }

        return positions;
    }

    public static double[] directionSpeed(float speed) {
        float forward = mc.player.input.movementForward;
        float side = mc.player.input.movementSideways;
        float yaw = mc.player.prevYaw + (mc.player.getYaw() - mc.player.prevYaw);

        if (forward != 0.0F) {
            if (side > 0.0F) {
                yaw += ((forward > 0.0F) ? -45 : 45);
            } else if (side < 0.0F) {
                yaw += ((forward > 0.0F) ? 45 : -45);
            }

            side = 0.0F;

            if (forward > 0.0F) {
                forward = 1.0F;
            } else if (forward < 0.0F) {
                forward = -1.0F;
            }
        }

        double sin = Math.sin(Math.toRadians(yaw + 90.0F));
        double cos = Math.cos(Math.toRadians(yaw + 90.0F));
        double dx = forward * speed * cos + side * speed * sin;
        double dz = forward * speed * sin - side * speed * cos;

        return new double[] { dx, dz };
    }

    public static boolean isAttackable(EntityType<?> type) {
        return type != EntityType.AREA_EFFECT_CLOUD && type != EntityType.ARROW && type != EntityType.FALLING_BLOCK && type != EntityType.FIREWORK_ROCKET && type != EntityType.ITEM && type != EntityType.LLAMA_SPIT && type != EntityType.SPECTRAL_ARROW && type != EntityType.ENDER_PEARL && type != EntityType.EXPERIENCE_BOTTLE && type != EntityType.POTION && type != EntityType.TRIDENT && type != EntityType.LIGHTNING_BOLT && type != EntityType.FISHING_BOBBER && type != EntityType.EXPERIENCE_ORB && type != EntityType.EGG;
    }

    public static float getTotalHealth(PlayerEntity target) {
        return target.getHealth() + target.getAbsorptionAmount();
    }

    public static int getPing(PlayerEntity player) {
        if (mc.getNetworkHandler() == null) return 0;

        PlayerListEntry playerListEntry = mc.getNetworkHandler().getPlayerListEntry(player.getUuid());
        if (playerListEntry == null) return 0;
        return playerListEntry.getLatency();
    }

    public static GameMode getGameMode(PlayerEntity player) {
        if (player == null) return null;
        PlayerListEntry playerListEntry = mc.getNetworkHandler().getPlayerListEntry(player.getUuid());
        if (playerListEntry == null) return null;
        return playerListEntry.getGameMode();
    }

    public static boolean isAboveWater(Entity entity) {
        BlockPos.Mutable blockPos = entity.getBlockPos().mutableCopy();

        for (int i = 0; i < 64; i++) {
            BlockState state = mc.world.getBlockState(blockPos);

            if (state.getMaterial().blocksMovement()) break;

            Fluid fluid = state.getFluidState().getFluid();
            if (fluid == Fluids.WATER || fluid == Fluids.FLOWING_WATER) {
                return true;
            }

            blockPos.move(0, -1, 0);
        }

        return false;
    }

    public static boolean isInRenderDistance(Entity entity) {
        if (entity == null) return false;
        return isInRenderDistance(entity.getX(), entity.getZ());
    }

    public static boolean isInRenderDistance(BlockEntity entity) {
        if (entity == null) return false;
        return isInRenderDistance(entity.getPos().getX(), entity.getPos().getZ());
    }

    public static boolean isInRenderDistance(BlockPos pos) {
        if (pos == null) return false;
        return isInRenderDistance(pos.getX(), pos.getZ());
    }

    public static boolean isInRenderDistance(double posX, double posZ) {
        double x = Math.abs(mc.gameRenderer.getCamera().getPos().x - posX);
        double z = Math.abs(mc.gameRenderer.getCamera().getPos().z - posZ);
        double d = (mc.options.viewDistance + 1) * 16;

        return x < d && z < d;
    }

    public static Direction rayTraceCheck(BlockPos pos, boolean forceReturn) {
        Vec3d eyesPos = new Vec3d(mc.player.getX(), mc.player.getY() + (double) mc.player.getEyeHeight(mc.player.getPose()), mc.player.getZ());
        Direction[] var3 = Direction.values();
        int var4 = var3.length;

        for (int var5 = 0; var5 < var4; ++var5) {
            Direction direction = var3[var5];
            RaycastContext raycastContext = new RaycastContext(eyesPos, new Vec3d((double) pos.getX() + 0.5D + (double) direction.getVector().getX() * 0.5D, (double) pos.getY() + 0.5D + (double) direction.getVector().getY() * 0.5D, (double) pos.getZ() + 0.5D + (double) direction.getVector().getZ() * 0.5D), RaycastContext.ShapeType.COLLIDER, RaycastContext.FluidHandling.NONE, mc.player);
            BlockHitResult result = mc.world.raycast(raycastContext);
            if (result != null && result.getType() == HitResult.Type.BLOCK && result.getBlockPos().equals(pos)) {
                return direction;
            }
        }

        if (forceReturn) {
            if ((double) pos.getY() > eyesPos.y) {
                return Direction.DOWN;
            } else {
                return Direction.UP;
            }
        } else {
            return null;
        }
    }

    public static int getBlockBreakingSpeed(BlockState block, BlockPos pos, int slot) {
        PlayerEntity player = mc.player;

        float f = (player.getInventory().getStack(slot)).getMiningSpeedMultiplier(block);
        if (f > 1.0F) {
            int i = EnchantmentHelper.get(player.getInventory().getStack(slot)).getOrDefault(Enchantments.EFFICIENCY, 0);
            if (i > 0) {
                f += (float) (i * i + 1);
            }
        }

        if (StatusEffectUtil.hasHaste(player)) {
            f *= 1.0F + (float) (StatusEffectUtil.getHasteAmplifier(player) + 1) * 0.2F;
        }

        if (player.hasStatusEffect(StatusEffects.MINING_FATIGUE)) {
            float k;
            switch (player.getStatusEffect(StatusEffects.MINING_FATIGUE).getAmplifier()) {
                case 0:
                    k = 0.3F;
                    break;
                case 1:
                    k = 0.09F;
                    break;
                case 2:
                    k = 0.0027F;
                    break;
                case 3:
                default:
                    k = 8.1E-4F;
            }

            f *= k;
        }

        if (player.isSubmergedIn(FluidTags.WATER) && !EnchantmentHelper.hasAquaAffinity(player)) {
            f /= 5.0F;
        }

        if (!player.isOnGround()) {
            f /= 5.0F;
        }

        float t = block.getHardness(mc.world, pos);
        if (t == -1.0F) {
            return 0;
        } else {
            return (int) Math.ceil(1 / (f / t / 30));
        }
    }

    public static Vec3d crystalEdgePos(EndCrystalEntity crystal) {
        Vec3d crystalPos = crystal.getPos();
        return new Vec3d(
                crystalPos.x < mc.player.getX() ? crystalPos.add(Math.min(1, mc.player.getX() - crystalPos.x), 0, 0).x : crystalPos.x > mc.player.getX() ? crystalPos.add(Math.max(-1, mc.player.getX() - crystalPos.x), 0, 0).x : crystalPos.x,
                crystalPos.y < mc.player.getY() ? crystalPos.add(0, Math.min(1, mc.player.getY() - crystalPos.y), 0).y : crystalPos.y,
                crystalPos.z < mc.player.getZ() ? crystalPos.add(0, 0, Math.min(1, mc.player.getZ() - crystalPos.z)).z : crystalPos.z > mc.player.getZ() ? crystalPos.add(0, 0, Math.max(-1, mc.player.getZ() - crystalPos.z)).z : crystalPos.z);
    }

    public static boolean isBedrock(BlockPos pos) {
        return mc.world.getBlockState(pos).isOf(Blocks.BEDROCK);
    }

    public static boolean isBlastResistant(BlockPos pos) {
        return mc.world.getBlockState(pos).getBlock().getBlastResistance() >= 600;
    }

    public static List<BlockPos> getSurroundBlocks(PlayerEntity player) {
        if (player == null) return null;

        List<BlockPos> positions = new ArrayList<>();

        for (Direction direction : Direction.values()) {
            if (direction == Direction.UP || direction == Direction.DOWN) continue;

            BlockPos pos = player.getBlockPos().offset(direction);

            if (!mc.world.getBlockState(pos).getBlock().equals(Blocks.BEDROCK) && isBlastResistant(pos)) {
                positions.add(pos);
            }
        }

        return positions;
    }

    public static BlockPos getCityBlock(PlayerEntity player) {
        List<BlockPos> posList = getSurroundBlocks(player);
        posList.sort(Comparator.comparingDouble(PlayerUtils::distanceTo));
        return posList.isEmpty() ? null : posList.get(0);
    }

    public static BlockPos getTargetBlock(PlayerEntity player) {
        BlockPos finalPos = null;

        List<BlockPos> positions = getSurroundBlocks(player);
        List<BlockPos> myPositions = getSurroundBlocks(mc.player);

        if (positions == null) return null;

        for (BlockPos pos : positions) {

            if (myPositions != null && !myPositions.isEmpty() && myPositions.contains(pos)) continue;

            if (finalPos == null) {
                finalPos = pos;
                continue;
            }

            if (mc.player.squaredDistanceTo(Utils.vec3d(pos)) < mc.player.squaredDistanceTo(Utils.vec3d(finalPos))) {
                finalPos = pos;
            }
        }

        return finalPos;
    }

    public static String getName(Entity entity) {
        if (entity == null) return null;
        if (entity instanceof PlayerEntity) return entity.getEntityName();
        return entity.getType().getName().getString();
    }

    public static boolean isTopTrapped(PlayerEntity target) {
        assert mc.world != null;
        return isBlastResistant(target.getBlockPos().add(0, 2, 0));
    }

    public static boolean isFaceSurrounded(PlayerEntity target) {
        assert mc.world != null;
        return isBlastResistant(target.getBlockPos().add(1, 1, 0))
                && isBlastResistant(target.getBlockPos().add(-1, 1, 0))
                && isBlastResistant(target.getBlockPos().add(0, 1, 1))
                && isBlastResistant(target.getBlockPos().add(0, 1, -1));
    }

    public static boolean isGreenHole(PlayerEntity target) {
        assert mc.world != null;
        return mc.world.getBlockState(target.getBlockPos().add(1, 0, 0)).isOf(Blocks.BEDROCK)
                && mc.world.getBlockState(target.getBlockPos().add(-1, 0, 0)).isOf(Blocks.BEDROCK)
                && mc.world.getBlockState(target.getBlockPos().add(0, 0, 1)).isOf(Blocks.BEDROCK)
                && mc.world.getBlockState(target.getBlockPos().add(0, 0, -1)).isOf(Blocks.BEDROCK);
    }

    public static boolean NotBedrockSurrounded(PlayerEntity player) {
        assert mc.world != null;
        return !mc.world.getBlockState(mc.player.getBlockPos().add(1, 0, 0)).isAir()
                && !mc.world.getBlockState(mc.player.getBlockPos().add(1, 0, 0)).isOf(Blocks.BEDROCK)
                && !mc.world.getBlockState(mc.player.getBlockPos().add(-1, 0, 0)).isAir()
                && !mc.world.getBlockState(mc.player.getBlockPos().add(-1, 0, 0)).isOf(Blocks.BEDROCK)
                && !mc.world.getBlockState(mc.player.getBlockPos().add(0, 0, 1)).isAir()
                && !mc.world.getBlockState(mc.player.getBlockPos().add(0, 0, 1)).isOf(Blocks.BEDROCK)
                && !mc.world.getBlockState(mc.player.getBlockPos().add(0, 0, -1)).isAir()
                && !mc.world.getBlockState(mc.player.getBlockPos().add(0, 0, -1)).isOf(Blocks.BEDROCK);
    }

    public static boolean isSurrounded(PlayerEntity targetEntity) {
        assert mc.world != null;
        return isBlastResistant(targetEntity.getBlockPos().add(1, 0, 0))
                && isBlastResistant(targetEntity.getBlockPos().add(-1, 0, 0))
                && isBlastResistant(targetEntity.getBlockPos().add(0, 0, 1))
                && isBlastResistant(targetEntity.getBlockPos().add(0, 0, -1));
    }

    public static boolean isSurroundBroken(PlayerEntity targetEntity) {
        assert mc.world != null;
        return (!isBlastResistant(targetEntity.getBlockPos().add(1, 0, 0))
                && isBlastResistant(targetEntity.getBlockPos().add(-1, 0, 0))
                && isBlastResistant(targetEntity.getBlockPos().add(0, 0, 1))
                && isBlastResistant(targetEntity.getBlockPos().add(0, 0, -1)))

                || (isBlastResistant(targetEntity.getBlockPos().add(1, 0, 0))
                && !isBlastResistant(targetEntity.getBlockPos().add(-1, 0, 0))
                && isBlastResistant(targetEntity.getBlockPos().add(0, 0, 1))
                && isBlastResistant(targetEntity.getBlockPos().add(0, 0, -1)))

                || (isBlastResistant(targetEntity.getBlockPos().add(1, 0, 0))
                && isBlastResistant(targetEntity.getBlockPos().add(-1, 0, 0))
                && !isBlastResistant(targetEntity.getBlockPos().add(0, 0, 1))
                && isBlastResistant(targetEntity.getBlockPos().add(0, 0, -1)))

                || (isBlastResistant(targetEntity.getBlockPos().add(1, 0, 0))
                && isBlastResistant(targetEntity.getBlockPos().add(-1, 0, 0))
                && isBlastResistant(targetEntity.getBlockPos().add(0, 0, 1))
                && !isBlastResistant(targetEntity.getBlockPos().add(0, 0, -1)));
    }

    public static boolean isBurrowed(PlayerEntity targetEntity) {
        assert mc.world != null;
        return isBlastResistant(targetEntity.getBlockPos());
    }

    public static boolean isWebbed(PlayerEntity targetEntity) {
        assert mc.world != null;
        return mc.world.getBlockState(targetEntity.getBlockPos()).isOf(Blocks.COBWEB);
    }

    public static boolean isInHole(boolean doubles) {
        if (!Utils.canUpdate()) return false;

        BlockPos blockPos = mc.player.getBlockPos();
        int air = 0;

        for (Direction direction : Direction.values()) {
            if (direction == Direction.UP) continue;

            BlockState state = mc.world.getBlockState(blockPos.offset(direction));

            if (state.getBlock() != Blocks.BEDROCK && state.getBlock() != Blocks.OBSIDIAN
                    && state.getBlock() != Blocks.RESPAWN_ANCHOR && state.getBlock() != Blocks.NETHERITE_BLOCK
                    && state.getBlock() != Blocks.CRYING_OBSIDIAN && state.getBlock() != Blocks.ENDER_CHEST
                    && state.getBlock() != Blocks.ANCIENT_DEBRIS && state.getBlock() != Blocks.ANVIL
                    && state.getBlock() != Blocks.CHIPPED_ANVIL && state.getBlock() != Blocks.DAMAGED_ANVIL) {
                if (!doubles || direction == Direction.DOWN) return false;

                air++;

                for (Direction dir : Direction.values()) {
                    if (dir == direction.getOpposite() || dir == Direction.UP) continue;

                    BlockState blockState1 = mc.world.getBlockState(blockPos.offset(direction).offset(dir));

                    if (blockState1.getBlock() != Blocks.BEDROCK && blockState1.getBlock() != Blocks.OBSIDIAN
                            && blockState1.getBlock() != Blocks.RESPAWN_ANCHOR && blockState1.getBlock() != Blocks.NETHERITE_BLOCK
                            && blockState1.getBlock() != Blocks.CRYING_OBSIDIAN && blockState1.getBlock() != Blocks.ENDER_CHEST
                            && blockState1.getBlock() != Blocks.ANCIENT_DEBRIS && blockState1.getBlock() != Blocks.ANVIL
                            && blockState1.getBlock() != Blocks.CHIPPED_ANVIL && blockState1.getBlock() != Blocks.DAMAGED_ANVIL) {
                        return false;
                    }
                }
            }
        }

        return air < 2;
    }
}

