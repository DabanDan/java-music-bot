package ovh.not.javamusicbot.command;

import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import ovh.not.javamusicbot.Command;
import ovh.not.javamusicbot.CommandContext;
import ovh.not.javamusicbot.GuildManager;
import ovh.not.javamusicbot.MusicManager;

import java.util.List;

@SuppressWarnings("unchecked")
public class ReorderCommand extends Command {
    public ReorderCommand() {
        super("reorder", "order");
    }

    @Override
    public void on(CommandContext context) {
        MusicManager musicManager = GuildManager.getInstance().getMusicManager(context.getEvent().getGuild());
        if (!musicManager.isPlayingMusic()) {
            context.reply("No music is playing on this guild! To play a song use `{{prefix}}play`");
            return;
        }

        if (context.getArgs().size() < 2) {
            context.reply("Usage: `{{prefix}}reorder <song number> <position>`\nExample: `{{prefix}}reorder 5 1` - moves song at "
                    + "position 5 in queue to position 1");
            return;
        }

        int songNum, newPosition;
        try {
            songNum = Integer.parseInt(context.getArgs().get(0));
            newPosition = Integer.parseInt(context.getArgs().get(1));
        } catch (NumberFormatException e) {
            context.reply("Invalid song number or position!");
            return;
        }

        List<AudioTrack> queue = (List<AudioTrack>) musicManager.getTrackScheduler().getQueue();

        int index = songNum - 1;
        AudioTrack track = queue.get(index);
        if (track == null) {
            context.reply("Could not find the specified song! Use {{prefix}}queue to find the position");
            return;
        }

        queue.remove(index);
        queue.add(newPosition - 1, track);

        context.reply("Moved **%s** by **%s** from position **%d** to position **%d**!",
                track.getInfo().title, track.getInfo().author, songNum, newPosition);
    }
}
