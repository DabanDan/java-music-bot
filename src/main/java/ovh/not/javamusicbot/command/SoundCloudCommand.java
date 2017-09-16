package ovh.not.javamusicbot.command;

import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager;
import ovh.not.javamusicbot.command.base.AbstractPlayCommand;
import ovh.not.javamusicbot.command.base.PipelineHandlers;

public class SoundCloudCommand extends AbstractPlayCommand {
    private static final String INVALID_ARGUMENTS_MESSAGE = "Usage: `{{prefix}}soundcloud <song title>` - searches " +
            "for a song from soundcloud\n\nIf you already have a link to a song, use `{{prefix}}play <link>`";;

    public SoundCloudCommand(AudioPlayerManager playerManager) {
        super(playerManager, "soundcloud", "sc");
        // so that when the LoadResultHandler fails it doesn't try to search on youtube :ok_hand:
        this.allowSearch = false;
        this.isSearch = true;

        // check for valid args and add scsearch to the start of the identifier
        getPipeline()
                .before(PipelineHandlers.argumentCheckHandler(INVALID_ARGUMENTS_MESSAGE, 1))
                .before(context -> {
                    context.getArgs().add(0, "scsearch:");
                    return true;
                });
    }
}
