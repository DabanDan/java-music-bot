package ovh.not.javamusicbot.command;

import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.managers.AudioManager;
import ovh.not.javamusicbot.Command;
import ovh.not.javamusicbot.MusicManager;

public class StopCommand extends Command {
    public StopCommand() {
        super("stop", "leave", "clear");
    }

    @Override
    public void on(Context context) {
        Guild guild = context.getEvent().getGuild();
        MusicManager musicManager = MusicManager.get(guild);
        if (musicManager != null) {
            musicManager.close();
            musicManager.getScheduler().getQueue().clear();
            musicManager.getScheduler().next(null);
            MusicManager.getGUILDS().remove(context.getEvent().getGuild());
            context.reply("Stopped playing music & left the voice channel.");
        } else {
            AudioManager audioManager = guild.getAudioManager();
            audioManager.closeAudioConnection();
            context.reply("Left the voice channel.");
        }
    }
}
