package ovh.not.javamusicbot.command.base;

import net.dv8tion.jda.core.entities.VoiceChannel;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;
import ovh.not.javamusicbot.*;

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

    public static Function<CommandContext, Boolean> requiresNotInUseHandler() {
        return context -> {
            MessageReceivedEvent event = context.getEvent();
            MusicManager musicManager = GuildManager.getInstance().getMusicManager(event.getGuild());

            if (Utils.isBotInUse(musicManager, event.getMember())) {
                context.reply("dabBot is already playing music in %s so it cannot be moved. Members with the " +
                        "`Move Members` permission can do this.", musicManager.getVoiceChannel().get().getName());
                return false;
            } else {
                return true;
            }
        };
    }

    public static Function<CommandContext, Boolean> patronOnlyCommandHandler() {
        return context -> {
            if (!MusicBot.getConfigs().config.patreon) {
                context.reply("**The volume command is dabBot premium only!**\nDonate for the `Super supporter` " +
                        "tier on Patreon at https://patreon.com/dabbot to gain access.");
                return false;
            } else {
                return true;
            }
        };
    }

    /*
    if (!Utils.allowedSuperSupporterPatronAccess(context.getEvent().getGuild())) {
            return "**The volume command is dabBot premium only!**" +
                    "\nDonate for the `Super supporter` tier on Patreon at https://patreon.com/dabbot to gain access.";
        }
     */

    public static Function<CommandContext, Boolean> requireSuperSupporterHandler() {
        return context -> {
            if (!Utils.allowedSuperSupporterPatronAccess(context.getEvent().getGuild())) {
                context.reply("**The volume command is dabBot premium only!**\nDonate for the `Super supporter` tier" +
                        " on Patreon at https://patreon.com/dabbot to gain access.");
                return false;
            } else {
                return true;
            }
        };
    }
}
