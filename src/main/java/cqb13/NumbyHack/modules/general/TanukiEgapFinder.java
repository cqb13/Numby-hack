package cqb13.NumbyHack.modules.general;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Set;

import cqb13.NumbyHack.NumbyHack;
import meteordevelopment.meteorclient.events.world.ChunkDataEvent;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.settings.BoolSetting;
import meteordevelopment.meteorclient.settings.DoubleSetting;
import meteordevelopment.meteorclient.settings.IntSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.utils.Utils;
import meteordevelopment.meteorclient.utils.player.ChatUtils;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.ChestBlockEntity;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.BlockPos;

/**
 * Original from Tanuki:
 * https://gitlab.com/Walaryne/tanuki/-/blob/master/src/main/java/minegame159/meteorclient/modules/misc/EgapFinder.java
 */
public class TanukiEgapFinder extends Module {
    private static final String OUTPUT_FILE_NAME = "egap-coords";

    private static final int COMPARATOR_DELAY_TICKS = 3;
    private static final boolean DEBUG = true;

    private final SettingGroup sgDefault = settings.getDefaultGroup();
    private final SettingGroup sgAutoSearch = settings.createGroup("Auto-Search");

    private final Setting<Boolean> playSound = sgDefault.add(new BoolSetting.Builder()
            .name("play-sound")
            .description("Plays a sound when you find an egap.")
            .defaultValue(false)
            .build());

    private final Setting<Boolean> autoSearch = sgAutoSearch.add(new BoolSetting.Builder()
            .name("auto-search")
            .description("Teleports you to a new area to be scanned when the current area is already completed.")
            .defaultValue(false)
            .build());

    private final Setting<Integer> renderDistance = sgAutoSearch.add(new IntSetting.Builder()
            .name("render-distance")
            .description("The render distance in your settings, this is used to calculate the position to tp to.")
            .defaultValue(16)
            .min(5)
            .sliderMax(32)
            .visible(autoSearch::get)
            .build());

    private final Setting<Double> noChestTimeout = sgAutoSearch.add(new DoubleSetting.Builder()
            .name("no-chest-timeout")
            .description("Time in seconds where no chest is found and the area is deemed as looted.")
            .defaultValue(2.5)
            .min(1)
            .sliderMax(10)
            .visible(autoSearch::get)
            .decimalPlaces(1)
            .build());

    private final Setting<Double> chunkStableTime = sgAutoSearch.add(new DoubleSetting.Builder()
            .name("chunk-stable-time")
            .description("Time in seconds no new chunks are loading before moving on.")
            .defaultValue(2.5)
            .min(1)
            .sliderMax(10)
            .visible(autoSearch::get)
            .decimalPlaces(1)
            .build());

    private final Setting<Double> teleportCooldown = sgAutoSearch.add(new DoubleSetting.Builder()
            .name("teleport-cooldown")
            .description("Minimum time in seconds to wait after teleporting before moving again.")
            .defaultValue(5)
            .min(1)
            .sliderMax(20)
            .visible(autoSearch::get)
            .decimalPlaces(1)
            .build());

    private enum ProcessState {
        IDLE,
        PLACE_COMPARATOR,
        PLACE_LEAVES,
        CHECK_FOR_EGAP,
        VERIFY_RESULT
    }

    private ProcessState currentState = ProcessState.IDLE;
    private boolean isProcessingChest = false;
    private int ticksWithoutChest = 0;
    private int comparatorDelayCounter = 0;
    private BlockPos currentChestPos = null;
    private BlockPos lastCheckedChestPos = null;
    private SpiralTraversal spiralTraversal;
    private int ticksSinceLastChunkData = 0;
    private int ticksSinceTeleport = 0;

    public TanukiEgapFinder() {
        super(NumbyHack.CATEGORY, "egap-finder",
                "Finds Enchanted Golden Apples in chests and logs coordinates to " + OUTPUT_FILE_NAME
                        + "-yourworldseed.txt");
    }

    @Override
    public void onActivate() {
        BlockPos playerPos = mc.player.getBlockPos();
        spiralTraversal = new SpiralTraversal(playerPos.getX(), playerPos.getZ());
        resetState();
    }

    @EventHandler
    private void onChunkData(ChunkDataEvent event) {
        ticksSinceLastChunkData = 0;
    }

