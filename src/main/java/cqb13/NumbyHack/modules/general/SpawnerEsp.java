package cqb13.NumbyHack.modules.general;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReferenceArray;

import cqb13.NumbyHack.NumbyHack;
import meteordevelopment.meteorclient.events.game.GameLeftEvent;
import meteordevelopment.meteorclient.events.render.Render3DEvent;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.renderer.ShapeMode;
import meteordevelopment.meteorclient.settings.*;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.utils.player.ChatUtils;
import meteordevelopment.meteorclient.utils.render.color.Color;
import meteordevelopment.meteorclient.utils.render.color.SettingColor;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.MobSpawnerBlockEntity;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.world.chunk.WorldChunk;

/**
 * made by cqb13
 *
 * inspired by ActivatedSpawnerDetector from trouserstreak
 */
public class SpawnerEsp extends Module {
  private final SettingGroup sgGeneral = settings.getDefaultGroup();
  private final SettingGroup sgRender = settings.createGroup("Render");

  private final Setting<Boolean> findZombie = sgGeneral.add(new BoolSetting.Builder()
      .name("find-zombie-spawners")
      .description("Will search for zombie spawners")
      .defaultValue(true)
      .build());

  private final Setting<Boolean> findSkeleton = sgGeneral.add(new BoolSetting.Builder()
      .name("find-skeleton-spawners")
      .description("Will search for skeleton spawners")
      .defaultValue(true)
      .build());

  private final Setting<Boolean> findSpider = sgGeneral.add(new BoolSetting.Builder()
      .name("find-spider-spawners")
      .description("Will search for spider spawners")
      .defaultValue(true)
      .build());

  private final Setting<Boolean> findCaveSpider = sgGeneral.add(new BoolSetting.Builder()
      .name("find-cave-spider-spawners")
      .description("Will search for cave-spider spawners")
      .defaultValue(true)
      .build());

  private final Setting<Boolean> findSilverfish = sgGeneral.add(new BoolSetting.Builder()
      .name("find-silverfish-spawners")
      .description("Will search for silverfish spawners")
      .defaultValue(true)
      .build());

  private final Setting<Boolean> findBlaze = sgGeneral.add(new BoolSetting.Builder()
      .name("find-blaze-spawners")
      .description("Will search for blaze spawners")
      .defaultValue(true)
      .build());

  private final Setting<Boolean> findMagma = sgGeneral.add(new BoolSetting.Builder()
      .name("find-magma-spawners")
      .description("Will search for magma spawners")
      .defaultValue(true)
      .build());

  private final Setting<Boolean> findDungeon = sgGeneral.add(new BoolSetting.Builder()
      .name("find-dungeon-spawners")
      .description("Will search for all other spawners")
      .defaultValue(true)
      .build());

  private final Setting<Boolean> displaycoords = sgGeneral.add(new BoolSetting.Builder()
      .name("DisplayCoords")
      .description("Displays coords of found spawners in chat")
      .defaultValue(true)
      .build());

  private final Setting<ShapeMode> shapeMode = sgRender.add(new EnumSetting.Builder<ShapeMode>()
      .name("shape-mode")
      .description("How the shapes are rendered.")
      .defaultValue(ShapeMode.Both)
      .build());

  public final Setting<Integer> renderDistance = sgRender.add(new IntSetting.Builder()
      .name("render-distance")
      .description("How many chunks around the player to show detected spawners.")
      .defaultValue(32)
      .min(6)
      .sliderRange(6, 1024)
      .build());

  private final Setting<SettingColor> zombieSideColor = sgRender.add(new ColorSetting.Builder()
      .name("zombie-spawner-side-color")
      .description("Color of the zombie spawners.")
      .defaultValue(new SettingColor(146, 188, 98, 75))
      .visible(() -> ((shapeMode.get() == ShapeMode.Sides || shapeMode.get() == ShapeMode.Both) && findZombie.get()))
      .build());

