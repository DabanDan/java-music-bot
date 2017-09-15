package ovh.not.javamusicbot.command;

import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import ovh.not.javamusicbot.CommandContext;
import ovh.not.javamusicbot.GuildManager;
import ovh.not.javamusicbot.MusicManager;
import ovh.not.javamusicbot.command.base.AbstractTextResponseCommand;
import ovh.not.javamusicbot.command.base.PipelineHandlers;

import static ovh.not.javamusicbot.Utils.formatDuration;
import static ovh.not.javamusicbot.Utils.formatTrackDuration;

public class NowPlayingCommand extends AbstractTextResponseCommand {
    private static final String NOW_PLAYING_FORMAT = "Currently playing **%s** by **%s** `[%s/%s]`\nSong URL: %s";

    public NowPlayingCommand() {
        super("nowplaying", "current", "now", "np");

        // ensure a song is playing
        super.getPipeline().before(PipelineHandlers.requiresMusicHandler());
    }

    @Override
    public String textResponse(CommandContext context) {
        MusicManager musicManager = GuildManager.getInstance().getMusicManager(context.getEvent().getGuild());
        AudioTrack currentTrack = musicManager.getPlayer().getPlayingTrack();

        return String.format(NOW_PLAYING_FORMAT, currentTrack.getInfo().title, currentTrack.getInfo().author,
                formatDuration(currentTrack.getPosition()), formatTrackDuration(currentTrack),
                currentTrack.getInfo().uri);
    }
}
