package cqb13.NumbyHack.modules.hud;

import cqb13.NumbyHack.NumbyHack;
import meteordevelopment.meteorclient.systems.hud.HudElementInfo;
import meteordevelopment.meteorclient.systems.hud.elements.TextHud;

public class TextPresets {
  public static final HudElementInfo<TextHud> INFO = new HudElementInfo<>(NumbyHack.HUD_GROUP, "numby-text",
      "Displays arbitrary text with Starscript.", TextPresets::create);

  static {
    addPreset("81", "Number81 on top!", 0);
    addPreset("Kills", "Kills: #1{numbyhack.kills}", 0);
    addPreset("Deaths", "Deaths: #1{numbyhack.deaths}", 0);
    addPreset("KDR", "KDR: #1{numbyhack.kdr}", 0);
    addPreset("Highscore", "Highscore: #1{numbyhack.highscore}", 0);
    addPreset("Killstreak", "Killstreak: #1{numbyhack.killstreak}", 0);
    addPreset("Crystals/s", "Crystals/s: #1{numbyhack.crystalsps}", 0);
  }

  private static TextHud create() {
    return new TextHud(INFO);
  }

  private static HudElementInfo<TextHud>.Preset addPreset(String title, String text, int updateDelay) {
    return INFO.addPreset(title, textHud -> {
      if (text != null)
        textHud.text.set(text);
      if (updateDelay != -1)
        textHud.updateDelay.set(updateDelay);
    });
  }
}
