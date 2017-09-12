package ovh.not.javamusicbot.command;

import ovh.not.javamusicbot.Command;
import ovh.not.javamusicbot.GuildManager;
import ovh.not.javamusicbot.MusicManager;

public class PauseCommand extends Command {
    public PauseCommand() {
        super("pause", "resume");
    }

    @Override
    public void on(Context context) {
        MusicManager musicManager = GuildManager.getInstance().getMusicManager(context.getEvent().getGuild());
        if (!musicManager.isPlayingMusic()) {
            context.reply("No music is playing on this guild! To play a song use `{{prefix}}play`");
            return;
        }

        boolean action = !musicManager.getPlayer().isPaused();
        musicManager.getPlayer().setPaused(action);

        if (action) {
            context.reply("Paused music playback! Use `{{prefix}}resume` to resume.");
        } else {
            context.reply("Resumed music playback!");
        }
    }
}
