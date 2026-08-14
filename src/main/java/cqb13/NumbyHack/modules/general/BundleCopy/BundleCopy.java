package cqb13.NumbyHack.modules.general.BundleCopy;

import static org.lwjgl.glfw.GLFW.GLFW_KEY_C;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.TreeMap;
import java.util.function.Predicate;
import java.util.stream.IntStream;

import org.apache.commons.lang3.math.Fraction;

import cqb13.NumbyHack.NumbyHack;
import meteordevelopment.meteorclient.events.meteor.KeyInputEvent;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.settings.IntSetting;
import meteordevelopment.meteorclient.settings.KeybindSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.utils.misc.Keybind;
import meteordevelopment.meteorclient.utils.misc.input.KeyAction;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.inventory.ContainerInput;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.BundleContents;

/**
 * Heavily inspired by map copy from Ev Mod
 *
 * https://github.com/EvModder/EvMod
 */
public class BundleCopy extends Module {
    private final SettingGroup sgGeneral = settings.getDefaultGroup();

    private final Setting<Integer> actionDelay = sgGeneral.add(new IntSetting.Builder()
            .name("action-delay-ms")
            .description("Delay between clicks in milliseconds.")
            .defaultValue(50)
            .sliderMin(0)
            .sliderMax(100)
            .min(0)
            .max(500)
            .build());

    private final Setting<Keybind> copyKey = sgGeneral.add(new KeybindSetting.Builder()
            .name("copy-key")
            .description("Starts copying or cancels copying.")
            .defaultValue(Keybind.fromKey(GLFW_KEY_C))
            .build());

    private long lastCopy;
    private final long copyCooldown = 250L;

    private static final int HOTBAR_END = 44;

    private final ArrayDeque<InvAction> clickQueue = new ArrayDeque<>();
    private boolean processing = false;
    private int ticksWaited = 0;

    private InvAction pendingAction = null;
    private int pendingTicks = 0;

    public BundleCopy() {
        super(NumbyHack.CATEGORY, "bundle-copy",
                "Copies maps in bundles, have 2 empty bundles and a bundle with maps in your inventory then press the copy key.");
    }

    private static final InventoryLayout LAYOUT = new InventoryLayout(0, 1, 9, 36, 45);
    private static final int MAP_INPUT_SLOT = LAYOUT.inputStart + 1;

    @Override
    public void onActivate() {
        clickQueue.clear();
        processing = false;
        ticksWaited = 0;
    }

    @Override
    public void onDeactivate() {
        clickQueue.clear();
        processing = false;
        ticksWaited = 0;
    }

    @EventHandler
    private void onKeyPress(KeyInputEvent event) {
        if (event.action != KeyAction.Press) {
            return;
        }

        if (mc.gui.screen() == null) {
            return;
        }

        if (event.key() != copyKey.get().getValue()) {
            return;
        }

        if (!clickQueue.isEmpty()) {
            clickQueue.clear();
            processing = false;
            ticksWaited = 0;
            info("Cancelled");
            return;
        }

        if (!(mc.gui.screen() instanceof InventoryScreen)) {
            return;
        }

        if (System.currentTimeMillis() - lastCopy < copyCooldown) {
            return;
        }

        lastCopy = System.currentTimeMillis();
        buildQueue();
    }

    @EventHandler
    private void onTick(TickEvent.Post event) {
        int delayTicks = Math.max(1, (int) Math.ceil(actionDelay.get() / 50.0));
        if (ticksWaited < delayTicks) {
            ticksWaited++;
            return;
        }
        ticksWaited = 0;

        if (pendingAction != null) {
            pendingTicks++;
            int pendingThreshold = Math.max(1, (int) Math.ceil(actionDelay.get() / 50.0));
            if (pendingTicks >= pendingThreshold) {
                pendingAction = null;
                pendingTicks = 0;
            }
            return;
        }

        if (clickQueue.isEmpty()) {
            if (processing) {
                processing = false;
                info("Done");
            }
            return;
        }

        InvAction action = clickQueue.poll();
        executeClick(action);
        pendingAction = action;
        pendingTicks = 0;
    }

