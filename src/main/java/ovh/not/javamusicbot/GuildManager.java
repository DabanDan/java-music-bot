package ovh.not.javamusicbot;

import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import lavalink.client.io.Lavalink;
import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.Member;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class GuildManager {
    private static final GuildManager INSTANCE = new GuildManager();

    private final Map<String, MusicManager> musicManagers = new HashMap<>();
    private final Map<Member, Selection<AudioTrack>> trackSelectors = new HashMap<>();

    private Optional<Lavalink> lavalink = Optional.empty();

    void setLavalink(Lavalink lavalink) {
        this.lavalink = Optional.of(lavalink);
    }

    public boolean hasMusicManager(String guildId) {
        return musicManagers.containsKey(guildId);
    }

    public MusicManager getMusicManager(Guild guild) {
        return musicManagers.computeIfAbsent(guild.getId(), k -> new MusicManager(lavalink.orElseThrow(() ->
                        new RuntimeException("lavalink must be present to get a guild's MusicManager")), guild));
    }

    public Optional<Selection<AudioTrack>> getTrackSelector(Member member) {
        Selection<AudioTrack> trackSelector = trackSelectors.get(member);

        if (trackSelector == null) {
            return Optional.of(trackSelector);
        } else {
            return Optional.empty();
        }
    }

    public void addTrackSelector(Member member, Selection<AudioTrack> trackSelector) {
        trackSelectors.put(member, trackSelector);
    }

    public void removeTrackSelector(Member member) {
        trackSelectors.remove(member);
    }

    public static GuildManager getInstance() {
        return INSTANCE;
    }
}
