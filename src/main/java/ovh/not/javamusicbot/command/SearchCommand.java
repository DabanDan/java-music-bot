package ovh.not.javamusicbot.command;

import ovh.not.javamusicbot.AbstractCommand;
import ovh.not.javamusicbot.CommandContext;
import ovh.not.javamusicbot.CommandManager;

public class SearchCommand extends AbstractCommand {
    private final CommandManager commandManager;

    public SearchCommand(CommandManager commandManager) {
        super("search", "lookup", "youtube", "yt", "find");
        this.commandManager = commandManager;
    }

    @Override
    public void on(CommandContext context) {
        // todo change to {{prefix}}search [youtube/soundcloud...] <term> (and default to youtube)

        if (context.getArgs().isEmpty()) {
            context.reply("Usage: `{{prefix}}search <term>` - searches for a song on youtube\n" +
                    "To add the song as first in the queue, use `{{prefix}}search <term> -first`");
            return;
        }
        context.getArgs().add(0, "ytsearch:");
        commandManager.getCommands().get("play").on(context);
    }
}
