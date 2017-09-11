package ovh.not.javamusicbot;

import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import com.sedmelluq.discord.lavaplayer.track.AudioTrackEndReason;
import lavalink.client.player.event.AudioEventAdapterWrapped;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

public class TrackScheduler extends AudioEventAdapterWrapped {
    private static final Logger logger = LoggerFactory.getLogger(TrackScheduler.class);

    private final MusicManager musicManager;

    private final Queue<AudioTrack> queue;
    private boolean repeat = false;
    private boolean loop = false;

    TrackScheduler(MusicManager musicManager) {
        this.musicManager = musicManager;
        this.queue = new LinkedList<>();
    }

    public Queue<AudioTrack> getQueue() {
        return queue;
    }

    public boolean isRepeat() {
        return repeat;
    }

    public void setRepeat(boolean repeat) {
        this.repeat = repeat;
    }

    public boolean isLoop() {
        return loop;
    }

    public void setLoop(boolean loop) {
        this.loop = loop;
    }

    @SuppressWarnings("unchecked")
    public void queue(AudioPlayer player, AudioTrack track, boolean... first) {
        if (!player.startTrack(track, true)) {
            if (first != null && first.length > 0 && first[0]) {
                ((List<AudioTrack>) queue).add(0, track);
            } else {
                queue.offer(track);
            }
        }
    }

    public void next(AudioPlayer player, AudioTrack last) {
        AudioTrack track;
        if (repeat && last != null) {
            track = last.makeClone();
        } else {
            if (loop && last != null) {
                queue.add(last.makeClone());
            }
            track = queue.poll();
        }
        if (!player.startTrack(track, false)) {
            musicManager.close();
        }
    }

    @Override
    public void onTrackEnd(AudioPlayer player, AudioTrack track, AudioTrackEndReason endReason) {
        if (endReason.mayStartNext) {
            next(player, track);
        }
    }

    @Override
    public void onTrackStart(AudioPlayer player, AudioTrack track) {
        String message = String.format("Now playing **%s** by **%s** `[%s]`", track.getInfo().title,
                track.getInfo().author, Utils.formatTrackDuration(track));

        musicManager.getTextChannel().sendMessage(message).queue();
    }

    @Override
    public void onTrackException(AudioPlayer player, AudioTrack track, FriendlyException exception) {
        logger.error("track exception for track " + track.getInfo(), exception);
    }

    @Override
    public void onTrackStuck(AudioPlayer player, AudioTrack track, long thresholdMs) {
        logger.error("track {} stuck with thresholdMs {}", track.getIdentifier(), thresholdMs);
    }
}
