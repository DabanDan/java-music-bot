package ovh.not.javamusicbot.command;

import net.dv8tion.jda.core.entities.Guild;
import ovh.not.javamusicbot.CommandContext;
import ovh.not.javamusicbot.GuildManager;
import ovh.not.javamusicbot.MusicManager;
import ovh.not.javamusicbot.command.base.AbstractTextResponseCommand;

public class StopCommand extends AbstractTextResponseCommand {
    public StopCommand() {
        super("stop", "leave", "clear");
    }

    @Override
    public String textResponse(CommandContext context) {
        Guild guild = context.getEvent().getGuild();
        MusicManager musicManager = GuildManager.getInstance().getMusicManager(guild);
        if (musicManager != null) {
            musicManager.close();
            musicManager.getTrackScheduler().getQueue().clear();
            musicManager.getTrackScheduler().next(musicManager.getPlayer(), null);
            return "Stopped playing music & left the voice channel.";
        } else {
            return "Tried to stop but was not already playing :eyes:";
        }
    }
}
