package me.madeq.client.command.impl;

import me.madeq.client.command.Command;
import me.madeq.client.command.CommandContext;
import me.madeq.client.module.ModuleType;

public class AuthorCommand extends Command {
    public AuthorCommand() {
        super("author", "Informations about author", ModuleType.COMMAND, true, true);
    }

    @Override
    public void executeCommand(CommandContext context) {
        context.sendMessage("<white>Authors of client are: <aqua>madeq <white>and <aqua>0WhiteDev");
        context.sendMessage("<white>Client powered by <aqua>XynisTeam");
        context.sendMessage(" ");
        context.sendMessage("<gray>Contact with us:");
        context.sendMessage("<white>Discord: <aqua>https://discord.gg/Ks3DhjzJgn");
    }
}
