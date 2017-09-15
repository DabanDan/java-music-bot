package ovh.not.javamusicbot.command;

import ovh.not.javamusicbot.CommandContext;
import ovh.not.javamusicbot.GuildManager;
import ovh.not.javamusicbot.MusicManager;
import ovh.not.javamusicbot.command.base.AbstractTextResponseCommand;
import ovh.not.javamusicbot.command.base.PipelineHandlers;

import java.time.Duration;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class JumpCommand extends AbstractTextResponseCommand {
    private static final Pattern TIME_PATTERN = Pattern
            .compile("(?:(?<hours>\\d{1,2}):)?(?:(?<minutes>\\d{1,2}):)?(?<seconds>\\d{1,2})");

    private static final String INVALID_ARGUMENTS_MESSAGE = "Usage: `{{prefix}}jump <time>`\nExample: " +
            "`{{prefix}}jump 03:51` - starts playing the current song at 3 min 51s instead of at the start.\nTime " +
            "format: `hh:mm:ss`, e.g. 01:25:51 = 1 hour, 25 minutes & 51 seconds";

    public JumpCommand() {
        super("jump", "seek");

        // args :)
        this.getPipeline().before(PipelineHandlers.argumentCheckHandler(INVALID_ARGUMENTS_MESSAGE, 1));

        // require music to be playing
        this.getPipeline().before(PipelineHandlers.requiresMusicHandler());
    }

    @Override
    public String textResponse(CommandContext context) {
        Matcher matcher = TIME_PATTERN.matcher(context.getArgs().get(0));
        if (!matcher.find()) {
            return INVALID_ARGUMENTS_MESSAGE;
        }

        String sHours = matcher.group("hours");
        String sMinutes = matcher.group("minutes");
        if (sMinutes == null && sHours != null) {
            sMinutes = sHours;
            sHours = null;
        }
        String sSeconds = matcher.group("seconds");

        long hours = 0, minutes = 0, seconds = 0;
        try {
            if (sHours != null) {
                hours = Long.parseLong(sHours);
            }
            if (sMinutes != null) {
                minutes = Long.parseLong(sMinutes);
            }
            if (sSeconds != null) {
                seconds = Long.parseLong(sSeconds);
            }
        } catch (NumberFormatException e) {
            return INVALID_ARGUMENTS_MESSAGE;
        }

        long time = Duration.ofHours(hours).toMillis();
        time += Duration.ofMinutes(minutes).toMillis();
        time += Duration.ofSeconds(seconds).toMillis();

        MusicManager musicManager = GuildManager.getInstance().getMusicManager(context.getEvent().getGuild());
        musicManager.getPlayer().getPlayingTrack().setPosition(time);

        return "Jumped to the specified position. Use `{{prefix}}nowplaying` to see the current song & position.";
    }
}
