package ovh.not.javamusicbot.command;

import ovh.not.javamusicbot.Command;
import ovh.not.javamusicbot.MusicManager;

public class RepeatCommand extends Command {
    public RepeatCommand() {
        super("repeat");
    }

    @Override
    public void on(Context context) {
        MusicManager musicManager = MusicManager.get(context.getEvent().getGuild());
        if (musicManager == null || musicManager.getPlayer().getPlayingTrack() == null) {
            context.reply("No music is playing on this guild! To play a song use `{{prefix}}play`");
            return;
        }
        boolean repeat = !musicManager.getScheduler().isRepeat();
        musicManager.getScheduler().setRepeat(repeat);
        context.reply("**%s** song repeating!", repeat ? "Enabled" : "Disabled");
    }
}
