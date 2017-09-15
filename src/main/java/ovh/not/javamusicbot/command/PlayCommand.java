package ovh.not.javamusicbot.command;

import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager;
import ovh.not.javamusicbot.CommandManager;
import ovh.not.javamusicbot.command.base.AbstractPlayCommand;
import ovh.not.javamusicbot.command.base.PipelineHandlers;

public class PlayCommand extends AbstractPlayCommand {
    private static final String INVALID_ARGUMENTS_MESSAGE = "Usage: `{{prefix}}play <link>` - plays a song\n" +
            "To search YouTube, use `{{prefix}}play <youtube video title>`\n" +
            "To search SoundCloud, use `{{prefix}}soundcloud <soundcloud song name>`\n" +
            "To add as first in queue, use `{{prefix}}play <link> -first`";

    public PlayCommand(CommandManager commandManager, AudioPlayerManager playerManager) {
        super(commandManager, playerManager, "play", "p");

        getPipeline().before(PipelineHandlers.argumentCheckHandler(INVALID_ARGUMENTS_MESSAGE, 1));
    }
}
