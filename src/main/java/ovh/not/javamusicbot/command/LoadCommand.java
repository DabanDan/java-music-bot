package ovh.not.javamusicbot.command;

import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import net.dv8tion.jda.core.entities.VoiceChannel;
import okhttp3.Request;
import okhttp3.Response;
import org.json.JSONArray;
import org.json.JSONException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ovh.not.javamusicbot.*;
import ovh.not.javamusicbot.command.base.AbstractTextResponseCommand;
import ovh.not.javamusicbot.command.base.PipelineHandlers;

import java.io.IOException;

@SuppressWarnings("ConstantConditions")
public class LoadCommand extends AbstractTextResponseCommand {
    private static final Logger logger = LoggerFactory.getLogger(LoadCommand.class);

    public LoadCommand() {
        super("load", "undump");

        getPipeline()
                .before(PipelineHandlers.argumentCheckHandler("Usage: {{prefix}}load <paste.dabbot.org dump url>", 1))
                .before(PipelineHandlers.requiresUserInVoiceChannelHandler())
                .before(PipelineHandlers.requiresNotInUseHandler());
    }

    @Override
    public String textResponse(CommandContext context) {
        String url = context.getArgs().get(0);
        if (url.contains("hastebin.com") && !url.contains("raw")) {
            String name = url.substring(url.lastIndexOf("/") + 1);
            url = "https://hastebin.com/raw/" + name;
        }

        JSONArray tracks;
        try {
            Request request = new Request.Builder().url(url).build();
            Response response = MusicBot.HTTP_CLIENT.newCall(request).execute();
            tracks = new JSONArray(response.body().string());
        } catch (IOException | JSONException e) {
            logger.error("error occurred loading tracks from a dump", e);
            return String.format("An error occurred! %s", e.getMessage());
        }

        MusicManager musicManager = GuildManager.getInstance().getMusicManager(context.getEvent().getGuild());
        TrackScheduler scheduler = musicManager.getTrackScheduler();

        scheduler.getQueue().clear();
        scheduler.setRepeat(false);
        scheduler.setLoop(false);

        musicManager.getPlayer().stopTrack();

        for (int i = 0; i < tracks.length(); i++) {
            String encoded = tracks.getString(i);

            try {
                AudioTrack track = Utils.decode(encoded);
                scheduler.queue(musicManager.getPlayer(), track);
            } catch (IOException e) {
                logger.error("error occurred decoding encoded tracks", e);
                return String.format("An error occurred! %s", e.getMessage());
            }
        }

        if (!musicManager.isOpen()) {
            VoiceChannel channel = context.getEvent().getMember().getVoiceState().getChannel();
            musicManager.open(channel);
        }

        return String.format("Loaded %d tracks from <%s>!", tracks.length(), url);
    }
}