    private void buildQueue() {
        final ItemStack[] slots = new ItemStack[HOTBAR_END + 1];
        for (int i = 0; i <= HOTBAR_END; ++i) {
            slots[i] = mc.player.containerMenu.getSlot(i).getItem();
        }

        // Count empty maps in grid
        int numEmptyMapsInGrid = 0;
        ItemStack in = slots[MAP_INPUT_SLOT];
        if (!in.isEmpty() && in.getItem() == Items.MAP) {
            numEmptyMapsInGrid = in.getCount();
        }

        int lastEmptyMapSlot = -1;
        for (int i = HOTBAR_END; i >= LAYOUT.inventoryStart; --i) {
            if (slots[i].getItem() == Items.MAP) {
                lastEmptyMapSlot = i;
                break;
            }
        }

        if (lastEmptyMapSlot == -1) {
            error("No empty maps found");
            return;
        }

        final int totalEmptyMaps = IntStream.rangeClosed(LAYOUT.inventoryStart, lastEmptyMapSlot)
                .filter(i -> slots[i].getItem() == Items.MAP).map(i -> slots[i].getCount()).sum();

        ArrayDeque<InvAction> clicks = new ArrayDeque<>();
        copyMapArtInBundles(clicks, slots, LAYOUT, numEmptyMapsInGrid, totalEmptyMaps);

        if (clicks.isEmpty()) {
            error("Nothing to copy");
            return;
        }

        clickQueue.addAll(clicks);
        processing = true;
        info("Copying maps...");
    }

    private void executeClick(InvAction a) {
        if (mc.player == null || mc.gameMode == null) {
            return;
        }

        try {
            int syncId = 0;

            switch (a.type) {
                case CLICK -> mc.gameMode.handleContainerInput(syncId, a.slot, a.button,
                        ContainerInput.PICKUP, mc.player);
                case SHIFT_CLICK -> mc.gameMode.handleContainerInput(syncId, a.slot, a.button,
                        ContainerInput.QUICK_MOVE, mc.player);
                case HOTBAR_SWAP -> mc.gameMode.handleContainerInput(syncId, a.slot, a.button,
                        ContainerInput.SWAP, mc.player);
                case BUNDLE_SELECT -> {
                    mc.gameMode.handleContainerInput(syncId, a.slot, a.button, ContainerInput.PICKUP, mc.player);
                }
            }
        } catch (Exception e) {
            error("Click error: " + e.getMessage());
        }
    }

    private int getEmptyMapsIntoInput(final ArrayDeque<InvAction> clicks, final ItemStack[] slots,
            final InventoryLayout f, final int amtNeeded, int amtInGrid, final int dontLeaveEmptySlotsAfterThisSlot) {
        for (int slot = f.inventoryStart; slot < f.hotbarEnd && amtInGrid < amtNeeded; ++slot) {
            if (slots[slot].getItem() != Items.MAP) {
                continue;
            }

            final boolean leaveOne = slot > dontLeaveEmptySlotsAfterThisSlot;
            if (leaveOne && slots[slot].getCount() == 1) {
                continue;
            }

            final int combinedCnt = slots[slot].getCount() + amtInGrid;
            final int combinedHalfCnt = (slots[slot].getCount() + 1) / 2 + amtInGrid;

            if (slot >= f.hotbarStart && (!leaveOne || amtInGrid > 0) && slots[slot].getCount() >= amtNeeded) {
                clicks.add(new InvAction(f.inputStart + 1, slot - f.hotbarStart, InvAction.ActionType.HOTBAR_SWAP));
                amtInGrid = slots[slot].getCount();
                slots[slot].setCount(combinedCnt - amtInGrid);
                break;
            } else if (combinedCnt <= 64) {
                clicks.add(new InvAction(slot, 0, InvAction.ActionType.CLICK));
                clicks.add(new InvAction(f.inputStart + 1, 0, InvAction.ActionType.CLICK));
                slots[slot] = ItemStack.EMPTY;
                amtInGrid = combinedCnt;
            } else if (slots[slot].getCount() > 1 && combinedHalfCnt >= amtNeeded && combinedHalfCnt <= 64) {
                clicks.add(new InvAction(slot, 1, InvAction.ActionType.CLICK));
                clicks.add(new InvAction(f.inputStart + 1, 0, InvAction.ActionType.CLICK));
                slots[slot].setCount(slots[slot].getCount() / 2);
                amtInGrid = combinedHalfCnt;
            } else {
                clicks.add(new InvAction(slot, 0, InvAction.ActionType.CLICK));
                clicks.add(new InvAction(f.inputStart + 1, 0, InvAction.ActionType.CLICK));
                clicks.add(new InvAction(slot, 0, InvAction.ActionType.CLICK));
                slots[slot].setCount(combinedCnt - 64);
                amtInGrid = 64;
            }
        }

        return amtInGrid;
    }

