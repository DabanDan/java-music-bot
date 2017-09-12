package ovh.not.javamusicbot.command;

import ovh.not.javamusicbot.Command;
import ovh.not.javamusicbot.MusicBot;
import ovh.not.javamusicbot.Utils;

import java.util.TreeMap;
import java.util.stream.Collectors;

public class HelpCommand extends Command {
    public HelpCommand() {
        super("help", "commands", "h", "music");
    }

    @Override
    public void on(Context context) {
        TreeMap<String, String> descriptions = MusicBot.getConfigs().constants.commandDescriptions;

        String message = "**Commands:**\n" + descriptions.entrySet().stream()
                        .map(e -> String.format("`%s` %s", Utils.capitalise(e.getKey()), e.getValue()))
                        .collect(Collectors.joining("\n")) +
                "\n\n**Quick start:** Use `{{prefix}}play <link>` to start playing a song, use the same command to " +
                "add another song, `{{prefix}}skip` to go to the next song and `{{prefix}}stop` to stop playing and " +
                "leave.";

        context.reply(message);
    }
}
