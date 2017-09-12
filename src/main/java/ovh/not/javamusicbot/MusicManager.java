package ovh.not.javamusicbot;

import lavalink.client.io.Lavalink;
import lavalink.client.player.LavalinkPlayer;
import net.dv8tion.jda.core.Permission;
import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.Member;
import net.dv8tion.jda.core.entities.TextChannel;
import net.dv8tion.jda.core.entities.VoiceChannel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Optional;

public class MusicManager {
    private static final Logger LOGGER = LoggerFactory.getLogger(MusicManager.class);

    private final Lavalink lavalink;
    private final Guild guild;
    private final TrackScheduler trackScheduler;

    private Optional<String> textChannelId = Optional.empty();
    private Optional<String> voiceChannelId = Optional.empty();

    MusicManager(Lavalink lavalink, Guild guild) {
        this.lavalink = lavalink;
        this.guild = guild;

        // register the track scheduler
        this.trackScheduler = new TrackScheduler(this);
        this.lavalink.getPlayer(guild.getId()).addListener(trackScheduler);
    }

    public LavalinkPlayer getPlayer() {
        return lavalink.getPlayer(guild.getId());
    }

    public Guild getGuild() {
        return guild;
    }

    public TrackScheduler getTrackScheduler() {
        return trackScheduler;
    }

    public TextChannel getTextChannel() {
        return textChannelId
                .map(guild::getTextChannelById)
                .orElseGet(guild::getDefaultChannel);
    }

    public boolean hasTextChannel() {
        return textChannelId.isPresent();
    }

    public void setTextChannelIfNotPresent(TextChannel textChannel) {
        if (!hasTextChannel()) textChannelId = Optional.of(textChannel.getId());
    }

    public Optional<VoiceChannel> getVoiceChannel() {
        return voiceChannelId.map(guild::getVoiceChannelById);
    }

    public boolean isVoiceChannelEmpty() {
        return Utils.isVoiceChannelEmpty(getVoiceChannel());
    }

    public boolean isOpen() {
        return voiceChannelId.isPresent();
    }

    public void open(VoiceChannel voiceChannel) {
        Guild guild = voiceChannel.getGuild();
        Member selfMember = guild.getSelfMember();

        if (!selfMember.hasPermission(voiceChannel, Permission.VOICE_CONNECT, Permission.VOICE_SPEAK)) {
            String message = "**dabBot does not have enough permissions to play music in the %s voice channel!**" +
                    "\nTo fix this, give dabBot permission to `Connect` and to `Speak` in that voice channel." +
                    "\nIf you are not the guild owner, please send this to them.";

            getTextChannel().sendMessage(String.format(message, voiceChannel.getName())).queue();
            return;
        }

        lavalink.openVoiceConnection(voiceChannel);
        voiceChannelId = Optional.of(voiceChannel.getId());

        LOGGER.info("opened connection to voice channel {}", voiceChannel.getId());
    }

    public void close() {
        lavalink.closeVoiceConnection(guild);
        voiceChannelId = Optional.empty();

        LOGGER.info("closed voice connection for guild {}", guild.getId());
    }

    public boolean isPlayingMusic() {
        return isOpen() && getPlayer().getPlayingTrack() != null;
    }
}
