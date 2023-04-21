package cqb13.NumbyHack;

import com.mojang.logging.LogUtils;
import cqb13.NumbyHack.modules.commands.*;
import cqb13.NumbyHack.modules.general.*;
import cqb13.NumbyHack.modules.hud.*;
import cqb13.NumbyHack.utils.*;
import meteordevelopment.meteorclient.addons.MeteorAddon;
import meteordevelopment.meteorclient.systems.commands.Commands;
import meteordevelopment.meteorclient.systems.hud.Hud;
import meteordevelopment.meteorclient.systems.hud.HudGroup;
import meteordevelopment.meteorclient.systems.modules.Category;
import meteordevelopment.meteorclient.systems.modules.Modules;
import meteordevelopment.meteorclient.utils.misc.MeteorStarscript;
import meteordevelopment.starscript.value.Value;
import meteordevelopment.starscript.value.ValueMap;
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
				.set("brand", NumbyHackStarscript::getServerBrand)
				.set("crystalsps", NumbyHackStarscript::getCrystalsPs)
				.set("baritone", NumbyHackStarscript::getBaritoneAction))
		);

		Log("Adding modules...");
		Modules.get().add(new AutoLogPlus());
		Modules.get().add(new BetterPlace());
		Modules.get().add(new Beyblade());
		Modules.get().add(new ChatEncryption());
		Modules.get().add(new ChorusExploit());
		Modules.get().add(new ConditionToggle());
		Modules.get().add(new Confetti());
		Modules.get().add(new FloRida());
		Modules.get().add(new GameSettings());
		Modules.get().add(new GodBridge());
		Modules.get().add(new IgnoreDeaths());
		Modules.get().add(new NewChunks());
		Modules.get().add(new NoStrip());
		Modules.get().add(new Number81());
		Modules.get().add(new RideStats());
		Modules.get().add(new SafeFire());
		Modules.get().add(new SafetyNet());
		Modules.get().add(new TanukiEgapFinder());
		Modules.get().add(new TunnelESP());
		Modules.get().add(new WurstGlide());

		Log("Adding HUD modules...");
		Hud.get().register(ItemCounter.INFO);
		Hud.get().register(Keys.INFO);
		Hud.get().register(Logo.INFO);
		Hud.get().register(TextPresets.INFO);
		Hud.get().register(TextRadarHud.INFO);

		Log("Adding Commands...");
		Commands.get().add(new Trash());
		Commands.get().add(new HeadItem());
		Commands.get().add(new ClearChat());

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