    private int lastEmptySlot(ItemStack[] slots, final int END, final int START) {
        for (int i = END - 1; i >= START; --i) {
            if (slots[i].isEmpty()) {
                return i;
            }
        }

        return -1;
    }

    private final int getNumStored(Fraction fraction) {
        assert 64 % fraction.getDenominator() == 0;
        return (64 / fraction.getDenominator()) * fraction.getNumerator();
    }

    private final String getCustomNameOrNull(ItemStack stack) {
        final var text = stack.get(DataComponents.CUSTOM_NAME);
        return text == null ? null : text.getString();
    }

    private BundleScan scanBundles(ItemStack[] slots, InventoryLayout f) {
        int[] bundleSlots = IntStream.range(f.inventoryStart, f.hotbarEnd).filter(i -> {
            BundleContents contents = slots[i].get(DataComponents.BUNDLE_CONTENTS);
            if (contents == null)
                return false;
            for (var t : contents.items()) {
                if (t.item().value() != Items.FILLED_MAP)
                    return false;
            }
            return true;
        }).toArray();

        BundleContents[] contents = Arrays.stream(bundleSlots)
                .mapToObj(i -> slots[i].get(DataComponents.BUNDLE_CONTENTS))
                .toArray(BundleContents[]::new);

        int sourceCount = (int) Arrays.stream(contents).filter(Predicate.not(BundleContents::isEmpty)).count();
        int usableEmptyCount = contents.length - sourceCount - 1;
        return new BundleScan(bundleSlots, contents, sourceCount, usableEmptyCount);
    }

    private PlanResult planDestinations(ItemStack[] slots, InventoryLayout f,
            int[] slotsWithBundles, BundleContents[] bundles,
            int sourceCount, int usableEmptyCount, int destsPerSource) {
        TreeMap<Integer, List<Integer>> bundlesToCopy = new TreeMap<>();
        HashSet<Integer> usedDests = new HashSet<>();
        HashSet<Item> srcBundleTypes = new HashSet<>();
        HashSet<Item> dstBundleTypes = new HashSet<>();
        for (int i = 0; i < slotsWithBundles.length; ++i) {
            final int s1 = slotsWithBundles[i];
            if (bundles[i].isEmpty()) {
                continue;
            }

            srcBundleTypes.add(slots[s1].getItem());
            ArrayList<Integer> copyDests = new ArrayList<>();
            final String name1 = getCustomNameOrNull(slots[s1]);
            if (name1 != null) {
                for (int j = 0; j < slotsWithBundles.length && usedDests.size() < usableEmptyCount
                        && copyDests.size() < destsPerSource; ++j) {
                    if (bundles[j].isEmpty() && name1.equals(getCustomNameOrNull(slots[slotsWithBundles[j]]))
                            && usedDests.add(j)) {
                        copyDests.add(j);
                    }
                }
            }

            if (copyDests.isEmpty() && slots[s1].getItem() != Items.BUNDLE) {
                for (int j = 0; j < slotsWithBundles.length && usedDests.size() < usableEmptyCount
                        && copyDests.size() < destsPerSource; ++j) {
                    if (bundles[j].isEmpty() && slots[s1].getItem() == slots[slotsWithBundles[j]].getItem()
                            && (name1 == null || getCustomNameOrNull(slots[slotsWithBundles[j]]) == null)
                            && usedDests.add(j)) {
                        copyDests.add(j);
                    }
                }
            }

            if (copyDests.isEmpty()) {
                for (int j = 0; j < slotsWithBundles.length && usedDests.size() < usableEmptyCount
                        && copyDests.size() < destsPerSource; ++j) {
                    final int s2 = slotsWithBundles[j];
                    if (!bundles[j].isEmpty()) {
                        continue;
                    }
                    if (name1 != null && getCustomNameOrNull(slots[s2]) != null
                            && !name1.equals(getCustomNameOrNull(slots[s2]))) {
                        continue;
                    }
                    if (sourceCount != usableEmptyCount && slots[s1].getItem() != slots[s2].getItem()) {
                        continue;
                    }
                    if (!usedDests.add(j)) {
                        continue;
                    }

                    copyDests.add(j);
                }
            }

            if (copyDests.isEmpty()) {
                error("Could not determine destination bundles");
                return null;
            }

            for (int j : copyDests) {
                dstBundleTypes.add(slots[slotsWithBundles[j]].getItem());
            }

            bundlesToCopy.put(i, copyDests);
        }

        int tempBundleSlot;
        HashSet<Integer> unusedBundles = new HashSet<>();
        for (int i = 0; i < slotsWithBundles.length; ++i) {
            unusedBundles.add(i);
        }

        for (var e : bundlesToCopy.entrySet()) {
            unusedBundles.remove(e.getKey());
            unusedBundles.removeAll(e.getValue());
        }

        int[] unusedBundleSlots = unusedBundles.stream().mapToInt(i -> slotsWithBundles[i]).toArray();
        boolean anyUnnamedDst = bundlesToCopy.values().stream()
                .anyMatch(d -> d.stream()
                        .anyMatch(i -> slots[slotsWithBundles[i]].get(DataComponents.ITEM_NAME) == null));

        PrioAndSlot pas = Arrays.stream(unusedBundleSlots)
                .mapToObj(i -> new PrioAndSlot(
                        ((!anyUnnamedDst && slots[i].get(DataComponents.ITEM_NAME) != null ? 4 : 0)
                                + (srcBundleTypes.contains(slots[i].getItem()) ? 2 : 0)
                                + (dstBundleTypes.contains(slots[i].getItem()) ? 1 : 0)),
                        i))
                .min(Comparator.naturalOrder()).get();

        tempBundleSlot = pas.slot;
        return new PlanResult(bundlesToCopy, tempBundleSlot);
    }

