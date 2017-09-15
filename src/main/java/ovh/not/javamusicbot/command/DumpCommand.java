package ovh.not.javamusicbot.command;

import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import org.json.JSONArray;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ovh.not.javamusicbot.CommandContext;
import ovh.not.javamusicbot.GuildManager;
import ovh.not.javamusicbot.MusicManager;
import ovh.not.javamusicbot.Utils;
import ovh.not.javamusicbot.command.base.AbstractPipelineCommand;
import ovh.not.javamusicbot.command.base.PipelineHandlers;

import java.io.IOException;

import static ovh.not.javamusicbot.Utils.encode;

public class DumpCommand extends AbstractPipelineCommand {
    private static final Logger logger = LoggerFactory.getLogger(DumpCommand.class);

    public DumpCommand() {
        super("dump");

        getPipeline()
                .before(PipelineHandlers.requiresMusicHandler())
                .after(PipelineHandlers.textUploadTransformer((context, url) -> {
                    if (!url.isPresent()) {
                        context.reply("An error occurred!");
                        return;
                    }

                    context.reply("Dump created! %s", url.get());
                }));
    }

    @Override
    public Object run(CommandContext context) {
        MusicManager musicManager = GuildManager.getInstance().getMusicManager(context.getEvent().getGuild());
        String[] items = new String[musicManager.getTrackScheduler().getQueue().size() + 1];

        AudioTrack current = musicManager.getPlayer().getPlayingTrack();
        try {
            items[0] = Utils.encode(current);
        } catch (IOException e) {
            logger.error("error occurred encoding an AudioTrack", e);
            context.reply("An error occurred!");
            return null;
        }

        int i = 1;
        for (AudioTrack track : musicManager.getTrackScheduler().getQueue()) {
            try {
                items[i] = encode(track);
            } catch (IOException e) {
                logger.error("error occurred encoding audio tracks", e);
                context.reply("An error occurred!");
                return null;
            }
            i++;
        }

        return new JSONArray(items).toString();
    }
}
