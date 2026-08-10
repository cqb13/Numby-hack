package cqb13.NumbyHack.modules.commands;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;

import meteordevelopment.meteorclient.commands.Command;
import net.minecraft.client.multiplayer.ClientSuggestionProvider;

public class ClearChat extends Command {
    public ClearChat() {
        super("clear-chat", "Clears your chat.", "clear", "cls");
    }

    @Override
    public void build(LiteralArgumentBuilder<ClientSuggestionProvider> builder) {
        builder.executes(context -> {
            mc.gui.hud.getChat().clearMessages(false);
            return SINGLE_SUCCESS;
        });
    }
}
