package cqb13.NumbyHack.mixins.meteor;

import meteordevelopment.discordipc.RichPresence;
import meteordevelopment.meteorclient.systems.modules.Category;
import meteordevelopment.meteorclient.systems.modules.misc.DiscordPresence;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.invoke.arg.Args;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.settings.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = DiscordPresence.class, remap = false)
public abstract class DiscordPresenceMixin extends Module {
  private SettingGroup numby$sgNumby;
  private Setting<Boolean> numby$numbyImage;
  private Setting<Boolean> numby$numbyImageText;
  private Setting<Boolean> numby$numbyDetails;

  public DiscordPresenceMixin(Category category, String name, String description) {
    super(category, name, description);
  }

  @Inject(method = "<init>", at = @At("TAIL"))
  private void constructor(CallbackInfo ci) {
    numby$sgNumby = settings.createGroup("Numby Presence");
    numby$numbyImage = numby$sgNumby.add(new BoolSetting.Builder()
        .name("numby-image")
        .description("Shows the Numby Hack logo as the large image.")
        .defaultValue(true)
        .build());
    numby$numbyImageText = numby$sgNumby.add(new BoolSetting.Builder()
        .name("numby-image-text")
        .description("Displays Numby Hack text as the large image text.")
        .defaultValue(true)
        .build());
    numby$numbyDetails = numby$sgNumby.add(new BoolSetting.Builder()
        .name("numby-details")
        .description("Shows the current module in use as the details.")
        .defaultValue(true)
        .build());
  }

  @ModifyArg(method = "onActivate", at = @At(value = "INVOKE", target = "Lmeteordevelopment/discordipc/DiscordIPC;start(JLjava/lang/Runnable;)Z"))
  private long modifyAppId(long appId) {
    if (numby$numbyImage.get()) {
      return 943264770642034708L;
    }
    return appId;
  }

  @ModifyArgs(method = "onActivate", at = @At(value = "INVOKE", target = "Lmeteordevelopment/discordipc/RichPresence;setLargeImage(Ljava/lang/String;Ljava/lang/String;)V"))
  private void modifyLargeImage(Args args) {
    if (numby$numbyImageText.get()) {
      args.set(0, "large");
      args.set(1, "Numby Hack!");
    }
  }

  @Redirect(method = "onTick", at = @At(value = "INVOKE", target = "Lmeteordevelopment/discordipc/RichPresence;setState(Ljava/lang/String;)V", ordinal = 0))
  private void modifyState(RichPresence instance, String state) {
    if (numby$numbyDetails.get()) {
      instance.setDetails(state); // this is stupid -Crosby
    }
  }

  @ModifyArg(method = "onTick", at = @At(value = "INVOKE", target = "Lmeteordevelopment/discordipc/RichPresence;setDetails(Ljava/lang/String;)V", ordinal = 1))
  private String modifyDetails(String details) {
    if (numby$numbyDetails.get()) {
      return "Number81 on top!";
    }
    return details;
  }

  @Redirect(method = "onTick", at = @At(value = "INVOKE", target = "Lmeteordevelopment/discordipc/RichPresence;setState(Ljava/lang/String;)V"), slice = @Slice(from = @At(value = "INVOKE", target = "Lmeteordevelopment/discordipc/RichPresence;setDetails(Ljava/lang/String;)V", ordinal = 1)))
  private void deleteMethods(RichPresence instance, String state) {
  }
}
