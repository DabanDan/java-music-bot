package ovh.not.javamusicbot.command;

import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import net.dv8tion.jda.bot.sharding.ShardManager;
import net.dv8tion.jda.core.JDA;
import net.dv8tion.jda.core.entities.VoiceChannel;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ovh.not.javamusicbot.*;
import ovh.not.javamusicbot.command.base.AbstractPipelineCommand;
import ovh.not.javamusicbot.command.base.AbstractTextResponseCommand;
import ovh.not.javamusicbot.command.base.PipelineHandlers;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

public class AdminCommand extends AbstractPipelineCommand {
    private static final Logger logger = LoggerFactory.getLogger(AdminCommand.class);

    private final Map<String, Command> subCommands = new HashMap<>();

    public AdminCommand() {
        super("admin", "a");

        CommandManager.register(subCommands,
                new EvalCommand(),
                new ShutdownCommand(),
                new ShardRestartCommand(),
                new EncodeCommand(),
                new DecodeCommand(),
                new ReloadCommand()
        );

        // only continue if the user is an owner
        super.getPipeline().before(context ->
                Utils.stringArrayContains(MusicBot.getConfigs().config.owners, context.getEvent().getAuthor().getId()));

        // return the a list of sub commands if the args are empty
        super.getPipeline().before(PipelineHandlers.argumentCheckHandler(String
                .format("Admin commands: %s", subCommands.values().stream()
                        .map(command -> command.getNames()[0])
                        .collect(Collectors.joining(", "))), 1));
    }

    @Override
    public Object run(CommandContext context) {
        String name = context.getArgs().get(0);
        if (!subCommands.containsKey(name)) {
            context.reply("Invalid subcommand!");
            return null;
        }

        Command command = subCommands.get(name);
        if (context.getArgs().size() > 0) {
            context.getArgs().remove(0);
        }

        command.on(context);
        return null;
    }

    private class ShutdownCommand extends Command {
        private ShutdownCommand() {
            super("shutdown");
        }

        @Override
        public void on(CommandContext context) {
            context.reply("Shutting down!");
            MusicBot.running = false; // break the running loop
            context.getEvent().getJDA().asBot().getShardManager().shutdown(); // shutdown jda
        }
    }

    private class EvalCommand extends AbstractTextResponseCommand {
        private static final String INVALID_ARGUMENTS_MESSAGE = "Usage: `{{prefix}}eval <code>`\n\n" +
                "Available variables:```\n" +
                "event -> net.dv8tion.jda.core.events.message.MessageReceivedEvent\n" +
                "jda   -> net.dv8tion.jda.core.JDA\n" +
                "args  -> String[]\n" +
                "```";

        private final ScriptEngineManager engineManager = new ScriptEngineManager();

        private EvalCommand() {
            super("eval", "js");

            // require some code ;p
            this.getPipeline().before(PipelineHandlers.argumentCheckHandler(INVALID_ARGUMENTS_MESSAGE, 1));
        }

        @Override
        public String textResponse(CommandContext context) {
            ScriptEngine engine = engineManager.getEngineByName("nashorn");
            engine.put("event", context.getEvent());
            engine.put("jda", context.getEvent().getJDA());
            engine.put("args", context.getArgs());

            try {
                Object result = engine.eval(String.join(" ", context.getArgs()));

                if (result != null) {
                    return result.toString();
                } else {
                    return null;
                }
            } catch (ScriptException e) {
                logger.error("error performing eval command", e);

                // todo add error to a paste instead of sentry as it isnt a code error

                return String.format("An error occurred: %s", e.getMessage());
            }
        }
    }

    private class ShardRestartCommand extends AbstractTextResponseCommand {
        private ShardRestartCommand() {
            super("shardrestart", "sr");
        }

        @Override
        public String textResponse(CommandContext context) {
            MessageReceivedEvent event = context.getEvent();
            JDA jda = event.getJDA();
            ShardManager manager = jda.asBot().getShardManager();

            try {
                int shardId;

                if (context.getArgs().isEmpty()) {
                    shardId = jda.getShardInfo().getShardId();
                } else {
                    try {
                        shardId = Integer.parseInt(context.getArgs().get(0));
                        if (manager.getShard(shardId) == null) {
                            return String.format("Invalid shard %d.", shardId);
                        }
                    } catch (NumberFormatException e) {
                        return String.format("Invalid input `%s`. Must be an integer.", context.getArgs().get(0));
                    }
                }

                context.reply("Restarting shard %d...", shardId);
                manager.restart(shardId);

                return String.format("Restarted shard %d!", shardId);
            } catch (Exception e) {
                logger.error("error performing shardrestart command", e);
                return String.format("Error running the shardrestart command: %s", e.getMessage());
            }
        }
    }

    private class EncodeCommand extends AbstractTextResponseCommand {
        private EncodeCommand() {
            super("encode");

            // ensure there is a track playing before trying to encode it or NPE!! :)
            this.getPipeline().before(PipelineHandlers.requiresMusicHandler());
        }

        @Override
        public String textResponse(CommandContext context) {
            MusicManager musicManager = GuildManager.getInstance().getMusicManager(context.getEvent().getGuild());

            try {
                return Utils.encode(musicManager.getPlayer().getPlayingTrack());
            } catch (IOException e) {
                logger.error("error performing encode command", e);
                return String.format("An error occurred: %s", e.getMessage());
            }
        }
    }

    private class DecodeCommand extends AbstractPipelineCommand {
        private static final String INVALID_ARGUMENTS_MESSAGE = "Usage: {{prefix}}a decode <base64 string>";

        private DecodeCommand() {
            super("decode");

            // require at least 1 argument and that the user is in a voice channel
            super.getPipeline()
                    .before(PipelineHandlers.argumentCheckHandler(INVALID_ARGUMENTS_MESSAGE, 1))
                    .before(PipelineHandlers.requiresUserInVoiceChannelHandler());
        }

        @Override
        public Object run(CommandContext context) {
            String base64 = context.getArgs().get(0);

            AudioTrack track;
            try {
                track = Utils.decode(base64);
            } catch (IOException e) {
                logger.error("error performing decode command", e);
                context.reply("An error occurred!");
                return null;
            }

            MusicManager musicManager = GuildManager.getInstance().getMusicManager(context.getEvent().getGuild());
            VoiceChannel channel = context.getEvent().getMember().getVoiceState().getChannel();

            if (!musicManager.isOpen()) {
                musicManager.open(channel);
            }

            musicManager.getPlayer().playTrack(track);
            return null;
        }
    }

    private class ReloadCommand extends AbstractTextResponseCommand {
        private ReloadCommand() {
            super("reload");
        }

        @Override
        public String textResponse(CommandContext context) {
            try {
                MusicBot.reloadConfigs();
                RadioCommand.reloadUsageMessage();

                return "Configs reloaded!";
            } catch (Exception e) {
                logger.error("error performing reload command", e);
                return "Could not reload configs: " + e.getMessage();
            }
        }
    }
}
