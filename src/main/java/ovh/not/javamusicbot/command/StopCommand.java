package ovh.not.javamusicbot.command;

import net.dv8tion.jda.core.entities.Guild;
import ovh.not.javamusicbot.Command;
import ovh.not.javamusicbot.CommandContext;
import ovh.not.javamusicbot.GuildManager;
import ovh.not.javamusicbot.MusicManager;

public class StopCommand extends Command {
    public StopCommand() {
        super("stop", "leave", "clear");
    }

    @Override
    public void on(CommandContext context) {
        Guild guild = context.getEvent().getGuild();
        MusicManager musicManager = GuildManager.getInstance().getMusicManager(guild);
        if (musicManager != null) {
            musicManager.close();
            musicManager.getTrackScheduler().getQueue().clear();
            musicManager.getTrackScheduler().next(musicManager.getPlayer(), null);
            context.reply("Stopped playing music & left the voice channel.");
        } else {
            context.reply("Tried to stop but was not already playing :eyes:");
        }
    }
}
