package ovh.not.javamusicbot.command;

import ovh.not.javamusicbot.command.base.AbstractCachedTextResponseCommand;
import ovh.not.javamusicbot.CommandContext;
import ovh.not.javamusicbot.MusicBot;

import java.util.TreeMap;
import java.util.stream.Collectors;

public class HelpCommand extends AbstractCachedTextResponseCommand {
    public HelpCommand() {
        super("help", "commands", "h", "music");
    }

    @Override
    protected String cachedTextResponse(CommandContext context) {
        TreeMap<String, String> descriptions = MusicBot.getConfigs().constants.commandDescriptions;

        return "**Commands:**\n" + descriptions.entrySet().stream()
                .map(e -> String.format("`%s` %s", e.getKey(), e.getValue()))
                .collect(Collectors.joining("\n")) +
                "\n\n**Quick start:** Use `{{prefix}}play <link>` to start playing a song, use the same command to " +
                "add another song, `{{prefix}}skip` to go to the next song and `{{prefix}}stop` to stop playing and " +
                "leave.";
    }
}
