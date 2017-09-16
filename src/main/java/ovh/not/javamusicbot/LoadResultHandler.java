package ovh.not.javamusicbot;

import com.sedmelluq.discord.lavaplayer.player.AudioLoadResultHandler;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException;
import com.sedmelluq.discord.lavaplayer.track.AudioPlaylist;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LoadResultHandler implements AudioLoadResultHandler {
    private static final Logger LOGGER = LoggerFactory.getLogger(LoadResultHandler.class);

    private final MusicManager musicManager;
    private final AudioPlayerManager playerManager;
    private final CommandContext context;
  
    private boolean verbose = true;
    private boolean isSearch;
    private boolean allowSearch;
    private boolean setFirstInQueue;

    public LoadResultHandler(MusicManager musicManager, AudioPlayerManager playerManager, CommandContext context) {
        this.musicManager = musicManager;
        this.playerManager = playerManager;
        this.context = context;
    }

    @Override
    public void trackLoaded(AudioTrack audioTrack) {
        boolean playing = musicManager.getPlayer().getPlayingTrack() != null;
        musicManager.getTrackScheduler().queue(musicManager.getPlayer(), audioTrack, setFirstInQueue);

        if (playing && verbose) {
            context.reply(String.format("Queued **%s** `[%s]`", audioTrack.getInfo().title,
                    Utils.formatTrackDuration(audioTrack)));
        }
    }

    @Override
    public void playlistLoaded(AudioPlaylist audioPlaylist) {
        if (audioPlaylist.getSelectedTrack() != null) {
            trackLoaded(audioPlaylist.getSelectedTrack());
        } else if (audioPlaylist.isSearchResult()) {
            int playlistSize = audioPlaylist.getTracks().size();

            if (playlistSize == 0) {
                context.reply("No song matches found! Usage: `{{prefix}}play <link or youtube video title>` or " +
                        "`{{prefix}}soundcloud <soundcloud song title>`");

                if (musicManager.getPlayer().getPlayingTrack() == null && musicManager.getTrackScheduler().getQueue().isEmpty()) {
                    musicManager.close();
                }

                return;
            }

            int size = playlistSize > 5 ? 5 : playlistSize;
            AudioTrack[] audioTracks = new AudioTrack[size];
            for (int i = 0; i < audioTracks.length; i++) {
                audioTracks[i] = audioPlaylist.getTracks().get(i);
            }

            Selection.Formatter<AudioTrack> formatter = track -> String.format("%s by %s `[%s]`",
                    track.getInfo().title, track.getInfo().author, Utils.formatTrackDuration(track));

            Selection<AudioTrack> selection = new Selection<>(audioTracks, formatter, (found, track) -> {
                if (!found) {
                    context.reply("Selection cancelled!");

                    if (musicManager.getPlayer().getPlayingTrack() == null
                            && musicManager.getTrackScheduler().getQueue().isEmpty()) {
                        musicManager.close();
                    }

                    return;
                }
                trackLoaded(track);
            });

            GuildManager.getInstance().addTrackSelector(context.getEvent().getMember(), selection);
            context.reply(selection.createMessage());
        } else {
            audioPlaylist.getTracks()
                    .forEach(track -> musicManager.getTrackScheduler().queue(musicManager.getPlayer(), track));

            context.reply(String.format("Added **%d songs** to the queue!", audioPlaylist.getTracks().size()));
        }
    }

    @Override
    public void noMatches() {
        if (verbose) {
            if (isSearch) {
                context.reply("No song matches found! Usage: `{{prefix}}play <link or youtube video title>` or " +
                        "`{{prefix}}soundcloud <soundcloud song title>`");

                if (context.getEvent().getGuild().getAudioManager().isConnected() &&
                        musicManager.getPlayer().getPlayingTrack() == null
                        && musicManager.getTrackScheduler().getQueue().isEmpty()) {
                    musicManager.close();
                }
            } else if (allowSearch) {
                this.isSearch = true;

                playerManager.loadItem("ytsearch: " + String.join(" ", context.getArgs()), this);
            }
        }
    }

    @Override
    public void loadFailed(FriendlyException e) {
        LOGGER.info("track load failed for query {}", e, String.join(" ", context.getArgs()));

        if (verbose) {
            context.reply("An error occurred: " + e.getMessage() + "\nPlease note that dabBot is primarily hosted in " +
                    "Canada and therefore cannot play songs that are blocked for copyright in Canada.");
        }
    }

    public void setVerbose(boolean verbose) {
        this.verbose = verbose;
    }

    public void setSearch(boolean search) {
        isSearch = search;
    }

    public void setAllowSearch(boolean allowSearch) {
        this.allowSearch = allowSearch;
    }

    public void setSetFirstInQueue(boolean setFirstInQueue) {
        this.setFirstInQueue = setFirstInQueue;
    }
}
