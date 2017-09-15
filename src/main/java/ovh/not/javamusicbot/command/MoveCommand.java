package ovh.not.javamusicbot.command;

import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.VoiceChannel;
import ovh.not.javamusicbot.*;

import java.util.List;

public class MoveCommand extends AbstractCommand {
    public MoveCommand() {
        super("move");
    }

    @Override
    public void on(CommandContext context) {
        if (context.getArgs().isEmpty()) {
            context.reply("Usage: `{{prefix}}move <voice channel name>`");
            return;
        }

        Guild guild = context.getEvent().getGuild();
        MusicManager musicManager = GuildManager.getInstance().getMusicManager(guild);

        if (!musicManager.isPlayingMusic()) {
            context.reply("No music is playing on this guild! To play a song use `{{prefix}}play`");
            return;
        }

        if (Utils.warnIfBotInUse(musicManager, context)) return;

        List<VoiceChannel> channels = guild.getVoiceChannelsByName(String.join(" ", context.getArgs()), true);
        if (channels == null || channels.isEmpty()) {
            context.reply("Could not find the specified voice channel! Are you sure I have permission to connect to it?");
            return;
        }
        VoiceChannel channel = channels.get(0);

        // pause music and close the voice connection
        musicManager.getPlayer().setPaused(true);
        musicManager.close();

        // open the new voice connection and resume music
        musicManager.open(channel);
        musicManager.getPlayer().setPaused(false);

        context.reply("Moved voice channel!");
    }
}
