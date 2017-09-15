package ovh.not.javamusicbot.command;

import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import ovh.not.javamusicbot.CommandContext;
import ovh.not.javamusicbot.GuildManager;
import ovh.not.javamusicbot.MusicManager;
import ovh.not.javamusicbot.command.base.AbstractTextResponseCommand;
import ovh.not.javamusicbot.command.base.PipelineHandlers;

import java.util.List;

@SuppressWarnings("unchecked")
public class ReorderCommand extends AbstractTextResponseCommand {
    public static final String INVALID_ARGUMENTS_MESSAGE = "Usage: `{{prefix}}reorder <song number> <position>`\n" +
            "Example: `{{prefix}}reorder 5 1` - moves song at position 5 in queue to position 1";

    public ReorderCommand() {
        super("reorder", "order");

        super.getPipeline()
                .before(PipelineHandlers.argumentCheckHandler(INVALID_ARGUMENTS_MESSAGE, 2))
                .before(PipelineHandlers.requiresMusicHandler());
    }

    @Override
    public String textResponse(CommandContext context) {
        int songNum, newPosition;
        try {
            songNum = Integer.parseInt(context.getArgs().get(0));
            newPosition = Integer.parseInt(context.getArgs().get(1));
        } catch (NumberFormatException e) {
            return "Invalid song number or position!";
        }

        MusicManager musicManager = GuildManager.getInstance().getMusicManager(context.getEvent().getGuild());
        List<AudioTrack> queue = (List<AudioTrack>) musicManager.getTrackScheduler().getQueue();

        int index = songNum - 1;
        AudioTrack track = queue.get(index);
        if (track == null) {
            return "Could not find the specified song! Use {{prefix}}queue to find the position";
        }

        queue.remove(index);
        queue.add(newPosition - 1, track);

        return String.format("Moved **%s** by **%s** from position **%d** to position **%d**!",
                track.getInfo().title, track.getInfo().author, songNum, newPosition);
    }
}