    @EventHandler
    private void onTick(TickEvent.Pre event) {
        if (mc.world == null || mc.player == null) {
            debug("Tick skipped: world or player is null");
            return;
        }

        ticksSinceLastChunkData++;
        ticksSinceTeleport++;

        BlockPos foundChest = findNearestChest();

        if (foundChest != null) {
            if (currentChestPos == null || !currentChestPos.equals(foundChest)) {
                debug("Found new chest at: " + formatPos(foundChest));
            }
            currentChestPos = foundChest;
            ticksWithoutChest = 0;
            isProcessingChest = false;
        } else {
            ticksWithoutChest++;
        }

        if (autoSearch.get()
                && ticksWithoutChest >= noChestTimeout.get() * 20
                && ticksSinceLastChunkData >= chunkStableTime.get() * 20
                && ticksSinceTeleport >= teleportCooldown.get() * 20) {
            debug("Chunks stable (" + ticksSinceLastChunkData
                    + " ticks) and minimum wait satisfied, moving to new area");
            tpToNewSearchArea();
            resetState();
            ticksWithoutChest = 0;
            ticksSinceLastChunkData = 0;
            ticksSinceTeleport = 0;
            return;
        }

        if (isProcessingChest || currentChestPos == null) {
            return;
        }

        processCurrentState();
    }

    private void tpToNewSearchArea() {
        XZPos positionToTeleportTo = spiralTraversal.next();
        String command = "/tp %d ~ %d";

        ChatUtils.info(Formatting.GREEN +
                String.format("Teleporting to new search area with the center at (%d, ~, %d)!",
                        positionToTeleportTo.x(), positionToTeleportTo.z()));

        ChatUtils.sendPlayerMsg(String.format(command, positionToTeleportTo.x(), positionToTeleportTo.z()));
        ticksSinceTeleport = 0;
    }

    private BlockPos findNearestChest() {
        for (BlockEntity blockEntity : Utils.blockEntities()) {
            if (blockEntity instanceof ChestBlockEntity && !blockEntity.isRemoved()) {
                return blockEntity.getPos();
            }
        }

        return null;
    }

    private void processCurrentState() {
        debug("Processing state: " + currentState);

        switch (currentState) {
            case IDLE:
                currentState = ProcessState.PLACE_COMPARATOR;
                break;

            case PLACE_COMPARATOR:
                placeComparator();
                currentState = ProcessState.PLACE_LEAVES;
                break;

            case PLACE_LEAVES:
                placeLeavesAndWaitForComparatorUpdate();
                break;

            case CHECK_FOR_EGAP:
                checkForEgap();
                currentState = ProcessState.VERIFY_RESULT;
                break;

            case VERIFY_RESULT:
                verifyAndCleanup();
                currentState = ProcessState.IDLE;
                break;
        }
    }

    private void placeComparator() {
        BlockPos comparatorPos = currentChestPos.add(-1, 0, 0);
        Block existingBlock = mc.world.getBlockState(comparatorPos).getBlock();

        if (existingBlock != Blocks.COMPARATOR) {
            String command = String.format("/setblock %d %d %d minecraft:comparator[facing=east]",
                    comparatorPos.getX(), comparatorPos.getY(), comparatorPos.getZ());
            ChatUtils.sendPlayerMsg(command);
        }
    }

    private void placeLeavesAndWaitForComparatorUpdate() {
        BlockPos leavesPos = currentChestPos.add(-1, 1, 0);
        Block existingBlock = mc.world.getBlockState(leavesPos).getBlock();

        if (existingBlock != Blocks.ACACIA_LEAVES) {
            String command = String.format("/setblock %d %d %d minecraft:acacia_leaves",
                    leavesPos.getX(), leavesPos.getY(), leavesPos.getZ());
            ChatUtils.sendPlayerMsg(command);
        }

        comparatorDelayCounter++;

        // Wait for comparator to update before checking
        if (comparatorDelayCounter >= COMPARATOR_DELAY_TICKS) {
            currentState = ProcessState.CHECK_FOR_EGAP;
            comparatorDelayCounter = 0;
        }
    }

