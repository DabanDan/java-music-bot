package ovh.not.javamusicbot.command;

import ovh.not.javamusicbot.CommandContext;
import ovh.not.javamusicbot.CommandManager;
import ovh.not.javamusicbot.command.base.AbstractNoResponsePipelineCommand;
import ovh.not.javamusicbot.command.base.PipelineHandlers;

public class SearchCommand extends AbstractNoResponsePipelineCommand {
    private static final String INVALID_ARGUMENTS_MESSAGE = "Usage: `{{prefix}}search <term>` - searches for a song " +
            "on youtube\nTo add the song as first in the queue, use `{{prefix}}search <term> -first`";

    private final CommandManager commandManager;

    public SearchCommand(CommandManager commandManager) {
        super("search", "lookup", "youtube", "yt", "find");
        this.commandManager = commandManager;

        getPipeline().before(PipelineHandlers.argumentCheckHandler(INVALID_ARGUMENTS_MESSAGE, 1));
    }

    @Override
    public void noResponse(CommandContext context) {
        // todo change to {{prefix}}search [youtube/soundcloud...] <term> (and default to youtube)
        context.getArgs().add(0, "ytsearch:");
        commandManager.getCommands().get("play").on(context);
    }
}
