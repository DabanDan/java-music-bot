package ovh.not.javamusicbot.command;

import ovh.not.javamusicbot.Command;
import ovh.not.javamusicbot.CommandContext;
import ovh.not.javamusicbot.GuildManager;
import ovh.not.javamusicbot.MusicManager;

public class LoopCommand extends Command {
    public LoopCommand() {
        super("loop");
    }

    @Override
    public void on(CommandContext context) {
        MusicManager musicManager = GuildManager.getInstance().getMusicManager(context.getEvent().getGuild());
        if (!musicManager.isPlayingMusic()) {
            context.reply("No music is playing on this guild! To play a song use `{{prefix}}play`");
            return;
        }

        boolean loop = !musicManager.getTrackScheduler().isLoop();
        musicManager.getTrackScheduler().setLoop(loop);

        context.reply("**%s** queue looping!", loop ? "Enabled" : "Disabled");
    }
}
