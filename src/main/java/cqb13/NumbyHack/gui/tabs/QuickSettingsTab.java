package cqb13.NumbyHack.gui.tabs;

import static meteordevelopment.meteorclient.MeteorClient.mc;

import meteordevelopment.meteorclient.gui.GuiTheme;
import meteordevelopment.meteorclient.gui.tabs.Tab;
import meteordevelopment.meteorclient.gui.tabs.TabScreen;
import meteordevelopment.meteorclient.gui.tabs.WindowTabScreen;
import meteordevelopment.meteorclient.gui.widgets.WWidget;
import meteordevelopment.meteorclient.gui.widgets.containers.WHorizontalList;
import meteordevelopment.meteorclient.gui.widgets.containers.WVerticalList;
import meteordevelopment.meteorclient.gui.widgets.pressable.WButton;
import meteordevelopment.meteorclient.utils.player.ChatUtils;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.world.entity.player.ChatVisiblity;

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
            add(theme.label("Quick Settings", true)).expandX().centerX();
            add(theme.horizontalSeparator()).expandX();

            WVerticalList list = theme.verticalList();

            WButton hudToggle = list.add(theme.button("Toggle HUD")).expandX().widget();
            hudToggle.action = QuickSettingsTab::toggleHUD;

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
                toggleSmoothCamera(!mc.options.smoothCamera);
            };

            WButton advancedItemTooltipsToggle = list.add(theme.button("Toggle Advanced Tooltips")).expandX()
                    .widget();
            advancedItemTooltipsToggle.action = () -> {
                toggleAdvancedTooltips(!mc.options.advancedItemTooltips);
            };

            WVerticalList chatStyle = theme.verticalList();
            chatStyle.add(theme.label("Chat Style", false)).expandX().centerX().widget();

            WHorizontalList chatStyleButtons = theme.horizontalList();

            WButton setChatToNormal = theme.button("Normal");
            setChatToNormal.action = () -> setChatToNormal();

            WButton setChatToCmdOnly = theme.button("Command");
            setChatToCmdOnly.action = () -> setChatToCmdOnly();

            WButton setChatToHidden = theme.button("Hidden");
            setChatToHidden.action = () -> setChatToHidden();

            chatStyle.add(chatStyleButtons);

            double maxWidth = maxWidgetWidth(setChatToNormal, setChatToCmdOnly, setChatToHidden);

            chatStyleButtons.add(setChatToNormal).minWidth(maxWidth + 5).widget();
            chatStyleButtons.add(setChatToCmdOnly).minWidth(maxWidth + 5).widget();
            chatStyleButtons.add(setChatToHidden).minWidth(maxWidth + 5).widget();

            list.add(chatStyle);

            add(list);
        }

        @Override
        public boolean shouldCloseOnEsc() {
            return true;
        }
    }

    private static void toggleHUD() {
        mc.gui.hud.toggle();
        sendChatInfo("HUD", mc.gui.hud.isHidden() ? "hidden" : "shown");
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
        mc.options.smoothCamera = b;
        sendChatInfo("Smooth Camera", b ? "enabled" : "disabled");
    }

    private static void toggleAdvancedTooltips(Boolean b) {
        mc.options.advancedItemTooltips = b;
        sendChatInfo("Advanced Tooltips", b ? "enabled" : "disabled");
    }

    private static void setChatToNormal() {
        mc.options.chatVisibility().set(ChatVisiblity.FULL);
        sendChatInfo("Chat", "shown");
    }

    private static void setChatToCmdOnly() {
        mc.options.chatVisibility().set(ChatVisiblity.SYSTEM);
        sendChatInfo("Chat", "command only");
    }

    private static void setChatToHidden() {
        mc.options.chatVisibility().set(ChatVisiblity.HIDDEN);
        sendChatInfo("Chat", "hidden");
    }

    private static void sendChatInfo(String setting, String value) {
        ChatUtils.info("Set %s to %s.", setting, value);
    }

    private static double maxWidgetWidth(WWidget... widgets) {
        double max = 0;

        for (WWidget widget : widgets) {
            widget.calculateSize();
            if (widget != null && widget.width > max) {
                max = widget.width;
            }
        }

        return max;
    }
}