  private final Setting<SettingColor> zombieLineColor = sgRender.add(new ColorSetting.Builder()
      .name("zombie-spawner-line-color")
      .description("Color of the zombie spawners.")
      .defaultValue(new SettingColor(146, 188, 98, 255))
      .visible(() -> (shapeMode.get() == ShapeMode.Lines || shapeMode.get() == ShapeMode.Both) && findZombie.get())
      .build());

  private final Setting<SettingColor> skeletonSideColor = sgRender.add(new ColorSetting.Builder()
      .name("skeleton-spawner-side-color")
      .description("Color of the skeleton spawners.")
      .defaultValue(new SettingColor(255, 255, 255, 75))
      .visible(() -> (shapeMode.get() == ShapeMode.Sides || shapeMode.get() == ShapeMode.Both) && findSkeleton.get())
      .build());

  private final Setting<SettingColor> skeletonLineColor = sgRender.add(new ColorSetting.Builder()
      .name("skeleton-spawner-line-color")
      .description("Color of the skeleton spawners.")
      .defaultValue(new SettingColor(255, 255, 255, 255))
      .visible(() -> (shapeMode.get() == ShapeMode.Lines || shapeMode.get() == ShapeMode.Both) && findSkeleton.get())
      .build());

  private final Setting<SettingColor> spiderSideColor = sgRender.add(new ColorSetting.Builder()
      .name("spider-spawner-side-color")
      .description("Color of the spider spawners.")
      .defaultValue(new SettingColor(0, 0, 0, 75))
      .visible(() -> (shapeMode.get() == ShapeMode.Sides || shapeMode.get() == ShapeMode.Both) && findSpider.get())
      .build());

  private final Setting<SettingColor> spiderLineColor = sgRender.add(new ColorSetting.Builder()
      .name("spider-spawner-line-color")
      .description("Color of the spider spawners.")
      .defaultValue(new SettingColor(0, 0, 0, 255))
      .visible(() -> (shapeMode.get() == ShapeMode.Lines || shapeMode.get() == ShapeMode.Both) && findSpider.get())
      .build());

  private final Setting<SettingColor> caveSpiderSideColor = sgRender.add(new ColorSetting.Builder()
      .name("cave-spider-spawner-side-color")
      .description("Color of the cave spider spawners.")
      .defaultValue(new SettingColor(79, 17, 17, 75))
      .visible(() -> (shapeMode.get() == ShapeMode.Sides || shapeMode.get() == ShapeMode.Both) && findCaveSpider.get())
      .build());

  private final Setting<SettingColor> caveSpiderLineColor = sgRender.add(new ColorSetting.Builder()
      .name("cave-spider-spawner-line-color")
      .description("Color of the cave spider spawners.")
      .defaultValue(new SettingColor(79, 17, 17, 255))
      .visible(() -> (shapeMode.get() == ShapeMode.Lines || shapeMode.get() == ShapeMode.Both) && findCaveSpider.get())
      .build());

  private final Setting<SettingColor> silverfishSideColor = sgRender.add(new ColorSetting.Builder()
      .name("silverfish-spawner-side-color")
      .description("Color of the silverfish spawners.")
      .defaultValue(new SettingColor(176, 181, 181, 75))
      .visible(() -> (shapeMode.get() == ShapeMode.Sides || shapeMode.get() == ShapeMode.Both) && findSilverfish.get())
      .build());

  private final Setting<SettingColor> silverfishLineColor = sgRender.add(new ColorSetting.Builder()
      .name("silverfish-spawner-line-color")
      .description("Color of the silverfish spawners.")
      .defaultValue(new SettingColor(176, 181, 181, 255))
      .visible(() -> (shapeMode.get() == ShapeMode.Lines || shapeMode.get() == ShapeMode.Both) && findSilverfish.get())
      .build());

  private final Setting<SettingColor> blazeSideColor = sgRender.add(new ColorSetting.Builder()
      .name("blaze-spawner-side-color")
      .description("Color of the blaze spawners.")
      .defaultValue(new SettingColor(232, 144, 2, 75))
      .visible(() -> (shapeMode.get() == ShapeMode.Sides || shapeMode.get() == ShapeMode.Both) && findBlaze.get())
      .build());

