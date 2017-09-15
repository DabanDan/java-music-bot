package ovh.not.javamusicbot.command;

import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import ovh.not.javamusicbot.CommandContext;
import ovh.not.javamusicbot.GuildManager;
import ovh.not.javamusicbot.MusicManager;
import ovh.not.javamusicbot.command.base.AbstractTextResponseCommand;
import ovh.not.javamusicbot.command.base.PipelineHandlers;

import static ovh.not.javamusicbot.Utils.formatTrackDuration;

public class RestartCommand extends AbstractTextResponseCommand {
    public RestartCommand() {
        super("restart");

        getPipeline().before(PipelineHandlers.requiresMusicHandler());
    }

    @Override
    public String textResponse(CommandContext context) {
        MusicManager musicManager = GuildManager.getInstance().getMusicManager(context.getEvent().getGuild());

        AudioTrack currentTrack = musicManager.getPlayer().getPlayingTrack();
        currentTrack.setPosition(0);

        return String.format("Restarted **%s** by **%s** `[%s]`", currentTrack.getInfo().title,
                currentTrack.getInfo().author, formatTrackDuration(currentTrack));
    }
}
