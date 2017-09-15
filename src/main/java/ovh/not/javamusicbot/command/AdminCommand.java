package ovh.not.javamusicbot.command;

import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import net.dv8tion.jda.bot.sharding.ShardManager;
import net.dv8tion.jda.core.JDA;
import net.dv8tion.jda.core.entities.VoiceChannel;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ovh.not.javamusicbot.*;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class AdminCommand extends AbstractCommand {
    private static final Logger logger = LoggerFactory.getLogger(AdminCommand.class);

    private final Map<String, AbstractCommand> subCommands = new HashMap<>();
    private final String subCommandsString;

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
        StringBuilder builder = new StringBuilder("Subcommands:");
        subCommands.values().forEach(command -> builder.append(" ").append(command.getNames()[0]));
        subCommandsString = builder.toString();
    }

    @Override
    public void on(CommandContext context) {
        if (!Utils.stringArrayContains(MusicBot.getConfigs().config.owners, context.getEvent().getAuthor().getId())) {
            return;
        }

        if (context.getArgs().isEmpty()) {
            context.reply(subCommandsString);
            return;
        }

        String name = context.getArgs().get(0);
        if (!subCommands.containsKey(name)) {
            context.reply("Invalid subcommand!");
            return;
        }

        AbstractCommand command = subCommands.get(name);
        if (context.getArgs().size() > 0) {
            context.getArgs().remove(0);
        }
        command.on(context);
    }

    private class ShutdownCommand extends AbstractCommand {
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

    private class EvalCommand extends AbstractCommand {
        private final ScriptEngineManager engineManager = new ScriptEngineManager();

        private EvalCommand() {
            super("eval", "js");
        }

        @Override
        public void on(CommandContext context) {
            ScriptEngine engine = engineManager.getEngineByName("nashorn");
            engine.put("event", context.getEvent());
            engine.put("args", context.getArgs());
            engine.put("jda", context.getEvent().getJDA());
            try {
                Object result = engine.eval(String.join(" ", context.getArgs()));
                if (result != null) context.reply(result.toString());
            } catch (ScriptException e) {
                logger.error("error performing eval command", e);
                context.reply(e.getMessage());
            }
        }
    }

    private class ShardRestartCommand extends AbstractCommand {
        private ShardRestartCommand() {
            super("shardrestart", "sr");
        }

        @Override
        public void on(CommandContext context) {
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
                            context.reply("Invalid shard %d.", shardId);
                            return;
                        }
                    } catch (NumberFormatException e) {
                        context.reply("Invalid input %s. Must be an integer.", context.getArgs().get(0));
                        return;
                    }
                }

                context.reply("Restarting shard %d...", shardId);
                manager.restart(shardId);
            } catch (Exception e) {
                logger.error("error performing shardrestart command", e);
            }
        }
    }

    private class EncodeCommand extends AbstractCommand {
        private EncodeCommand() {
            super("encode");
        }

        @Override
        public void on(CommandContext context) {
            MusicManager musicManager = GuildManager.getInstance().getMusicManager(context.getEvent().getGuild());
            if (!musicManager.isPlayingMusic()) {
                context.reply("a track must be playing to encode it");
                return;
            }

            try {
                context.reply(Utils.encode(musicManager.getPlayer().getPlayingTrack()));
            } catch (IOException e) {
                logger.error("error performing encode command", e);
                context.reply("An error occurred!");
            }
        }
    }

    private class DecodeCommand extends AbstractCommand {
        private DecodeCommand() {
            super("decode");
        }

        @Override
        public void on(CommandContext context) {
            if (context.getArgs().isEmpty()) {
                context.reply("Usage: {{prefix}}a decode <base64 string>");
                return;
            }
            String base64 = context.getArgs().get(0);

            VoiceChannel channel = context.getEvent().getMember().getVoiceState().getChannel();
            if (channel == null) {
                context.reply("Must be in a voice channel!");
                return;
            }

            AudioTrack track;
            try {
                track = Utils.decode(base64);
            } catch (IOException e) {
                logger.error("error performing decode command", e);
                context.reply("An error occurred!");
                return;
            }

            MusicManager musicManager = GuildManager.getInstance().getMusicManager(context.getEvent().getGuild());
            if (!musicManager.isOpen()) {
                musicManager.open(channel);
            }

            musicManager.getPlayer().playTrack(track);
        }
    }

    private class ReloadCommand extends AbstractCommand {
        private ReloadCommand() {
            super("reload");
        }

        @Override
        public void on(CommandContext context) {
            try {
                MusicBot.reloadConfigs();
                RadioCommand.reloadUsageMessage();
            } catch (Exception e) {
                logger.error("error performing reload command", e);
                context.reply("Could not reload configs: " + e.getMessage());
                return;
            }
            context.reply("Configs reloaded!");
        }
    }
}