  private final Setting<SettingColor> blazeLineColor = sgRender.add(new ColorSetting.Builder()
      .name("blaze-spawner-line-color")
      .description("Color of the blaze spawners.")
      .defaultValue(new SettingColor(232, 144, 2, 255))
      .visible(() -> (shapeMode.get() == ShapeMode.Lines || shapeMode.get() == ShapeMode.Both) && findBlaze.get())
      .build());

  private final Setting<SettingColor> magmaSideColor = sgRender.add(new ColorSetting.Builder()
      .name("magma-spawner-side-color")
      .description("Color of the magma spawners.")
      .defaultValue(new SettingColor(232, 40, 2, 75))
      .visible(() -> (shapeMode.get() == ShapeMode.Sides || shapeMode.get() == ShapeMode.Both) && findMagma.get())
      .build());

  private final Setting<SettingColor> magmaLineColor = sgRender.add(new ColorSetting.Builder()
      .name("magma-spawner-line-color")
      .description("Color of the magma spawners.")
      .defaultValue(new SettingColor(232, 40, 2, 255))
      .visible(() -> (shapeMode.get() == ShapeMode.Lines || shapeMode.get() == ShapeMode.Both) && findMagma.get())
      .build());

  private final Setting<SettingColor> dungeonSideColor = sgRender.add(new ColorSetting.Builder()
      .name("dungeon-spawner-side-color")
      .description("Color of the dunegon spawners.")
      .defaultValue(new SettingColor(0, 0, 0, 75, true))
      .visible(() -> (shapeMode.get() == ShapeMode.Sides || shapeMode.get() == ShapeMode.Both) && findDungeon.get())
      .build());

  private final Setting<SettingColor> dungeonLineColor = sgRender.add(new ColorSetting.Builder()
      .name("dungeon-spawner-line-color")
      .description("Color of the dungeon spawners.")
      .defaultValue(new SettingColor(0, 0, 0, 255, true))
      .visible(() -> (shapeMode.get() == ShapeMode.Lines || shapeMode.get() == ShapeMode.Both) && findDungeon.get())
      .build());

  private final Set<BlockPos> positions = Collections.synchronizedSet(new HashSet<>());
  private final Set<FoundSpawner> foundSpawners = Collections.synchronizedSet(new HashSet<>());

  public SpawnerEsp() {
    super(NumbyHack.CATEGORY, "spawner-esp", "Shows spawners of different mobs in different colors");
  }

  @Override
  public void onDeactivate() {
    foundSpawners.clear();
    positions.clear();
  }

  @EventHandler
  private void onGameLeft(GameLeftEvent event) {
    foundSpawners.clear();
    positions.clear();
  }

  @EventHandler
  private void onPreTick(TickEvent.Pre event) {
    if (mc.world == null || mc.player == null)
      return;
    AtomicReferenceArray<WorldChunk> chunks = mc.world.getChunkManager().chunks.chunks;
    Set<WorldChunk> chunkSet = new HashSet<>();

    for (int i = 0; i < chunks.length(); i++) {
      WorldChunk chunk = chunks.get(i);
      if (chunk != null) {
        chunkSet.add(chunk);
      }
    }

    chunkSet.forEach(chunk -> extracted(chunk));
  }