    private void checkForEgap() {
        Block block = mc.world.getBlockState(currentChestPos).getBlock();

        if (block != Blocks.CHEST) {
            return;
        }

        String command = String.format(
                "/execute if data block %d %d %d Items[{id:\"minecraft:enchanted_golden_apple\"}] " +
                        "as @p run setblock %d %d %d minecraft:diamond_block",
                currentChestPos.getX(), currentChestPos.getY(), currentChestPos.getZ(),
                currentChestPos.getX(), currentChestPos.getY(), currentChestPos.getZ());

        ChatUtils.sendPlayerMsg(command);

        lastCheckedChestPos = currentChestPos;
    }

    private void verifyAndCleanup() {
        if (lastCheckedChestPos == null) {
            return;
        }

        Block resultBlock = mc.world.getBlockState(lastCheckedChestPos).getBlock();

        if (resultBlock == Blocks.DIAMOND_BLOCK) {
            handleEgapFound(lastCheckedChestPos);
        } else {
            String command = String.format("/setblock %d %d %d minecraft:air",
                    lastCheckedChestPos.getX(), lastCheckedChestPos.getY(), lastCheckedChestPos.getZ());
            ChatUtils.sendPlayerMsg(command);
        }
    }

    private void handleEgapFound(BlockPos pos) {
        String coords = String.format("%d %d %d", pos.getX(), pos.getY(), pos.getZ());

        if (playSound.get() && mc.player != null) {
            mc.player.playSound(SoundEvents.ENTITY_EXPERIENCE_ORB_PICKUP, 1.0f, 1.0f);
        }

        ChatUtils.info(Formatting.GREEN + "Found an egap (" + coords + ")!");

        if (!writeCoordinatesToFile(coords)) {
            ChatUtils.error("Failed to write coordinates to file!");
        } else {
        }
    }

    private boolean writeCoordinatesToFile(String coords) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(getOutputFileName(), true))) {
            writer.write(coords);
            writer.newLine();
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    private void resetState() {
        isProcessingChest = true;
        currentState = ProcessState.IDLE;
        comparatorDelayCounter = 0;
        currentChestPos = null;
        lastCheckedChestPos = null;
    }

    private void debug(String message) {
        if (DEBUG) {
            System.out.println("[EgapFinder] " + message);
        }
    }

    private String formatPos(BlockPos pos) {
        return String.format("(%d, %d, %d)", pos.getX(), pos.getY(), pos.getZ());
    }

    private class SpiralTraversal {
        private final Set<XZPos> visited = new HashSet<>();
        private final Set<XZPos> queued = new HashSet<>();
        private final Queue<XZPos> queue = new LinkedList<>();

        public SpiralTraversal(int originX, int originZ) {
            XZPos origin = new XZPos(originX, originZ);

            visited.add(origin);
            updateQueue(origin);
        }

        public XZPos next() {
            XZPos next = queue.remove();

            queued.remove(next);
            visited.add(next);
            updateQueue(next);

            return next;
        }

        private void updateQueue(XZPos position) {
            int blockOffset = 2 * 16 * renderDistance.get();
            Set<XZPos> neighbors = new HashSet<>();

            neighbors.add(new XZPos(position.x + blockOffset, position.z));
            neighbors.add(new XZPos(position.x, position.z - blockOffset));
            neighbors.add(new XZPos(position.x - blockOffset, position.z));
            neighbors.add(new XZPos(position.x, position.z + blockOffset));

            for (XZPos neighbor : neighbors) {
                if (visited.contains(neighbor) || queued.contains(neighbor))
                    continue;

                queue.add(neighbor);
                queued.add(neighbor);
            }
        }
    }

    record XZPos(int x, int z) {
    }

    private String getOutputFileName() {
        if (mc.world == null) {
            return OUTPUT_FILE_NAME + ".txt";
        }

        long seed = getWorldSeed();

        // If negative turn - into n
        String seedStr = seed < 0 ? "n" + Math.abs(seed) : String.valueOf(seed);
        return String.format("%s-%s.txt", OUTPUT_FILE_NAME, seedStr);
    }

    private Long getWorldSeed() {
        if (mc.getServer() != null) {
            var worldProperties = mc.getServer().getSaveProperties();
            if (worldProperties != null) {
                return worldProperties.getGeneratorOptions().getSeed();
            }
        }
        return null;
    }
}
