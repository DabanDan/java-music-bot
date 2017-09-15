package ovh.not.javamusicbot;

import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;
import net.dv8tion.jda.core.exceptions.PermissionException;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static ovh.not.javamusicbot.Utils.getPrivateChannel;

public class CommandContext {
    private static final Pattern FLAG_PATTERN = Pattern.compile("\\s+-([a-zA-Z]+)");

    private final MessageReceivedEvent event;
    private List<String> args;

    CommandContext(MessageReceivedEvent event, List<String> args) {
        this.event = event;
        this.args = args;
    }

    public MessageReceivedEvent getEvent() {
        return event;
    }

    public List<String> getArgs() {
        return args;
    }

    public Message reply(String message) {
        try {
            return event.getChannel().sendMessage(message.replace("{{prefix}}", MusicBot.getConfigs().config.prefix))
                    .complete();
        } catch (PermissionException e) {
            getPrivateChannel(event.getAuthor()).sendMessage("**dabBot does not have permission to talk in the #"
                    + event.getTextChannel().getName() + " text channel.**\nTo fix this, allow dabBot to " +
                    "`Read Messages` and `Send Messages` in that text channel.\nIf you are not the guild " +
                    "owner, please send this to them.").complete();
            return null;
        }
    }

    public Message reply(String format, Object... args) {
        return reply(String.format(format, args));
    }

    public Set<String> parseFlags() {
        String content = String.join(" ", args);
        Matcher matcher = FLAG_PATTERN.matcher(content);
        Set<String> matches = new HashSet<>();
        while (matcher.find()) {
            matches.add(matcher.group().replaceFirst("\\s+-", ""));
        }
        content = content.replaceAll("\\s+-([a-zA-Z]+)", "");
        args = new ArrayList<>(Arrays.asList(content.split("\\s+")));
        return matches;
    }
}