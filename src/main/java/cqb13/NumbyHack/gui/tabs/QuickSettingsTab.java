package cqb13.NumbyHack.gui.tabs;

import static meteordevelopment.meteorclient.MeteorClient.mc;

import meteordevelopment.meteorclient.gui.GuiTheme;
import meteordevelopment.meteorclient.gui.tabs.Tab;
import meteordevelopment.meteorclient.gui.tabs.TabScreen;
import meteordevelopment.meteorclient.gui.tabs.WindowTabScreen;
import meteordevelopment.meteorclient.gui.widgets.containers.WVerticalList;
import meteordevelopment.meteorclient.gui.widgets.pressable.WButton;
import meteordevelopment.meteorclient.utils.player.ChatUtils;
import net.minecraft.client.gui.screen.Screen;

public class QuickSettingsTab extends Tab {
    public QuickSettingsTab() {
        super("Quick Settings");
    }

    @Override
    public TabScreen createScreen(GuiTheme theme) {
        return new QuickSettingsScreen(theme, this);
    }

    @Override
    public boolean isScreen(Screen screen) {
        return screen instanceof QuickSettingsScreen;
    }

    private static class QuickSettingsScreen extends WindowTabScreen {
        public QuickSettingsScreen(GuiTheme theme, Tab tab) {
            super(theme, tab);
        }

        @Override
        public void initWidgets() {
            add(theme.label("Quick Settings")).expandX().centerX();
            add(theme.horizontalSeparator()).expandX();

            WVerticalList list = theme.verticalList();

            WButton hudToggle = list.add(theme.button("Toggle HUD")).expandX().widget();
            hudToggle.action = () -> {
                toggleHUD(!mc.options.hudHidden);
            };

            WButton pauseOnLostFocusToggle = list.add(theme.button("Toggle Pause on Lost Focus")).expandX().widget();
            pauseOnLostFocusToggle.action = () -> {
                togglePauseOnLostFocus(!mc.options.pauseOnLostFocus);
            };

            WButton skipMultiPlayerWarningToggle = list.add(theme.button("Toggle Skip Multiplayer Warning")).expandX()
                    .widget();
            skipMultiPlayerWarningToggle.action = () -> {
                toggleSkipMultiplayerWarning(!mc.options.skipMultiplayerWarning);
            };

            WButton smoothCameraToggle = list.add(theme.button("Toggle Smooth Camera")).expandX()
                    .widget();
            smoothCameraToggle.action = () -> {
                toggleSmoothCamera(!mc.options.smoothCameraEnabled);
            };

            WButton advancedItemTooltipsToggle = list.add(theme.button("Toggle Advanced Tooltips")).expandX()
                    .widget();
            advancedItemTooltipsToggle.action = () -> {
                toggleAdvancedTooltips(!mc.options.advancedItemTooltips);
            };

            add(list);
        }

        @Override
        public boolean shouldCloseOnEsc() {
            return true;
        }
    }

    private static void toggleHUD(Boolean b) {
        mc.options.hudHidden = b;
        sendChatInfo("HUD", b ? "hidden" : "shown");
    }

    private static void togglePauseOnLostFocus(Boolean b) {
        mc.options.pauseOnLostFocus = b;
        sendChatInfo("Pause on Lost Focus", b ? "enabled" : "disabled");
    }

    private static void toggleSkipMultiplayerWarning(Boolean b) {
        mc.options.skipMultiplayerWarning = b;
        sendChatInfo("Skip Multiplayer Warning", b ? "enabled" : "disabled");
    }

    private static void toggleSmoothCamera(Boolean b) {
        mc.options.smoothCameraEnabled = b;
        sendChatInfo("Smooth Camera", b ? "enabled" : "disabled");
    }

    private static void toggleAdvancedTooltips(Boolean b) {
        mc.options.advancedItemTooltips = b;
        sendChatInfo("Advanced Tooltips", b ? "enabled" : "disabled");
    }

    private static void sendChatInfo(String setting, String value) {
        ChatUtils.info("Set %s to %s.", setting, value);
    }
}
