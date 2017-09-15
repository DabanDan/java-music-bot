package ovh.not.javamusicbot.command;

import ovh.not.javamusicbot.CommandContext;
import ovh.not.javamusicbot.GuildManager;
import ovh.not.javamusicbot.MusicManager;
import ovh.not.javamusicbot.command.base.AbstractTextResponseCommand;
import ovh.not.javamusicbot.command.base.PipelineHandlers;

public class RepeatCommand extends AbstractTextResponseCommand {
    public RepeatCommand() {
        super("repeat");

        getPipeline().before(PipelineHandlers.requiresMusicHandler());
    }

    @Override
    public String textResponse(CommandContext context) {
        MusicManager musicManager = GuildManager.getInstance().getMusicManager(context.getEvent().getGuild());

        boolean repeat = !musicManager.getTrackScheduler().isRepeat();
        musicManager.getTrackScheduler().setRepeat(repeat);

        return String.format("**%s** song repeating!", repeat ? "Enabled" : "Disabled");
    }
}
