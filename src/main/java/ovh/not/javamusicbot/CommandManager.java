package ovh.not.javamusicbot;

import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.player.DefaultAudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.source.AudioSourceManagers;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import net.dv8tion.jda.core.entities.Member;
import ovh.not.javamusicbot.command.*;

import java.util.HashMap;
import java.util.Map;

public class CommandManager {
    private final Map<String, AbstractCommand> commands = new HashMap<>();
    private final Map<Member, Selection<AudioTrack, String>> selectors = new HashMap<>();

    CommandManager() {
        AudioPlayerManager playerManager = new DefaultAudioPlayerManager();

        AudioSourceManagers.registerRemoteSources(playerManager);

        CommandManager.register(commands,
                new AboutCommand(),
                new AdminCommand(),
                new ChooseCommand(this),
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
                new VolumeCommand(),

                new TestCommand() // todo remove this
        );
    }

    public Map<String, AbstractCommand> getCommands() {
        return commands;
    }

    public Map<Member, Selection<AudioTrack, String>> getSelectors() {
        return selectors;
    }


    public static void register(Map<String, AbstractCommand> commands, AbstractCommand... cmds) {
        for (AbstractCommand command : cmds) {
            for (String name : command.getNames()) {
                if (commands.containsKey(name)) {
                    throw new RuntimeException(String.format("AbstractCommand name collision %s in %s!", name,
                            command.getClass().getName()));
                }
                commands.put(name, command);
            }
        }
    }

    AbstractCommand getCommand(String name) {
        return commands.get(name);
    }
}
