package ovh.not.javamusicbot.command;

import ovh.not.javamusicbot.CommandContext;
import ovh.not.javamusicbot.GuildManager;
import ovh.not.javamusicbot.MusicManager;
import ovh.not.javamusicbot.command.base.AbstractTextResponseCommand;
import ovh.not.javamusicbot.command.base.PipelineHandlers;

public class LoopCommand extends AbstractTextResponseCommand {
    public LoopCommand() {
        super("loop");

        super.getPipeline().before(PipelineHandlers.requiresMusicHandler());
    }

    @Override
    public String textResponse(CommandContext context) {
        MusicManager musicManager = GuildManager.getInstance().getMusicManager(context.getEvent().getGuild());

        boolean loop = !musicManager.getTrackScheduler().isLoop();
        musicManager.getTrackScheduler().setLoop(loop);

        return String.format("**%s** queue looping!", loop ? "Enabled" : "Disabled");
    }
}