    private void copyMapArtInBundles(final ArrayDeque<InvAction> clicks, final ItemStack[] slots,
            final InventoryLayout f,
            int numEmptyMapsInGrid, final int totalEmptyMaps) {
        BundleScan scan = scanBundles(slots, f);
        if (scan.usableEmptyCount <= 0) {
            error("Could not find usable empty bundle");
            return;
        }

        int[] slotsWithBundles = scan.slots;
        BundleContents[] bundles = scan.contents;
        int SRC_BUNDLES = scan.sourceCount;
        int USABLE_EMPTY_BUNDLES = scan.usableEmptyCount;
        int LAST_EMPTY_SLOT = lastEmptySlot(slots, f.hotbarEnd, f.inventoryStart);
        int destsPerSource = SRC_BUNDLES > USABLE_EMPTY_BUNDLES ? 999 : USABLE_EMPTY_BUNDLES / SRC_BUNDLES;

        PlanResult plan = planDestinations(slots, f, slotsWithBundles, bundles,
                SRC_BUNDLES, USABLE_EMPTY_BUNDLES, destsPerSource);
        if (plan == null)
            return;

        int emptyMapsNeeded = plan.bundlesToCopy.entrySet().stream()
                .mapToInt(e -> getNumStored(bundles[e.getKey()].weight().getOrThrow()) * e.getValue().size()).sum();
        if (totalEmptyMaps < emptyMapsNeeded) {
            error("Insufficient empty maps");
            return;
        }

        TreeMap<Integer, List<Integer>> bundlesToCopy = plan.bundlesToCopy;
        int tempBundleSlot = plan.tempSlot;

        for (var entry : bundlesToCopy.entrySet()) {
            BundleContents content = bundles[entry.getKey()];

            ItemStack[] contentItems = extractItems(content);

            for (int _0 = 0; _0 < contentItems.length; ++_0) {
                clicks.add(new InvAction(slotsWithBundles[entry.getKey()], 1, InvAction.ActionType.CLICK));
                clicks.add(new InvAction(tempBundleSlot, 0, InvAction.ActionType.CLICK));
            }

            for (int i = 0; i < contentItems.length; ++i) {
                final int count = contentItems[i].getCount();

                clicks.add(new InvAction(tempBundleSlot, 1, InvAction.ActionType.CLICK));

                clicks.add(new InvAction(f.inputStart, 0, InvAction.ActionType.CLICK));
                boolean didShiftCraft = false;
                for (int d : entry.getValue()) {
                    if (numEmptyMapsInGrid < count) {
                        numEmptyMapsInGrid = getEmptyMapsIntoInput(clicks, slots, f, count, numEmptyMapsInGrid, 99);
                        LAST_EMPTY_SLOT = lastEmptySlot(slots, f.hotbarEnd, f.inventoryStart);
                    }

                    numEmptyMapsInGrid -= count;
                    if (didShiftCraft) {
                        if (LAST_EMPTY_SLOT >= f.hotbarStart) {
                            clicks.add(new InvAction(f.inputStart, LAST_EMPTY_SLOT - f.hotbarStart,
                                    InvAction.ActionType.HOTBAR_SWAP));
                            ItemStack temp = slots[f.inputStart];
                            slots[f.inputStart] = slots[LAST_EMPTY_SLOT];
                            slots[LAST_EMPTY_SLOT] = temp;
                        } else {
                            clicks.add(new InvAction(LAST_EMPTY_SLOT, 0, InvAction.ActionType.CLICK));
                            clicks.add(new InvAction(f.inputStart, 0, InvAction.ActionType.CLICK));
                            slots[f.inputStart] = slots[LAST_EMPTY_SLOT];
                            slots[LAST_EMPTY_SLOT] = ItemStack.EMPTY;
                        }
                        LAST_EMPTY_SLOT = lastEmptySlot(slots, f.hotbarEnd, f.inventoryStart);
                    }

                    if (LAST_EMPTY_SLOT != -1
                            && (count > 1 || d == entry.getValue().get(entry.getValue().size() - 1))) {
                        didShiftCraft = true;
                        clicks.add(new InvAction(f.resultSlot, 0, InvAction.ActionType.SHIFT_CLICK));
                        clicks.add(new InvAction(LAST_EMPTY_SLOT, 1, InvAction.ActionType.CLICK));
                        clicks.add(new InvAction(slotsWithBundles[d], 0, InvAction.ActionType.CLICK));
                    } else {
                        didShiftCraft = false;
                        clicks.add(new InvAction(f.resultSlot, 0, InvAction.ActionType.CLICK));
                        clicks.add(new InvAction(f.inputStart, 0, InvAction.ActionType.CLICK));
                        clicks.add(new InvAction(f.inputStart, 1, InvAction.ActionType.CLICK));
                        clicks.add(new InvAction(slotsWithBundles[d], 0, InvAction.ActionType.CLICK));
                    }
                }

                final int fromSlot = didShiftCraft ? LAST_EMPTY_SLOT : f.inputStart;
                clicks.add(new InvAction(fromSlot, 0, InvAction.ActionType.CLICK));
                clicks.add(new InvAction(slotsWithBundles[entry.getKey()], 0, InvAction.ActionType.CLICK));
            }
        }

        if (numEmptyMapsInGrid > 0) {
            clicks.add(new InvAction(MAP_INPUT_SLOT, 0, InvAction.ActionType.SHIFT_CLICK));
        }
    }

    private static ItemStack[] extractItems(BundleContents contents) {
        ItemStack[] items = new ItemStack[contents.size()];
        int i = 0;
        for (var t : contents.items()) {
            items[i++] = t.create();
        }
        return items;
    }

    @Override
    public String getInfoString() {
        if (processing && !clickQueue.isEmpty()) {
            return clickQueue.size() + " clicks";
        }

        return null;
    }

    private record PlanResult(TreeMap<Integer, List<Integer>> bundlesToCopy, int tempSlot) {
    }

    private record PrioAndSlot(int p, int slot) implements Comparable<PrioAndSlot> {
        @Override
        public int compareTo(PrioAndSlot o) {
            return p != o.p ? p - o.p : slot - o.slot;
        }
    }

    private record BundleScan(int[] slots, BundleContents[] contents, int sourceCount, int usableEmptyCount) {
    }
}
