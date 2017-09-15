package ovh.not.javamusicbot.command.base;

import net.dv8tion.jda.core.entities.VoiceChannel;
import ovh.not.javamusicbot.CommandContext;
import ovh.not.javamusicbot.GuildManager;
import ovh.not.javamusicbot.MusicManager;

import java.util.function.Function;

public class PipelineHandlers {
    public static Function<CommandContext, Boolean> argumentCheckHandler(String message, int... requirements) {
        int min = requirements[0];
        int max = requirements.length > 1 ? requirements[0] : -1;

        return context -> {
            int args = context.getArgs().size();

            if (context.getArgs().size() < min) {
                context.reply(message);
                return false;
            }

            if (max != -1 && args > max) {
                context.reply(message);
                return false;
            }

            return true;
        };
    }

    public static Function<CommandContext, Boolean> requiresMusicHandler() {
        return context -> {
            MusicManager musicManager = GuildManager.getInstance().getMusicManager(context.getEvent().getGuild());

            if (!musicManager.isPlayingMusic()) {
                context.reply("No music is playing on this guild! This command requires a song to be playing. " +
                        "To play a song use `{{prefix}}play`");
                return false;
            } else {
                return true;
            }
        };
    }

    public static Function<CommandContext, Boolean> requiresUserInVoiceChannelHandler() {
        return context -> {
            VoiceChannel channel = context.getEvent().getMember().getVoiceState().getChannel();
            if (channel == null) {
                context.reply("You must be in a voice channel to use this command!");
                return false;
            }
            return true;
        };
    }
}
