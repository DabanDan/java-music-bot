package ovh.not.javamusicbot.command;

import ovh.not.javamusicbot.Command;
import ovh.not.javamusicbot.CommandContext;
import ovh.not.javamusicbot.GuildManager;
import ovh.not.javamusicbot.MusicManager;

public class RepeatCommand extends Command {
    public RepeatCommand() {
        super("repeat");
    }

    @Override
    public void on(CommandContext context) {
        MusicManager musicManager = GuildManager.getInstance().getMusicManager(context.getEvent().getGuild());
        if (!musicManager.isPlayingMusic()) {
            context.reply("No music is playing on this guild! To play a song use `{{prefix}}play`");
            return;
        }

        boolean repeat = !musicManager.getTrackScheduler().isRepeat();
        musicManager.getTrackScheduler().setRepeat(repeat);

        context.reply("**%s** song repeating!", repeat ? "Enabled" : "Disabled");
    }
}
