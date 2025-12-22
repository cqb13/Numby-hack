package cqb13.NumbyHack;

import com.mojang.logging.LogUtils;
import cqb13.NumbyHack.modules.commands.*;
import cqb13.NumbyHack.modules.general.*;
import cqb13.NumbyHack.modules.hud.*;
import cqb13.NumbyHack.utils.*;
import meteordevelopment.meteorclient.addons.MeteorAddon;
import meteordevelopment.meteorclient.commands.Commands;
import meteordevelopment.meteorclient.systems.Systems;
import meteordevelopment.meteorclient.systems.hud.Hud;
import meteordevelopment.meteorclient.systems.hud.HudGroup;
import meteordevelopment.meteorclient.systems.modules.Category;
import meteordevelopment.meteorclient.systems.modules.Modules;
import meteordevelopment.meteorclient.utils.misc.MeteorStarscript;
import org.meteordev.starscript.value.Value;
import org.meteordev.starscript.value.ValueMap;
import net.minecraft.item.Items;
import org.slf4j.Logger;

public class NumbyHack extends MeteorAddon {
    public static final Category CATEGORY = new Category("Numby Hack", Items.TURTLE_HELMET.getDefaultStack());
    public static final HudGroup HUD_GROUP = new HudGroup("Numby Hack");
    public static final Logger LOGGER = LogUtils.getLogger();

    @Override
    public void onInitialize() {
        Log("Beginning initialization.");

        Log("Adding Player Particles...");
        PlayerParticle.init();

        NumbyHackStarscript.init();
        MeteorStarscript.ss.set("numbyhack", Value.map(new ValueMap()
                .set("kills", NumbyHackStarscript::getKills)
                .set("deaths", NumbyHackStarscript::getDeaths)
                .set("kdr", NumbyHackStarscript::getKDR)
                .set("killstreak", NumbyHackStarscript::getKillstreak)
                .set("highscore", NumbyHackStarscript::getHighscore)
                .set("crystalsps", NumbyHackStarscript::getCrystalsPs)));

        Log("Adding modules...");
        Modules modules = Modules.get();
        modules.add(new AutoLogPlus());
        modules.add(new BetterPlace());
        modules.add(new Beyblade());
        modules.add(new BonemealAura());
        modules.add(new CarpetPlacer());
        modules.add(new ChatEncryption());
        modules.add(new ConditionToggle());
        modules.add(new Confetti());
        modules.add(new GameSettings());
        modules.add(new IgnoreDeaths());
        modules.add(new JumpHelper());
        modules.add(new LogOutSpots());
        modules.add(new NewChunks());
        modules.add(new NoStrip());
        modules.add(new Number81());
        modules.add(new PacketDelay());
        modules.add(new RideStats());
        modules.add(new SafeFire());
        modules.add(new SafetyNet());
        modules.add(new SpawnerEsp());
        modules.add(new EgapFinder());
        modules.add(new WurstGlide());

        Log("Adding HUD modules...");
        Hud hud = Systems.get(Hud.class);
        hud.register(ItemCounter.INFO);
        hud.register(Keys.INFO);
        hud.register(LavaWarning.INFO);
        hud.register(TextPresets.INFO);
        hud.register(TextRadarHud.INFO);

        Log("Adding Commands...");
        Commands.add(new ClearChat());
        Commands.add(new CoordinateConverter());

        Log("Initialized successfully!");
    }

    @Override
    public void onRegisterCategories() {
        Modules.registerCategory(CATEGORY);
    }

    @Override
    public String getPackage() {
        return "cqb13.NumbyHack";
    }

    public static void Log(String text) {
        LOGGER.info(text);
    }
}
