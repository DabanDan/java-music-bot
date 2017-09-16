package ovh.not.javamusicbot.command;

import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import net.dv8tion.jda.core.entities.Member;
import ovh.not.javamusicbot.Command;
import ovh.not.javamusicbot.CommandContext;
import ovh.not.javamusicbot.GuildManager;
import ovh.not.javamusicbot.Selection;

import java.util.Optional;

public class ChooseCommand extends Command {
    public ChooseCommand() {
        super("choose", "pick", "select", "cancel", "c", "choos", "chose");
    }

    @Override
    public void on(CommandContext context) {
        Member member = context.getEvent().getMember();
        GuildManager guildManager = GuildManager.getInstance();
        Optional<Selection<AudioTrack>> optionalTrackSelector = guildManager.getTrackSelector(member);

        if (!optionalTrackSelector.isPresent()) {
            context.reply("There's no selection active in this guild - are you sure you ran `{{prefix}}play`?\n\n" +
                    "To play a song...\n" +
                    "* Join a voice channel\n" +
                    "* Use `{{prefix}}play <song name/link>`\n" +
                    "* Choose one of the song options with {`{prefix}}choose <song number>`");
            return;
        }

        Selection<AudioTrack> trackSelector = optionalTrackSelector.get();

        if (context.getArgs().isEmpty()) {
            guildManager.removeTrackSelector(member);
            trackSelector.getCallback().accept(false, null);
            return;
        }

        switch (context.getArgs().get(0).toLowerCase()) {
            case "c":
            case "cancel":
                guildManager.removeTrackSelector(member);
                trackSelector.getCallback().accept(false, null);
                return;
        }

        for (String arg : context.getArgs()) {
            int selected;
            try {
                selected = Integer.parseInt(arg);
            } catch (NumberFormatException e) {
                context.reply("Invalid input `%s`. Must be an integer with the range 1 - %d. **To cancel selection**, "
                        + "use `{{prefix}}cancel`.", arg, trackSelector.getItems().length);
                return;
            }

            int length = trackSelector.getItems().length;

            if (selected < 1 || selected > length) {
                context.reply("Invalid input `%s`. Must be an integer with the range 1 - %d. **To cancel selection**, "
                        + "use `{{prefix}}cancel`.", arg, length);
                return;
            }

            AudioTrack track = trackSelector.getItems()[selected - 1];
            trackSelector.getCallback().accept(true, track);
        }

        guildManager.removeTrackSelector(member);
    }
}
