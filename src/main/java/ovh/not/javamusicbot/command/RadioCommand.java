package ovh.not.javamusicbot.command;

import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager;
import net.dv8tion.jda.core.entities.Member;
import net.dv8tion.jda.core.entities.VoiceChannel;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;
import ovh.not.javamusicbot.*;

import java.util.Iterator;
import java.util.Map;

public class RadioCommand extends Command {
    private final CommandManager commandManager;
    private final AudioPlayerManager playerManager;

    private static String usageMessage = null;

    public RadioCommand(CommandManager commandManager, AudioPlayerManager playerManager) {
        super("radio", "station", "stations", "fm", "r");
        this.commandManager = commandManager;
        this.playerManager = playerManager;
        reloadUsageMessage();
    }

    public static void reloadUsageMessage() {
        StringBuilder builder = new StringBuilder("Streams a variety of radio stations.\n" +
                "Usage: `{{prefix}}radio <station>`\n" +
                "\n**Available stations:**\n");
        Iterator<String> iterator = MusicBot.getConfigs().constants.radioStations.keySet().iterator();
        while (iterator.hasNext()) {
            String station = iterator.next();
            builder.append(station.substring(1, station.length() - 1));
            if (iterator.hasNext()) {
                builder.append(", ");
            }
        }
        builder.append("\n\nNeed another station? Join the support server with the link in `{{prefix}}support`.");
        usageMessage = builder.toString();
    }

    @Override
    public void on(CommandContext context) {
        // todo sort out this mess
        if (context.getArgs().isEmpty()) {
            if (usageMessage.length() < 2000) {
                context.reply(usageMessage);
            }
            String message = usageMessage;
            while (message.length() > 1950) {
                StringBuilder builder = new StringBuilder();
                int i = 0;
                for (char c : message.toCharArray()) {
                    builder.append(c);
                    i++;
                    if (i > 1950 && c == ',') {
                        i++;
                        break;
                    }
                }
                message = message.substring(i);
                context.reply(builder.toString());
            }
            context.reply(message);
            return;
        }
        String station = "\"" + String.join(" ", context.getArgs()) + "\"";
        String url = null;
        for (Map.Entry<String, String> entry : MusicBot.getConfigs().constants.radioStations.entrySet()) {
            if (entry.getKey().equalsIgnoreCase(station)) {
                url = entry.getValue();
                break;
            }
        }
        if (url == null) {
            context.reply("Invalid station! For usage & stations, use `{{prefix}}radio`");
            return;
        }

        MessageReceivedEvent event = context.getEvent();
        Member member = event.getMember();

        if (!Utils.isInVoiceChannel(member)) {
            context.reply("You must be in a voice channel!");
            return;
        }

        VoiceChannel channel = member.getVoiceState().getChannel();
        MusicManager musicManager = GuildManager.getInstance().getMusicManager(event.getGuild());

        if (Utils.warnIfBotInUse(musicManager, context)) return;

        LoadResultHandler handler = new LoadResultHandler(commandManager, musicManager, playerManager, context);

        TrackScheduler scheduler = musicManager.getTrackScheduler();
        scheduler.getQueue().clear();
        scheduler.setRepeat(false);
        scheduler.setLoop(false);

        musicManager.getPlayer().stopTrack();

        playerManager.loadItem(url, handler);
        if (!musicManager.isOpen()) {
            musicManager.open(channel);
        }
    }
}
