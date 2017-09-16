package ovh.not.javamusicbot.command.base;

import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager;
import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.VoiceChannel;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;
import ovh.not.javamusicbot.*;

import java.util.Set;

public abstract class AbstractPlayCommand extends AbstractNoResponsePipelineCommand {
    private final CommandManager commandManager;
    private final AudioPlayerManager playerManager;
    protected boolean allowSearch = true;
    protected boolean isSearch = false;

    protected AbstractPlayCommand(CommandManager commandManager, AudioPlayerManager playerManager, String name, String... names) {
        super(name, names);
        this.commandManager = commandManager;
        this.playerManager = playerManager;

        getPipeline().before(PipelineHandlers
                .requiresUserInVoiceChannelHandler())
                .before(PipelineHandlers.requiresNotInUseHandler());
    }

    @Override
    public void noResponse(CommandContext context) {
        MessageReceivedEvent event = context.getEvent();
        Guild guild = event.getGuild();

        MusicManager musicManager = GuildManager.getInstance().getMusicManager(guild);
        musicManager.setTextChannelIfNotPresent(event.getTextChannel());

        LoadResultHandler handler = new LoadResultHandler(musicManager, playerManager, context);
        handler.setAllowSearch(allowSearch);
        handler.setSearch(isSearch);
      
        Set<String> flags = context.parseFlags();
        if (flags.contains("first") || flags.contains("f")) {
            handler.setSetFirstInQueue(true);
        }

        playerManager.loadItem(String.join(" ", context.getArgs()), handler);

        if (!musicManager.isOpen()) {
            VoiceChannel channel = context.getEvent().getMember().getVoiceState().getChannel();
            musicManager.open(channel);
        }
    }
}
