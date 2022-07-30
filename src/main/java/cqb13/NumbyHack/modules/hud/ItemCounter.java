package cqb13.NumbyHack.modules.hud;

import cqb13.NumbyHack.NumbyHack;
import meteordevelopment.meteorclient.settings.*;
import meteordevelopment.meteorclient.systems.hud.Alignment;
import meteordevelopment.meteorclient.systems.hud.HudElement;
import meteordevelopment.meteorclient.systems.hud.HudElementInfo;
import meteordevelopment.meteorclient.systems.hud.HudRenderer;
import meteordevelopment.meteorclient.systems.hud.elements.TextHud;
import meteordevelopment.meteorclient.utils.Utils;
import meteordevelopment.meteorclient.utils.misc.Names;
import meteordevelopment.meteorclient.utils.player.InvUtils;
import net.minecraft.item.*;

import java.util.*;

public class ItemCounter extends HudElement {
    public static final HudElementInfo<ItemCounter> INFO = new HudElementInfo<>(NumbyHack.HUD_GROUP, "item-counter", "Count different items in text.", ItemCounter::new);

    private final SettingGroup sgGeneral = settings.getDefaultGroup();
    private final SettingGroup sgRender = settings.createGroup("Render");
    private final SettingGroup sgScale = settings.createGroup("Scale");

    public enum SortMode {
        Longest,
        Shortest
    }

    // General
    private final Setting<SortMode> sortMode = sgGeneral.add(new EnumSetting.Builder<SortMode>()
            .name("sort-mode")
            .description("How to sort the item list.")
            .defaultValue(SortMode.Shortest)
            .build()
    );

    private final Setting<List<Item>> items = sgGeneral.add(new ItemListSetting.Builder()
            .name("items")
            .description("Which items to display in the counter list.")
            .defaultValue(new ArrayList<>(0))
            .build()
    );

    // Render
    private final Setting<Boolean> shadow = sgRender.add(new BoolSetting.Builder()
            .name("shadow")
            .description("Renders shadow behind text.")
            .defaultValue(true)
            .build()
    );

    private final Setting<Alignment> alignment = sgRender.add(new EnumSetting.Builder<Alignment>()
            .name("alignment")
            .description("Horizontal alignment.")
            .defaultValue(Alignment.Auto)
            .build()
    );

    // Scale
    private final Setting<Boolean> customScale = sgScale.add(new BoolSetting.Builder()
            .name("custom-scale")
            .description("Applies custom text scale rather than the global one.")
            .defaultValue(false)
            .build()
    );

    private final Setting<Double> scale = sgScale.add(new DoubleSetting.Builder()
            .name("scale")
            .description("Custom scale.")
            .visible(customScale::get)
            .defaultValue(1)
            .min(0.5)
            .sliderRange(0.5, 3)
            .build()
    );

    public ItemCounter() {
        super(INFO);
    }


    private final ArrayList<String> itemCounter = new ArrayList<>();

    @Override
    public void tick(HudRenderer renderer) {
        if (!Utils.canUpdate()) return;

        updateCounter();

        double width = 0;
        double height = 0;
        int i = 0;

        if (itemCounter.isEmpty()) {
            String t = "Item Counter";
            width = Math.max(width, renderer.textWidth(t));
            height += renderer.textHeight();
        } else {
            for (String counter : itemCounter) {
                width = Math.max(width, renderer.textWidth(counter));
                height += renderer.textHeight();
                if (i > 0) height += 2;
                i++;
            }
        }
        box.setSize(width, height);
    }

    @Override
    public void render(HudRenderer renderer) {
        if (!Utils.canUpdate()) return;

        updateCounter();

        double x = this.x;
        double y = this.y;
        int i = 0;

        if (itemCounter.isEmpty()) {
            String text = "Item Counter:";
                renderer.text(text, x + alignX(renderer.textWidth(text, shadow.get(), getScale()), alignment.get()), y, TextHud.getSectionColor(0), shadow.get(), getScale());
        } else {
            for (String counter: itemCounter) {
                renderer.text(counter, x + alignX(renderer.textWidth(counter, shadow.get(), getScale()), alignment.get()), y, TextHud.getSectionColor(0), shadow.get(), getScale());
                y += renderer.textHeight();
                if (i > 0) y += 2;
                i++;
            }
        }
    }

    private double getScale() {
        return customScale.get() ? scale.get() : -1;
    }

    private void updateCounter() {
        items.get().sort(Comparator.comparingDouble(value -> getName(value).length()));

        itemCounter.clear();
        for (Item item: items.get()) itemCounter.add(getName(item) + ": " + InvUtils.find(item).count());

        if (sortMode.get().equals(SortMode.Shortest)) {
            itemCounter.sort(Comparator.comparing(String::length));
        } else {
            itemCounter.sort(Comparator.comparing(String::length).reversed());
        }
    }

    public static String getName(Item item) {
        if (item instanceof BedItem) return "Beds";
        if (item instanceof ExperienceBottleItem) return "XP Bottles";
        if (item instanceof EndCrystalItem) return "Crystals";
        if (item instanceof EnchantedGoldenAppleItem) return "Gapples";
        if (item instanceof EnderPearlItem) return "Pearls";
        if (item == Items.TOTEM_OF_UNDYING) return "Totems";
        if (item == Items.ENDER_CHEST) return "Echests";
        if (item == Items.OBSIDIAN) return "Obby";
        return Names.get(item);
    }
}