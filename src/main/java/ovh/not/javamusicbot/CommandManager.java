package ovh.not.javamusicbot;

import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.player.DefaultAudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.source.AudioSourceManagers;
import ovh.not.javamusicbot.command.*;

import java.util.HashMap;
import java.util.Map;

public class CommandManager {
    private final Map<String, Command> commands = new HashMap<>();

    CommandManager() {
        AudioPlayerManager playerManager = new DefaultAudioPlayerManager();

        AudioSourceManagers.registerRemoteSources(playerManager);

        CommandManager.register(commands,
                new AboutCommand(),
                new AdminCommand(),
                new ChooseCommand(),
                new DiscordFMCommand(this, playerManager),
                new DumpCommand(),
                new HelpCommand(),
                new InviteCommand(),
                new JumpCommand(),
                new LoadCommand(),
                new LoopCommand(),
                new MoveCommand(),
                new NowPlayingCommand(),
                new PauseCommand(),
                new PlayCommand(this, playerManager),
                new QueueCommand(),
                new RadioCommand(this, playerManager),
                new RemoveCommand(),
                new ReorderCommand(),
                new RepeatCommand(),
                new RestartCommand(),
                new SearchCommand(this),
                new ShuffleCommand(),
                new SkipCommand(),
                new SoundCloudCommand(this, playerManager),
                new StopCommand(),
                new VolumeCommand()
        );
    }

    public Map<String, Command> getCommands() {
        return commands;
    }

    public static void register(Map<String, Command> commands, Command... cmds) {
        for (Command command : cmds) {
            for (String name : command.getNames()) {
                if (commands.containsKey(name)) {
                    throw new RuntimeException(String.format("Command name collision %s in %s!", name,
                            command.getClass().getName()));
                }
                commands.put(name, command);
            }
        }
    }

    Command getCommand(String name) {
        return commands.get(name);
    }
}
