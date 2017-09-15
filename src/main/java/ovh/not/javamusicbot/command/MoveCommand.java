package ovh.not.javamusicbot.command;

import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.VoiceChannel;
import ovh.not.javamusicbot.CommandContext;
import ovh.not.javamusicbot.GuildManager;
import ovh.not.javamusicbot.MusicManager;
import ovh.not.javamusicbot.Utils;
import ovh.not.javamusicbot.command.base.AbstractTextResponseCommand;
import ovh.not.javamusicbot.command.base.PipelineHandlers;

import java.util.Optional;

public class MoveCommand extends AbstractTextResponseCommand {
    private static final String INVALID_ARGUMENTS_MESSAGE = "Usage: {{prefix}}move <voice channel name>";

    public MoveCommand() {
        super("move");

        // require correct args, is playing music, bot not in use in another channel
        super.getPipeline()
                .before(PipelineHandlers.argumentCheckHandler(INVALID_ARGUMENTS_MESSAGE, 1))
                .before(PipelineHandlers.requiresMusicHandler())
                .before(PipelineHandlers.requiresNotInUseHandler());
    }

    @Override
    public String textResponse(CommandContext context) {
        Guild guild = context.getEvent().getGuild();
        MusicManager musicManager = GuildManager.getInstance().getMusicManager(guild);

        Optional<VoiceChannel> channel = Utils.getVoiceChannel(guild, String.join(" ", context.getArgs()));
        if (!channel.isPresent()) {
            return "Could not find the specified voice channel! Are you sure I have permission to connect to it?";
        }

        // pause music and close the voice connection
        musicManager.getPlayer().setPaused(true);
        musicManager.close();

        // open the new voice connection and resume music
        musicManager.open(channel.get());
        musicManager.getPlayer().setPaused(false);

        return "Moved voice channel!";
    }
}
