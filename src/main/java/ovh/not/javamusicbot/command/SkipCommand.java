package ovh.not.javamusicbot.command;

import ovh.not.javamusicbot.AbstractCommand;
import ovh.not.javamusicbot.CommandContext;
import ovh.not.javamusicbot.GuildManager;
import ovh.not.javamusicbot.MusicManager;

public class SkipCommand extends AbstractCommand {
    public SkipCommand() {
        super("skip", "s", "next");
    }

    @Override
    public void on(CommandContext context) {
        MusicManager musicManager = GuildManager.getInstance().getMusicManager(context.getEvent().getGuild());
        if (!musicManager.isPlayingMusic()) {
            context.reply("No music is playing on this guild! To play a song use `{{prefix}}play`");
            return;
        }

        musicManager.getTrackScheduler().next(musicManager.getPlayer(), musicManager.getPlayer().getPlayingTrack());
    }
}
