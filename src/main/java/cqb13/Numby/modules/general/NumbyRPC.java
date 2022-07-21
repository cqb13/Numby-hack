package cqb13.NumbyHack.modules.general;

import cqb13.NumbyHack.NumbyHack;
import meteordevelopment.discordipc.DiscordIPC;
import meteordevelopment.discordipc.RichPresence;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.gui.GuiTheme;
import meteordevelopment.meteorclient.gui.widgets.WWidget;
import meteordevelopment.meteorclient.gui.widgets.pressable.WButton;
import meteordevelopment.meteorclient.settings.*;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.utils.Utils;
import meteordevelopment.meteorclient.utils.misc.MeteorStarscript;
import meteordevelopment.meteorclient.utils.player.ChatUtils;
import meteordevelopment.orbit.EventHandler;
import meteordevelopment.starscript.Script;
import meteordevelopment.starscript.compiler.Compiler;
import meteordevelopment.starscript.compiler.Parser;
import meteordevelopment.starscript.utils.StarscriptError;
import net.minecraft.util.Util;

import java.util.ArrayList;
import java.util.List;


public class NumbyRPC extends Module {
    public enum SelectMode {
        Random,
        Sequential
    }

    private final SettingGroup sgLine1 = settings.createGroup("Line 1");
    private final SettingGroup sgLine2 = settings.createGroup("Line 2");

    // Line 1

    private final Setting<List<String>> line1Strings = sgLine1.add(new StringListSetting.Builder()
            .name("line-1-messages")
            .description("Messages used for the first line.")
            .defaultValue("{player}")
            .onChanged(strings -> recompileLine1())
            .build()
    );

    private final Setting<Integer> line1UpdateDelay = sgLine1.add(new IntSetting.Builder()
            .name("line-1-update-delay")
            .description("How fast to update the first line in ticks.")
            .defaultValue(200)
            .min(10)
            .sliderRange(10, 200)
            .build()
    );

    private final Setting<SelectMode> line1SelectMode = sgLine1.add(new EnumSetting.Builder<SelectMode>()
            .name("line-1-select-mode")
            .description("How to select messages for the first line.")
            .defaultValue(SelectMode.Sequential)
            .build()
    );

    // Line 2

    private final Setting<List<String>> line2Strings = sgLine2.add(new StringListSetting.Builder()
            .name("line-2-messages")
            .description("Messages used for the second line.")
            .defaultValue("Numby Hack")
            .onChanged(strings -> recompileLine2())
            .build()
    );

    private final Setting<Integer> line2UpdateDelay = sgLine2.add(new IntSetting.Builder()
            .name("line-2-update-delay")
            .description("How fast to update the second line in ticks.")
            .defaultValue(60)
            .min(10)
            .sliderRange(10, 200)
            .build()
    );

    private final Setting<SelectMode> line2SelectMode = sgLine2.add(new EnumSetting.Builder<SelectMode>()
            .name("line-2-select-mode")
            .description("How to select messages for the second line.")
            .defaultValue(SelectMode.Sequential)
            .build()
    );

    private static final RichPresence rpc = new RichPresence();
    private int ticks;
    private boolean forceUpdate, lastWasInMainMenu;

    private final List<Script> line1Scripts = new ArrayList<>();
    private int line1Ticks, line1I;

    private final List<Script> line2Scripts = new ArrayList<>();
    private int line2Ticks, line2I;

    public NumbyRPC() {
        super(NumbyHack.CATEGORY, "numby-presence", "Displays Numby Hack as your presence on Discord.");

        runInMainMenu = true;
    }

    @Override
    public void onActivate() {
        DiscordIPC.start(943264770642034708L, null);

        rpc.setStart(System.currentTimeMillis() / 1000L);

        String largeText = "Numby Hack " + "!";
        rpc.setLargeImage("large", largeText);

        recompileLine1();
        recompileLine2();

        ticks = 0;
        line1Ticks = 0;
        line2Ticks = 0;
        lastWasInMainMenu = false;

        line1I = 0;
        line2I = 0;
    }

    @Override
    public void onDeactivate() {
        DiscordIPC.stop();
    }

    private void recompile(List<String> messages, List<Script> scripts) {
        scripts.clear();

        for (int i = 0; i < messages.size(); i++) {
            Parser.Result result = Parser.parse(messages.get(i));

            if (result.hasErrors()) {
                if (Utils.canUpdate()) {
                    MeteorStarscript.printChatError(i, result.errors.get(0));
                }

                continue;
            }

            scripts.add(Compiler.compile(result));
        }

        forceUpdate = true;
    }

    private void recompileLine1() {
        recompile(line1Strings.get(), line1Scripts);
    }

    private void recompileLine2() {
        recompile(line2Strings.get(), line2Scripts);
    }

    @EventHandler
    private void onTick(TickEvent.Post event) {
        boolean update = false;

        // Image
        if (ticks >= 200 || forceUpdate) {
            update = true;

            ticks = 0;
        }
        else ticks++;

        if (Utils.canUpdate()) {
            // Line 1
            if (line1Ticks >= line1UpdateDelay.get() || forceUpdate) {
                if (line1Scripts.size() > 0) {
                    int i = Utils.random(0, line1Scripts.size());
                    if (line1SelectMode.get() == SelectMode.Sequential) {
                        if (line1I >= line1Scripts.size()) line1I = 0;
                        i = line1I++;
                    }

                    try {
                        rpc.setDetails(MeteorStarscript.ss.run(line1Scripts.get(i)).toString());
                    } catch (StarscriptError e) {
                        ChatUtils.error("Starscript", e.getMessage());
                    }
                }
                update = true;

                line1Ticks = 0;
            } else line1Ticks++;

            // Line 2
            if (line2Ticks >= line2UpdateDelay.get() || forceUpdate) {
                if (line2Scripts.size() > 0) {
                    int i = Utils.random(0, line2Scripts.size());
                    if (line2SelectMode.get() == SelectMode.Sequential) {
                        if (line2I >= line2Scripts.size()) line2I = 0;
                        i = line2I++;
                    }

                    try {
                        rpc.setDetails(MeteorStarscript.ss.run(line2Scripts.get(i)).toString());
                    } catch (StarscriptError e) {
                        ChatUtils.error("Starscript", e.getMessage());
                    }
                }
                update = true;

                line2Ticks = 0;
            } else line2Ticks++;
        }
        else {
            if (!lastWasInMainMenu) {
                rpc.setDetails("Number81 on top!");

                update = true;
            }
        }

        // Update
        if (update) DiscordIPC.setActivity(rpc);
        forceUpdate = false;
        lastWasInMainMenu = !Utils.canUpdate();
    }

    @Override
    public WWidget getWidget(GuiTheme theme) {
        WButton help = theme.button("Open documentation.");
        help.action = () -> Util.getOperatingSystem().open("https://github.com/MeteorDevelopment/meteor-client/wiki/Starscript");

        return help;
    }
}