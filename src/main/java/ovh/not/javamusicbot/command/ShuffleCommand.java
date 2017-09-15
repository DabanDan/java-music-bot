package ovh.not.javamusicbot.command;

import ovh.not.javamusicbot.CommandContext;
import ovh.not.javamusicbot.GuildManager;
import ovh.not.javamusicbot.MusicManager;
import ovh.not.javamusicbot.command.base.AbstractTextResponseCommand;
import ovh.not.javamusicbot.command.base.PipelineHandlers;

import java.util.Collections;
import java.util.List;

public class ShuffleCommand extends AbstractTextResponseCommand {
    public ShuffleCommand() {
        super("shuffle");

        getPipeline().before(PipelineHandlers.requiresMusicHandler());
    }

    @Override
    public String textResponse(CommandContext context) {
        MusicManager musicManager = GuildManager.getInstance().getMusicManager(context.getEvent().getGuild());
        if (!musicManager.isPlayingMusic()) {
            return "No music is playing on this guild! To play a song use `{{prefix}}play`";
        }

        Collections.shuffle((List<?>) musicManager.getTrackScheduler().getQueue());
        return "Queue shuffled!";
    }
}
