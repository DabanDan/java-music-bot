package ovh.not.javamusicbot.command;

import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager;
import net.dv8tion.jda.core.Permission;
import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.VoiceChannel;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;
import ovh.not.javamusicbot.*;

import java.util.Set;

abstract class BasePlayCommand extends Command {
    private final CommandManager commandManager;
    private final AudioPlayerManager playerManager;
    boolean allowSearch = true;
    boolean isSearch = false;

    BasePlayCommand(CommandManager commandManager, AudioPlayerManager playerManager, String name, String... names) {
        super(name, names);
        this.commandManager = commandManager;
        this.playerManager = playerManager;
    }

    @Override
    public void on(Context context) {
        if (context.getArgs().length == 0) {
            context.reply(this.noArgumentMessage());
            return;
        }
      
        VoiceChannel channel = context.getEvent().getMember().getVoiceState().getChannel();
        if (channel == null) {
            context.reply("You must be in a voice channel!");
            return;
        }

        MessageReceivedEvent event = context.getEvent();
        Guild guild = event.getGuild();

        MusicManager musicManager = GuildManager.getInstance().getMusicManager(guild);

        // if the bot is playing music and the voice channel is not empty
        if (musicManager.isPlayingMusic() && !musicManager.isVoiceChannelEmpty() && event.getMember().hasPermission(Permission.VOICE_MOVE_OTHERS)) {
            context.reply("dabBot is already playing music in %s so it cannot be moved. Members with the " +
                    "`Move Members` permission can do this.", musicManager.getVoiceChannel().get().getName());
            return;
        }

        LoadResultHandler handler = new LoadResultHandler(commandManager, musicManager, playerManager, context);
        handler.setAllowSearch(allowSearch);
        handler.setSearch(isSearch);
      
        Set<String> flags = context.parseFlags();
        if (flags.contains("first") || flags.contains("f")) {
            handler.setSetFirstInQueue(true);
        }

        context.setArgs(this.transformQuery(context.getArgs()));

        playerManager.loadItem(String.join(" ", context.getArgs()), handler);
        if (!musicManager.isOpen()) {
            musicManager.open(channel);
        }
    }

    protected abstract String noArgumentMessage();

    protected String[] transformQuery(String[] args) {
        return args;
    }
}
