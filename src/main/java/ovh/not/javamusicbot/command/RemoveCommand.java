package ovh.not.javamusicbot.command;

import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import ovh.not.javamusicbot.CommandContext;
import ovh.not.javamusicbot.GuildManager;
import ovh.not.javamusicbot.MusicManager;
import ovh.not.javamusicbot.command.base.AbstractTextResponseCommand;
import ovh.not.javamusicbot.command.base.PipelineHandlers;

import java.util.List;

public class RemoveCommand extends AbstractTextResponseCommand {
    private static final String INVALID_ARGUMENTS_MESSAGE = "Usage: `{{prefix}}remove <song position>`\nExample: " +
            "`{{prefix}}remove 5` - moves song at position 5 in queue";

    public RemoveCommand() {
        super("remove", "delete", "rm");

        // ensure args & music is playing
        super.getPipeline()
                .before(PipelineHandlers.argumentCheckHandler(INVALID_ARGUMENTS_MESSAGE, 1))
                .before(PipelineHandlers.requiresMusicHandler());
    }

    @SuppressWarnings("unchecked")
    @Override
    public String textResponse(CommandContext context) {
        MusicManager musicManager = GuildManager.getInstance().getMusicManager(context.getEvent().getGuild());

        int position;
        try {
            position = Integer.parseInt(context.getArgs().get(0));
        } catch (NumberFormatException e) {
            return "Invalid song position!";
        }

        List<AudioTrack> queue = (List<AudioTrack>) musicManager.getTrackScheduler().getQueue();
        if (position > queue.size()) {
            return String.format("Invalid song position! Maximum: %d", queue.size());
        }

        int index = position - 1;

        AudioTrack track = queue.get(index);
        if (track == null) {
            return "Invalid song!";
        }

        queue.remove(index);

        return String.format("Removed **%s** by **%s** at position **%d** from the queue!",
                track.getInfo().title, track.getInfo().author, position);
    }
}
