package cqb13.NumbyHack.modules.general;

import static org.lwjgl.glfw.GLFW.GLFW_MOUSE_BUTTON_MIDDLE;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import cqb13.NumbyHack.NumbyHack;
import meteordevelopment.meteorclient.events.game.GameLeftEvent;
import meteordevelopment.meteorclient.events.meteor.MouseClickEvent;
import meteordevelopment.meteorclient.events.render.Render3DEvent;
import meteordevelopment.meteorclient.gui.GuiTheme;
import meteordevelopment.meteorclient.gui.widgets.WWidget;
import meteordevelopment.meteorclient.gui.widgets.containers.WHorizontalList;
import meteordevelopment.meteorclient.gui.widgets.containers.WVerticalList;
import meteordevelopment.meteorclient.gui.widgets.pressable.WButton;
import meteordevelopment.meteorclient.renderer.ShapeMode;
import meteordevelopment.meteorclient.settings.BoolSetting;
import meteordevelopment.meteorclient.settings.ColorSetting;
import meteordevelopment.meteorclient.settings.EnumSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.utils.misc.input.KeyAction;
import meteordevelopment.meteorclient.utils.render.RenderUtils;
import meteordevelopment.meteorclient.utils.render.color.Color;
import meteordevelopment.meteorclient.utils.render.color.SettingColor;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.MapIdComponent;
import net.minecraft.entity.Entity;
import net.minecraft.entity.decoration.ItemFrameEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.WorldSavePath;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Direction;

public class MapArtTracker extends Module {
    private final SettingGroup sgGeneral = settings.getDefaultGroup();

    private final Setting<Boolean> render = sgGeneral.add(new BoolSetting.Builder()
            .name("render")
            .description("Highlight filled maps around you.")
            .defaultValue(true)
            .build());

    private final Setting<Boolean> tracers = sgGeneral.add(new BoolSetting.Builder()
            .name("tracers")
            .description("Render tracer lines to maps.")
            .defaultValue(false)
            .build());

    private final Setting<ShapeMode> shapeMode = sgGeneral.add(new EnumSetting.Builder<ShapeMode>()
            .name("shape-mode")
            .description("The shape.")
            .defaultValue(ShapeMode.Both)
            .build());

    private final Setting<SettingColor> sideColor = sgGeneral.add(new ColorSetting.Builder()
            .name("side-color")
            .description("The side color for maps.")
            .defaultValue(new SettingColor(146, 188, 98, 50)).build());

    private final Setting<SettingColor> lineColor = sgGeneral.add(new ColorSetting.Builder()
            .name("line-color")
            .description("The line color for maps.")
            .defaultValue(new SettingColor(146, 188, 98, 255))
            .build());

    private final Setting<SettingColor> collectedMapSideColor = sgGeneral.add(new ColorSetting.Builder()
            .name("collected-side-color")
            .description("The side color for collected maps.")
            .defaultValue(new SettingColor(73, 107, 190, 50))
            .build());

    private final Setting<SettingColor> collectedMapLineColor = sgGeneral.add(new ColorSetting.Builder()
            .name("collected-line-color")
            .description("The line color for collected maps.")
            .defaultValue(new SettingColor(73, 107, 190, 255))
            .build());

    private final Map<Integer, MapRecord> maps = new HashMap<>();
    private Path storeFile;

    public MapArtTracker() {
        super(NumbyHack.CATEGORY, "map-art-tracker", "Track and highlights map arts.");
    }

    @Override
    public WWidget getWidget(GuiTheme theme) {
        WVerticalList list = theme.verticalList();

        WHorizontalList l1 = list.add(theme.horizontalList()).expandX().widget();

        WButton clearMapCacheBtn = l1.add(theme.button("Clear Map Cache")).expandX().widget();
        clearMapCacheBtn.action = () -> {
            if (!this.isActive() || mc.world == null)
                return;

            maps.clear();

            if (storeFile != null) {
                try {
                    Files.deleteIfExists(storeFile);
                    info("Map cache cleared.");
                } catch (IOException e) {
                    error("Failed to delete map cache file.");
                    e.printStackTrace();
                }
            }
        };

        return list;
    }

    @Override
    public void onActivate() {
        if (mc.world == null)
            return;

        maps.clear();
        initStoreFile();
        loadMaps();
    }

    @Override
    public void onDeactivate() {
        saveMaps();
    }

    @EventHandler
    private void onGameLeft(GameLeftEvent event) {
        saveMaps();
    }


    //TODO: map name rendering option
    @EventHandler
    private void onRender(Render3DEvent event) {
        if (mc.world == null || mc.player == null) {
            return;
        }

        for (Entity entity : mc.world.getEntities()) {
            if (!(entity instanceof ItemFrameEntity frame)) {
                continue;
            }

            ItemStack stack = frame.getHeldItemStack();
            if (stack.isEmpty() || stack.getItem() != Items.FILLED_MAP) {
                continue;
            }

            int mapId = getMapId(stack);
            if (mapId < 0) {
                continue;
            }

            String name = stack.getName().getString();

            MapRecord rec = maps.get(mapId);
            if (rec == null) {
                rec = new MapRecord(mapId);
                rec.collected = false; // seen but not collected
                if (name != null && !name.isEmpty()) {
                    rec.names.add(name);
                }
                maps.put(mapId, rec);
            } else {
                if (name != null && !name.isEmpty()) {
                    // add an alternate name associated with a map
                    rec.names.add(name);
                }
            }

            if (!render.get()) {
                continue;
            }

            Box box = frame.getBoundingBox();
            float pitch = frame.getPitch();

            Color fill = new Color(sideColor.get());
            Color outline = new Color(lineColor.get());

            if (rec.collected) {
                fill = new Color(collectedMapSideColor.get());
                outline = new Color(collectedMapLineColor.get());
            }

            event.renderer.box(box, fill, outline, shapeMode.get(), 0);

            if (tracers.get()) {
                event.renderer.line(RenderUtils.center.x, RenderUtils.center.y, RenderUtils.center.z, entity.getX(),
                        entity.getY(), entity.getZ(), outline);
            }
        }
    }

