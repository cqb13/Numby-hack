package cqb13.NumbyHack;

import cqb13.NumbyHack.modules.commands.*;
import cqb13.NumbyHack.modules.general.*;
import cqb13.NumbyHack.modules.hud.*;
import cqb13.NumbyHack.utils.*;
import meteordevelopment.meteorclient.MeteorClient;
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

import java.lang.invoke.MethodHandles;

public class NumbyHack extends MeteorAddon {
	public static final Category CATEGORY = new Category("Numby Hack", Items.TURTLE_HELMET.getDefaultStack());
	public static final HudGroup HUD_GROUP = new HudGroup("Numby Hack");

	@Override
	public void onInitialize() {
	    Log("Beginning initialization.");

		// Required when using @EventHandler
		MeteorClient.EVENT_BUS.registerLambdaFactory("cqb13.NumbyHack", (lookupInMethod, klass) -> (MethodHandles.Lookup) lookupInMethod.invoke(null, klass, MethodHandles.lookup()));

		Log("Adding Player Particles...");
		new PlayerParticle();

		MeteorStarscript.ss.set("numbyhack", Value.map(new ValueMap()
				.set("kills", StatsUtils::getKills)
				.set("deaths", StatsUtils::getDeaths)
				.set("kdr", StatsUtils::getKDR)
				.set("killstreak", StatsUtils::getKillstreak)
				.set("highscore", StatsUtils::getHighscore)
				.set("crystalsps", StatsUtils::getCrystalsPs))
		);

		Modules modules = Modules.get();
		Hud hud = Hud.get();

		Log("Adding modules...");
		modules.add(new AutoLogPlus());
		modules.add(new ChatEncryption());
		modules.add(new ChorusExploit());
		modules.add(new Confetti());
		modules.add(new NewChunks());
		modules.add(new Number81());
		modules.add(new SafeFire());
		modules.add(new SafetyNet());
		modules.add(new TunnelESP());

		Log("Adding HUD modules...");
		hud.register(CombatHUD.INFO);
		hud.register(ItemCounter.INFO);
		hud.register(Logo.INFO);
		hud.register(TextPresets.INFO);

		Log("Adding Commands...");
		Commands commands = Commands.get();
		commands.add(new Trash());

		Log("Initialized successfully!");
	}

	@Override
	public void onRegisterCategories() {
		Modules.registerCategory(CATEGORY);
	}

	public static void Log(String text) {
		System.out.println("[Numby Hack] " + text);
	}
}
