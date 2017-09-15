package ovh.not.javamusicbot.command;

import ovh.not.javamusicbot.CommandContext;
import ovh.not.javamusicbot.GuildManager;
import ovh.not.javamusicbot.MusicManager;
import ovh.not.javamusicbot.command.base.AbstractTextResponseCommand;
import ovh.not.javamusicbot.command.base.PipelineHandlers;

public class PauseCommand extends AbstractTextResponseCommand {
    public PauseCommand() {
        super("pause", "resume", "unpause");

        // ensure a song is playing
        this.getPipeline().before(PipelineHandlers.requiresMusicHandler());
    }

    @Override
    public String textResponse(CommandContext context) {
        MusicManager musicManager = GuildManager.getInstance().getMusicManager(context.getEvent().getGuild());

        boolean action = !musicManager.getPlayer().isPaused();
        musicManager.getPlayer().setPaused(action);

        if (action) {
            return "Paused music playback! Use `{{prefix}}resume` to resume.";
        } else {
            return "Resumed music playback!";
        }
    }
}
