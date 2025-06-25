package cqb13.NumbyHack.modules.general;

import cqb13.NumbyHack.NumbyHack;
import meteordevelopment.meteorclient.events.game.ReceiveMessageEvent;
import meteordevelopment.meteorclient.settings.BoolSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.settings.StringListSetting;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.text.Text;

import java.util.List;

public class IgnoreDeaths extends Module {
  private final SettingGroup sgGeneral = settings.getDefaultGroup();

  private final Setting<List<String>> names = sgGeneral.add(new StringListSetting.Builder()
      .name("player names")
      .description("The names of players who's death messages you wish to hide.")
      .defaultValue(List.of())
      .build());

  private final Setting<Boolean> mustContainWords = sgGeneral.add(new BoolSetting.Builder()
      .name("must contain words")
      .description("Will only  ignore the message if it contains specified words and player name.")
      .defaultValue(false)
      .build());

  private final Setting<List<String>> blockedWords = sgGeneral.add(new StringListSetting.Builder()
      .name("blocked words")
      .description("The list of words that will be cause a message to be blocked.")
      .defaultValue(List.of())
      .visible(mustContainWords::get)
      .build());

  public IgnoreDeaths() {
    super(NumbyHack.CATEGORY, "ignore-deaths", "Removes chat messages containing a death spammers name from chat.");
  }

  @EventHandler
  private void onMessageReceive(ReceiveMessageEvent event) {
    Text message = event.getMessage();

    if (message == null)
      return;

    message = Text.of(message.getString().toLowerCase());

    if (mustContainWords.get()) {
      for (String name : names.get()) {
        for (String word : blockedWords.get()) {
          if (message.getString().contains(name) && message.getString().contains(word.toLowerCase())) {
            event.cancel();
          }
        }
      }
    } else {
      for (String name : names.get()) {
        if (message.getString().contains(name)) {
          event.cancel();
        }
      }
    }

    event.setMessage(message);
  }
}
