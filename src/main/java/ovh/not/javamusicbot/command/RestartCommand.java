package ovh.not.javamusicbot.command;

import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import ovh.not.javamusicbot.AbstractCommand;
import ovh.not.javamusicbot.CommandContext;
import ovh.not.javamusicbot.GuildManager;
import ovh.not.javamusicbot.MusicManager;

import static ovh.not.javamusicbot.Utils.formatTrackDuration;

public class RestartCommand extends AbstractCommand {
    public RestartCommand() {
        super("restart");
    }

    @Override
    public void on(CommandContext context) {
        MusicManager musicManager = GuildManager.getInstance().getMusicManager(context.getEvent().getGuild());
        if (!musicManager.isPlayingMusic()) {
            context.reply("No music is playing on this guild! To play a song use `{{prefix}}play`");
            return;
        }

        AudioTrack currentTrack = musicManager.getPlayer().getPlayingTrack();
        currentTrack.setPosition(0);

        context.reply("Restarted **%s** by **%s** `[%s]`", currentTrack.getInfo().title,
                currentTrack.getInfo().author, formatTrackDuration(currentTrack));
    }
}