    @EventHandler
    private void onMouseClick(MouseClickEvent event) {
        if (event.action != KeyAction.Press || event.button() != GLFW_MOUSE_BUTTON_MIDDLE || mc.currentScreen != null) {
            return;
        }

        HitResult hr = mc.crosshairTarget;
        if (hr instanceof EntityHitResult ehr) {
            Entity e = ehr.getEntity();
            if (e instanceof ItemFrameEntity frame) {
                ItemStack stack = frame.getHeldItemStack();
                if (!stack.isEmpty() && stack.getItem() == Items.FILLED_MAP) {
                    int mapId = getMapId(stack);
                    if (isCollected(mapId)) {
                        removeCollected(mapId, stack.getName().getString());
                    } else {
                        markCollected(mapId, stack.getName().getString());
                    }
                }
            }
        }
    }

    private int getMapId(ItemStack stack) {
        if (mc.world == null || stack == null || stack.getItem() != Items.FILLED_MAP) {
            return -1;
        }

        try {
            MapIdComponent mapId = stack.get(DataComponentTypes.MAP_ID);

            if (mapId == null) {
                return -1;
            }

            return mapId.id();

        } catch (Exception e) {
            return -1;
        }
    }

    private boolean isCollected(int mapId) {
        if (mapId < 0) {
            return false;
        }

        MapRecord rec = maps.get(mapId);
        if (rec == null) {
            return false;
        }

        return rec.collected;
    }

    private void markCollected(int mapId, String observedName) {
        if (mapId < 0) {
            return;
        }

        MapRecord rec = maps.get(mapId);
        if (rec == null) {
            rec = new MapRecord(mapId);
            maps.put(mapId, rec);
        }

        if (observedName != null && !observedName.isEmpty() && !rec.names.contains(observedName)) {
            rec.names.add(observedName);
        }
        if (!rec.collected) {
            rec.collected = true;

            if (chatFeedback) {
                info("Collected a new map " + observedName + "!");
            }
        }
    }

    private void removeCollected(int mapId, String observedName) {
        if (mapId < 0) {
            return;
        }

        MapRecord rec = maps.get(mapId);
        if (rec == null) {
            rec = new MapRecord(mapId);
            maps.put(mapId, rec);
        }

        if (rec.collected) {
            rec.collected = false;

            if (chatFeedback) {
                info("Removed " + observedName + " from map collection!");
            }
        }
    }

    /**
     * File format:
     * int: entry count
     * for each entry:
     * int: mapId
     * boolean: collected
     * int: nameCount
     * for nameCount:
     * UTF string: name
     */
    private void loadMaps() {
        if (storeFile == null || !Files.exists(storeFile)) {
            return;
        }

        try (DataInputStream in = new DataInputStream(new BufferedInputStream(Files.newInputStream(storeFile)))) {
            int count = in.readInt();
            for (int i = 0; i < count; i++) {
                int mapId = in.readInt();
                boolean collected = in.readBoolean();
                int nameCount = in.readInt();
                Set<String> names = new HashSet<>();
                for (int n = 0; n < nameCount; n++) {
                    String s = in.readUTF();
                    names.add(s);
                }
                MapRecord r = new MapRecord(mapId);
                r.collected = collected;
                r.names.addAll(names);
                maps.put(mapId, r);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * File format:
     * int: entry count
     * for each entry:
     * int: mapId
     * boolean: collected
     * int: nameCount
     * for nameCount:
     * UTF string: name
     */
    private void saveMaps() {
        if (storeFile == null) {
            return;
        }

        try (DataOutputStream out = new DataOutputStream(new BufferedOutputStream(Files.newOutputStream(storeFile)))) {
            out.writeInt(maps.size());
            for (MapRecord r : maps.values()) {
                out.writeInt(r.mapId);
                out.writeBoolean(r.collected);
                out.writeInt(r.names.size());
                for (String name : r.names)
                    out.writeUTF(name);
            }
            out.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private String getServerSafeName() {
        if (mc.isInSingleplayer()) {
            String[] array = mc.getServer().getSavePath(WorldSavePath.ROOT).toString().replace(':', '_')
                    .split("/|\\\\");
            return array[array.length - 2];
        }

        return mc.getCurrentServerEntry().address.replace(':', '_');
    }

    private void initStoreFile() {
        String serverSafe = getServerSafeName();
        try {
            Path dir = mc.runDirectory.toPath().resolve("found-maps");
            Files.createDirectories(dir);
            storeFile = dir.resolve(serverSafe + ".dat");
        } catch (IOException e) {
            storeFile = mc.runDirectory.toPath().resolve("found-maps.dat");
        }
    }

    private static class MapRecord {
        final int mapId;
        final Set<String> names = new HashSet<>();
        boolean collected = false;

        MapRecord(int mapId) {
            this.mapId = mapId;
        }
    }
}
