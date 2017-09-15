package ovh.not.javamusicbot.command;

import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.VoiceChannel;
import net.dv8tion.jda.core.utils.IOUtil;
import org.json.JSONArray;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ovh.not.javamusicbot.*;
import ovh.not.javamusicbot.command.base.AbstractNoResponsePipelineCommand;
import ovh.not.javamusicbot.command.base.PipelineHandlers;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

@SuppressWarnings("ConstantConditions")
public class DiscordFMCommand extends AbstractNoResponsePipelineCommand {
    private static final Logger logger = LoggerFactory.getLogger(DiscordFMCommand.class);

    private static final String DFM_DIRECTORY_PATH = "discordfm/";

    private final CommandManager commandManager;
    private final AudioPlayerManager playerManager;
    private Collection<Library> libraries = null;
    private String usageResponse = null;

    public DiscordFMCommand(CommandManager commandManager, AudioPlayerManager playerManager) {
        super("discordfm", "dfm");
        this.commandManager = commandManager;
        this.playerManager = playerManager;

        getPipeline()
                .before(PipelineHandlers.requiresUserInVoiceChannelHandler())
                .before(PipelineHandlers.requiresNotInUseHandler());
    }

    private void load() {
        libraries = Arrays.stream(new File(DFM_DIRECTORY_PATH).listFiles())
                .map(file -> new Library(file.getName(), file))
                .sorted(Comparator.comparing(o -> o.name))
                .collect(Collectors.toCollection(ArrayList::new));

        StringBuilder builder = new StringBuilder("Uses a song playlist from the now defunct Discord.FM\nUsage: `{{prefix}}dfm <library>`" +
                "\n\n**Available libraries:**\n");

        Iterator<Library> iterator = libraries.iterator();

        while (iterator.hasNext()) {
            Library library = iterator.next();
            builder.append(library.name);

            if (iterator.hasNext()) {
                builder.append(", ");
            }
        }

        usageResponse = builder.toString();
    }

    @Override
    public void noResponse(CommandContext context) {
        if (libraries == null || usageResponse == null) {
            Message msg = context.reply("Loading libraries..");
            load();
            msg.delete().queue();
        }

        if (context.getArgs().isEmpty()) {
            context.reply(usageResponse);
            return;
        }

        String libraryName = String.join(" ", context.getArgs());

        Optional<Library> library = libraries.stream()
                .filter(lib -> lib != null && lib.name.equalsIgnoreCase(libraryName))
                .findFirst();

        if (!library.isPresent()) {
            context.reply("Invalid library! Use `{{prefix}}dfm` to see usage & libraries.");
            return;
        }

        String[] songs;
        try {
            songs = library.get().getSongs();
        } catch (IOException e) {
            logger.error("error getting discord.fm queue", e);
            context.reply("An error occurred!");
            return;
        }

        if (songs == null) {
            context.reply("An error occurred!");
            return;
        }

        MusicManager musicManager = GuildManager.getInstance().getMusicManager(context.getEvent().getGuild());
        musicManager.setTextChannelIfNotPresent(context.getEvent().getTextChannel());

        musicManager.getTrackScheduler().getQueue().clear();
        musicManager.getTrackScheduler().setRepeat(false);
        musicManager.getTrackScheduler().setLoop(false);
        musicManager.getPlayer().stopTrack();

        LoadResultHandler handler = new LoadResultHandler(commandManager, musicManager, playerManager, context);
        handler.setVerbose(false);

        for (String song : songs) {
            playerManager.loadItem(song, handler);
        }

        if (!musicManager.isOpen()) {
            VoiceChannel channel = context.getEvent().getMember().getVoiceState().getChannel();
            musicManager.open(channel);
        }
    }

    private class Library {
        private final String name;
        private final File file;

        private String[] songs = null;

        private Library(String name, File file) {
            name = name.replace("_", " ");
            this.name = name.substring(0, name.length() - 5);
            this.file = file;
        }

        private String[] getSongs() throws IOException {
            if (songs != null) {
                return songs;
            }

            JSONArray array = new JSONArray(new String(IOUtil.readFully(file)));

            String[] songs = new String[array.length()];
            for (int i = 0; i < array.length(); i++) {
                songs[i] = array.getJSONObject(i).getString("identifier");
            }

            this.songs = songs;
            return songs;
        }
    }
}
