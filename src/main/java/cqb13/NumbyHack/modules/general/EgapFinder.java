package cqb13.NumbyHack.modules.general;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;

import cqb13.NumbyHack.NumbyHack;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.settings.BoolSetting;
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
 * Original from Tanuki: https://gitlab.com/Walaryne/tanuki
 */
public class EgapFinder extends Module {
    private static final String OUTPUT_FILE = "egap-coords.txt";
    private static final int COMPARATOR_DELAY_TICKS = 3;
    private static final int NO_CHEST_TICK_THRESHOLD = 2;
    private static final int CHUNK_SEARCHED_THRESHOLD = 10 * 20;
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
        .build()
    );

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

    public EgapFinder() {
        super(NumbyHack.CATEGORY, "egap-finder",
            "Finds Enchanted Golden Apples in chests and logs coordinates to " + OUTPUT_FILE);
    }

    @Override
    public void onActivate() {
        resetState();
    }

    @EventHandler
    private void onTick(TickEvent.Pre event) {
        if (mc.world == null || mc.player == null) {
            debug("Tick skipped: world or player is null");
            return;
        }

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

        if (ticksWithoutChest >= NO_CHEST_TICK_THRESHOLD && ticksWithoutChest < CHUNK_SEARCHED_THRESHOLD) {
            if (currentState != ProcessState.IDLE) {
                debug("NO_CHEST_TICK_THRESHOLD reached (" + ticksWithoutChest + " ticks), resetting state");
                resetState();
            }
            return;
        }

        if (ticksWithoutChest >= CHUNK_SEARCHED_THRESHOLD && autoSearch.get()) {
            debug("CHUNK_SEARCHED_THRESHOLD reached (" + ticksWithoutChest + " ticks), looking for new area");
            tpToNewSearchArea();
            resetState();
            ticksWithoutChest = 0;
        }

        if (isProcessingChest) {
            return;
        }

        if (currentChestPos == null) {
            return;
        }

        processCurrentState();
    }

    private void tpToNewSearchArea() {
        BlockPos positionToTeleportTo = this.calculateNewSearchArea(mc.player.getBlockPos());
        String command = "/tp %d %d %d";

        ChatUtils.info(Formatting.GREEN +
            String.format("Teleporting to new search area with the center at (%d, %d, %d)!",
                positionToTeleportTo.getX(), positionToTeleportTo.getY(), positionToTeleportTo.getZ()
            )
        );

        ChatUtils.sendPlayerMsg(String.format(command, positionToTeleportTo.getX(), positionToTeleportTo.getY(), positionToTeleportTo.getZ()));
    }

    private BlockPos calculateNewSearchArea(BlockPos currentSearchArea) {
        int blocksInRange = 16 * renderDistance.get();
        return new BlockPos(currentSearchArea.getX() + (2 * blocksInRange), currentSearchArea.getY(), currentSearchArea.getZ());
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
            currentChestPos.getX(), currentChestPos.getY(), currentChestPos.getZ()
        );

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
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(OUTPUT_FILE, true))) {
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
}
