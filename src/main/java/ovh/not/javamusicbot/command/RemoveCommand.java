package ovh.not.javamusicbot.command;

import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import ovh.not.javamusicbot.AbstractCommand;
import ovh.not.javamusicbot.CommandContext;
import ovh.not.javamusicbot.GuildManager;
import ovh.not.javamusicbot.MusicManager;

import java.util.List;

public class RemoveCommand extends AbstractCommand {
    public RemoveCommand() {
        super("remove", "delete", "rm");
    }

    @SuppressWarnings("unchecked")
    @Override
    public void on(CommandContext context) {
        if (context.getArgs().isEmpty()) {
            context.reply("Usage: `{{prefix}}remove <song position>`\nExample: `{{prefix}}remove 5` - moves song at "
                    + "position 5 in queue");
            return;
        }

        MusicManager musicManager = GuildManager.getInstance().getMusicManager(context.getEvent().getGuild());
        if (!musicManager.isPlayingMusic()) {
            context.reply("No music is playing on this guild! To play a song use `{{prefix}}play`");
            return;
        }

        int position;
        try {
            position = Integer.parseInt(context.getArgs().get(0));
        } catch (NumberFormatException e) {
            context.reply("Invalid song position!");
            return;
        }
        List<AudioTrack> queue = (List<AudioTrack>) musicManager.getTrackScheduler().getQueue();
        if (position > queue.size()) {
            context.reply("Invalid song position! Maximum: %d", queue.size());
            return;
        }
        int index = position - 1;
        AudioTrack track = queue.get(index);
        if (track == null) {
            context.reply("Invalid song!");
            return;
        }
        queue.remove(index);
        context.reply("Removed **%s** by **%s** at position **%d** from the queue!",
                track.getInfo().title, track.getInfo().author, position);
    }
}
