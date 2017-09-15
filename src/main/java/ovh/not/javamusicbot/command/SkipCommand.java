package ovh.not.javamusicbot.command;

import ovh.not.javamusicbot.CommandContext;
import ovh.not.javamusicbot.GuildManager;
import ovh.not.javamusicbot.MusicManager;
import ovh.not.javamusicbot.command.base.AbstractNoResponsePipelineCommand;
import ovh.not.javamusicbot.command.base.PipelineHandlers;

public class SkipCommand extends AbstractNoResponsePipelineCommand {
    public SkipCommand() {
        super("skip", "s", "next");

        getPipeline().before(PipelineHandlers.requiresMusicHandler());
    }

    @Override
    public void noResponse(CommandContext context) {
        MusicManager musicManager = GuildManager.getInstance().getMusicManager(context.getEvent().getGuild());
        musicManager.getTrackScheduler().next(musicManager.getPlayer(), musicManager.getPlayer().getPlayingTrack());
    }
}
