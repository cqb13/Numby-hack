package cqb13.NumbyHack.modules.general;

import cqb13.NumbyHack.NumbyHack;
import meteordevelopment.meteorclient.settings.BoolSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.utils.player.ChatUtils;

/**
 * made by cqb13
 */
public class GameSettings extends Module {
    private final SettingGroup sgGeneral = settings.getDefaultGroup();

    private final Setting<Boolean> hudHidden = sgGeneral.add(new BoolSetting.Builder()
            .name("hide-HUD")
            .description("Hide your HUD.")
            .defaultValue(mc.options.hudHidden)
            .onChanged(this::toggleHUD)
            .build());

    private final Setting<Boolean> pauseOnLostFocus = sgGeneral.add(new BoolSetting.Builder()
            .name("pause-on-lost-focus")
            .description("Pauses the game when it is not focussed.")
            .defaultValue(mc.options.pauseOnLostFocus)
            .onChanged(this::togglePauseOnLostFocus)
            .build());

    private final Setting<Boolean> skipMultiplayerWarning = sgGeneral.add(new BoolSetting.Builder()
            .name("skip-multiplayer-warning")
            .description("Skips the Multiplayer warning.")
            .defaultValue(mc.options.skipMultiplayerWarning)
            .onChanged(this::toggleSkipMultiplayerWarning)
            .build());

    private final Setting<Boolean> smoothCameraEnabled = sgGeneral.add(new BoolSetting.Builder()
            .name("cinematic-camera")
            .description("Smooth camera movement.")
            .defaultValue(mc.options.smoothCameraEnabled)
            .onChanged(this::toggleSmoothCamera)
            .build());

    private final Setting<Boolean> advancedTooltips = sgGeneral.add(new BoolSetting.Builder()
            .name("advanced-tooltips")
            .description("More information on items in your inventory.")
            .defaultValue(mc.options.advancedItemTooltips)
            .onChanged(this::toggleAdvancedTooltips)
            .build());

    private final Setting<Boolean> hideScore = sgGeneral.add(new BoolSetting.Builder()
            .name("hide-score")
            .description("Hides the score when you die.")
            .defaultValue(true)
            .onChanged(this::toggleHideScore)
            .build());

    public GameSettings() {
        super(NumbyHack.CATEGORY, "game-settings",
                "Allows for easier access to Minecraft's settings and adds some tweaks.");
    }

    private void toggleHUD(Boolean b) {
        mc.options.hudHidden = b;
        sendChatInfo("HUD", b ? "hidden" : "shown");
    }

    private void togglePauseOnLostFocus(Boolean b) {
        mc.options.pauseOnLostFocus = b;
        sendChatInfo("Pause on Lost Focus", b ? "enabled" : "disabled");
    }

    private void toggleSkipMultiplayerWarning(Boolean b) {
        mc.options.skipMultiplayerWarning = b;
        sendChatInfo("Skip Multiplayer Warning", b ? "enabled" : "disabled");
    }

    private void toggleSmoothCamera(Boolean b) {
        mc.options.smoothCameraEnabled = b;
        sendChatInfo("Smooth Camera", b ? "enabled" : "disabled");
    }

    private void toggleAdvancedTooltips(Boolean b) {
        mc.options.advancedItemTooltips = b;
        sendChatInfo("Advanced Tooltips", b ? "enabled" : "disabled");
    }

    private void toggleHideScore(Boolean b) {
        sendChatInfo("Hide Score", b ? "enabled" : "disabled");
    }

    public boolean hideTheScore() {
        return isActive() && hideScore.get();
    }

    private void sendChatInfo(String setting, String value) {
        if (!this.chatFeedback)
            return;
        ChatUtils.info("Set %s to %s.", setting, value);
    }
}
