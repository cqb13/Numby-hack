package cqb13.NumbyHack.modules.general;

import cqb13.NumbyHack.NumbyHack;
import meteordevelopment.meteorclient.settings.BoolSetting;
import meteordevelopment.meteorclient.settings.IntSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.systems.modules.Module;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemStack;

/**
 * made by cqb13
 * Heavily inspired by fast-use in meteor client
 */
public class CarpetPlacer extends Module {
  private final SettingGroup sgGeneral = settings.getDefaultGroup();

  private final Setting<Boolean> antiStack = sgGeneral.add(new BoolSetting.Builder()
      .name("anti-stack")
      .description("Prevents you from placing carpets on top of other carpets.")
      .defaultValue(true)
      .build());

  private final Setting<Boolean> fastPlace = sgGeneral.add(new BoolSetting.Builder()
      .name("fast-place")
      .description("Place carpets faster than normal.")
      .defaultValue(true)
      .build());

  private final Setting<Integer> cooldown = sgGeneral.add(new IntSetting.Builder()
      .name("cooldown")
      .description("Fast-place cooldown in ticks.")
      .defaultValue(0)
      .min(0)
      .sliderMax(4)
      .visible(fastPlace::get)
      .build());

  public CarpetPlacer() {
    super(NumbyHack.CATEGORY, "carpet-placer", "Helps with manual carpet placing for spawn proofing and mapart.");
  }

  public boolean isAntiStackEnabled() {
    return antiStack.get();
  }

  public boolean isFastPlaceEnabled() {
    return fastPlace.get();
  }

  public int getCooldown() {
    return cooldown.get();
  }

  public int getItemUseCooldown(ItemStack itemStack) {
    if (shouldFastPlace(itemStack)) {
      return cooldown.get();
    }
    return 4; // default cooldown
  }

  private boolean shouldFastPlace(ItemStack itemStack) {
    return (fastPlace.get() && itemStack.getItem() instanceof BlockItem);
  }
}
