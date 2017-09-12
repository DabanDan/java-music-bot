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

import java.io.IOException;

@SuppressWarnings("ConstantConditions")
public class LoadCommand extends Command {
    private static final Logger logger = LoggerFactory.getLogger(LoadCommand.class);

    public LoadCommand() {
        super("load", "undump");
    }

    @Override
    public void on(Context context) {
        VoiceChannel channel = context.getEvent().getMember().getVoiceState().getChannel();
        if (channel == null) {
            context.reply("You must be in a voice channel!");
            return;
        }

        if (context.getArgs().length == 0) {
            context.reply("Usage: `{{prefix}}load <dumped playlist url>`");
            return;
        }

        MusicManager musicManager = GuildManager.getInstance().getMusicManager(context.getEvent().getGuild());

        if (Utils.warnIfBotInUse(musicManager, context)) return;

        String url = context.getArgs()[0];
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
            context.reply("An error occurred! %s", e.getMessage());
            return;
        }

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
                context.reply("An error occurred! %s", e.getMessage());
                return;
            }
        }

        context.reply("Loaded %d tracks from <%s>!", tracks.length(), url);

        if (!musicManager.isOpen()) {
            musicManager.open(channel);
        }
    }
}