  private void extracted(WorldChunk chunk) {
    List<BlockEntity> blockEntities = new ArrayList<>(chunk.getBlockEntities().values());

    for (BlockEntity blockEntity : blockEntities) {
      if (!(blockEntity instanceof MobSpawnerBlockEntity))
        continue;

      MobSpawnerBlockEntity spawner = (MobSpawnerBlockEntity) blockEntity;
      BlockPos pos = spawner.getPos();

      if (positions.contains(pos))
        continue;

      String monster = null;

      if (spawner.getLogic().spawnEntry != null && spawner.getLogic().spawnEntry.getNbt().get("id") != null)
        monster = spawner.getLogic().spawnEntry.getNbt().get("id").toString();

      if (monster != null) {
        positions.add(pos);
        if (monster.contains("zombie") && findZombie.get()) {
          sendMsg("zombie", pos);
          foundSpawners.add(new FoundSpawner(SpawnerType.Zombie, pos));
        } else if (monster.contains("skeleton") && findSkeleton.get()) {
          sendMsg("skeleton", pos);
          foundSpawners.add(new FoundSpawner(SpawnerType.Skeleton, pos));
        } else if (monster.contains(":spider") && findSpider.get()) {
          foundSpawners.add(new FoundSpawner(SpawnerType.Spider, pos));
          sendMsg("spider", pos);
        } else if (monster.contains("cave_spider") && findCaveSpider.get()) {
          sendMsg("cave spider", pos);
          foundSpawners.add(new FoundSpawner(SpawnerType.CaveSpider, pos));
        } else if (monster.contains("silverfish") && findSilverfish.get()) {
          sendMsg("silverfish", pos);
          foundSpawners.add(new FoundSpawner(SpawnerType.Silverfish, pos));
        } else if (monster.contains("blaze") && findBlaze.get()) {
          sendMsg("blaze", pos);
          foundSpawners.add(new FoundSpawner(SpawnerType.Blaze, pos));
        } else if (monster.contains("magma") && findMagma.get()) {
          sendMsg("magma", pos);
          foundSpawners.add(new FoundSpawner(SpawnerType.Magma, pos));
        } else {
          if (!findDungeon.get())
            return;
          sendMsg("dungeon", pos);
          foundSpawners.add(new FoundSpawner(SpawnerType.Dungeon, pos));
        }
      }
    }
  }

  @EventHandler
  private void onRender(Render3DEvent event) {
    synchronized (foundSpawners) {
      for (FoundSpawner spawner : foundSpawners) {
        BlockPos playerPos = new BlockPos(mc.player.getBlockX(), spawner.pos.getY(), mc.player.getBlockZ());
        if (playerPos.isWithinDistance(spawner.pos, renderDistance.get() * 16)) {
          event.renderer.box(new Box(spawner.pos), getSideColor(spawner.spawnerType()),
              getLineColor(spawner.spawnerType()), shapeMode.get(), 0);
        }
      }
    }
  }

  private final void sendMsg(String name, BlockPos pos) {
    if (!chatFeedback)
      return;

    if (displaycoords.get()) {
      ChatUtils.sendMsg(Text.of("Found " + name + " spawner at " + pos.getX() + ", " + pos.getY() + ", " + pos.getZ()));
    } else {
      ChatUtils.sendMsg(Text.of("Found " + name + " spawner"));
    }
  }

  private Color getSideColor(SpawnerType spawnerType) {
    switch (spawnerType) {
      case SpawnerType.Zombie:
        return zombieSideColor.get();
      case SpawnerType.Skeleton:
        return skeletonSideColor.get();
      case SpawnerType.Spider:
        return spiderSideColor.get();
      case SpawnerType.CaveSpider:
        return caveSpiderSideColor.get();
      case SpawnerType.Silverfish:
        return silverfishSideColor.get();
      case SpawnerType.Blaze:
        return blazeSideColor.get();
      case SpawnerType.Magma:
        return magmaSideColor.get();
      case SpawnerType.Dungeon:
        return dungeonSideColor.get();
      default:
        return dungeonSideColor.get();
    }
  }

  private Color getLineColor(SpawnerType spawnerType) {
    switch (spawnerType) {
      case SpawnerType.Zombie:
        return zombieLineColor.get();
      case SpawnerType.Skeleton:
        return skeletonLineColor.get();
      case SpawnerType.Spider:
        return spiderLineColor.get();
      case SpawnerType.CaveSpider:
        return caveSpiderLineColor.get();
      case SpawnerType.Silverfish:
        return silverfishLineColor.get();
      case SpawnerType.Blaze:
        return blazeLineColor.get();
      case SpawnerType.Magma:
        return magmaLineColor.get();
      case SpawnerType.Dungeon:
        return dungeonLineColor.get();
      default:
        return dungeonLineColor.get();
    }
  }

  private record FoundSpawner(SpawnerType spawnerType, BlockPos pos) {
  }

  private enum SpawnerType {
    Zombie,
    Skeleton,
    Spider,
    CaveSpider,
    Silverfish,
    Blaze,
    Magma,
    Dungeon,
  }
}
