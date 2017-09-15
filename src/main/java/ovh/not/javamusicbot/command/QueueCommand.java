package ovh.not.javamusicbot.command;

import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ovh.not.javamusicbot.CommandContext;
import ovh.not.javamusicbot.GuildManager;
import ovh.not.javamusicbot.MusicManager;
import ovh.not.javamusicbot.Pageable;
import ovh.not.javamusicbot.command.base.AbstractPipelineCommand;
import ovh.not.javamusicbot.command.base.PipelineHandlers;

import java.util.List;
import java.util.Queue;

import static ovh.not.javamusicbot.Utils.*;

@SuppressWarnings("unchecked")
public class QueueCommand extends AbstractPipelineCommand {
    private static final Logger logger = LoggerFactory.getLogger(QueueCommand.class);

    private static final String BASE_LINE = "%s by %s `[%s]`";
    private static final String CURRENT_LINE = "__Currently playing:__\n" + BASE_LINE;
    private static final String QUEUE_LINE = "\n`%02d` " + BASE_LINE;
    private static final String SONG_QUEUE_LINE = "\n\n__Song queue:__ (Page **%d** of **%d**)";
    private static final int PAGE_SIZE = 10;

    public QueueCommand() {
        super("queue", "list", "q");

        getPipeline()
                .before(PipelineHandlers.requiresMusicHandler())
                .after(PipelineHandlers.textUploadTransformer((context, result) -> {
                    if (!result.isPresent()) {
                        context.reply("An error occurred!");
                        return;
                    }

                    context.reply("Full song queue: %s", result.get());
                }));
    }

    @Override
    public Object run(CommandContext context) {
        MusicManager musicManager = GuildManager.getInstance().getMusicManager(context.getEvent().getGuild());

        // todo clean up this big mess
        AudioTrack playing = musicManager.getPlayer().getPlayingTrack();
        Queue<AudioTrack> queue = musicManager.getTrackScheduler().getQueue();
        StringBuilder builder = new StringBuilder();
        if (!context.getArgs().isEmpty() && context.getArgs().get(0).equalsIgnoreCase("all")) {
            long durationTotal = playing.getDuration();
            List<AudioTrack> list = (List<AudioTrack>) queue;
            StringBuilder items = new StringBuilder();
            for (int i = 0; i < list.size(); i++) {
                AudioTrack track = list.get(i);
                durationTotal += track.getDuration();
                items.append(String.format("\n%02d %s by %s [%s/%s]", i + 1, track.getInfo().title,
                        track.getInfo().author, formatDuration(track.getPosition()),
                        formatTrackDuration(track)));
            }
            builder.append(String.format("Song queue for %s - %d songs (%s).\nCurrent song: %s by %s [%s/%s]\n",
                    context.getEvent().getGuild().getName(), queue.size(), formatLongDuration(durationTotal), playing.getInfo().title,
                    playing.getInfo().author, formatDuration(playing.getPosition()),
                    formatTrackDuration(playing)));
            builder.append(items.toString());
            return builder.toString();
        } else {
            builder.append(String.format(CURRENT_LINE, playing.getInfo().title, playing.getInfo().author,
                    formatDuration(playing.getPosition()) + "/" + formatTrackDuration(playing)));
            Pageable<AudioTrack> pageable = new Pageable<>((List<AudioTrack>) queue);
            pageable.setPageSize(PAGE_SIZE);
            if (!context.getArgs().isEmpty()) {
                int page;
                try {
                    page = Integer.parseInt(context.getArgs().get(0));
                } catch (NumberFormatException e) {
                    context.reply("Invalid page! Must be an integer within the range %d - %d",
                            pageable.getMinPageRange(), pageable.getMaxPages());
                    return null;
                }
                if (page < pageable.getMinPageRange() || page > pageable.getMaxPages()) {
                    context.reply("Invalid page! Must be an integer within the range %d - %d",
                            pageable.getMinPageRange(), pageable.getMaxPages());
                    return null;
                }
                pageable.setPage(page);
            } else {
                pageable.setPage(pageable.getMinPageRange());
            }
            builder.append(String.format(SONG_QUEUE_LINE, pageable.getPage(), pageable.getMaxPages()));
            int index = 1;
            for (AudioTrack track : pageable.getListForPage()) {
                builder.append(String.format(QUEUE_LINE, ((pageable.getPage() - 1) * pageable.getPageSize()) + index, track.getInfo().title, track.getInfo().author,
                        formatTrackDuration(track)));
                index++;
            }
            if (pageable.getPage() < pageable.getMaxPages()) {
                builder.append("\n\n__To see the next page:__ `{{prefix}}queue ").append(pageable.getPage() + 1)
                        .append("`\nTo see the full queue, use `{{prefix}}queue all`");
            }
            context.reply(builder.toString());
            return null;
        }
    }
}
