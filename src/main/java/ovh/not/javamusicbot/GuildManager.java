package ovh.not.javamusicbot;

import lavalink.client.io.Lavalink;
import net.dv8tion.jda.core.entities.Guild;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class GuildManager {
    private static final GuildManager INSTANCE = new GuildManager();

    private final Map<String, MusicManager> musicManagers = new HashMap<>();

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

    public static GuildManager getInstance() {
        return INSTANCE;
    }
}
