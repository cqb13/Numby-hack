package cqb13.NumbyHack.modules.commands;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;

import meteordevelopment.meteorclient.commands.Command;
import net.minecraft.command.CommandSource;

public class ClearChat extends Command {
    public ClearChat() {
        super("clear-chat", "Clears your chat.", "clear", "cls");
    }

    @Override
    public void build(LiteralArgumentBuilder<CommandSource> builder) {
        builder.executes(context -> {
            mc.inGameHud.getChatHud().clear(false);
            return SINGLE_SUCCESS;
        });
    }
}
