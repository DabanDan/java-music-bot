package ovh.not.javamusicbot;

import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.player.DefaultAudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.source.bandcamp.BandcampAudioSourceManager;
import com.sedmelluq.discord.lavaplayer.source.beam.BeamAudioSourceManager;
import com.sedmelluq.discord.lavaplayer.source.http.HttpAudioSourceManager;
import com.sedmelluq.discord.lavaplayer.source.soundcloud.SoundCloudAudioSourceManager;
import com.sedmelluq.discord.lavaplayer.source.twitch.TwitchStreamAudioSourceManager;
import com.sedmelluq.discord.lavaplayer.source.vimeo.VimeoAudioSourceManager;
import com.sedmelluq.discord.lavaplayer.source.youtube.YoutubeAudioSourceManager;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import lavalink.client.io.Lavalink;
import net.dv8tion.jda.core.entities.Member;
import ovh.not.javamusicbot.command.*;

import java.util.HashMap;
import java.util.Map;

public class CommandManager {
    private final Map<String, Command> commands = new HashMap<>();
    private final Map<Member, Selection<AudioTrack, String>> selectors = new HashMap<>();

    CommandManager(Lavalink lavalink) {
        CommandManager.register(commands,
                new AboutCommand(),
                new AdminCommand(),
                new ChooseCommand(this),
                new DiscordFMCommand(this),
                new DumpCommand(),
                new HelpCommand(this),
                new InviteCommand(),
                new JumpCommand(),
                new LoadCommand(),
                new LoopCommand(),
                new MoveCommand(),
                new NowPlayingCommand(),
                new PauseCommand(),
                new PlayCommand(this),
                new QueueCommand(),
                new RadioCommand(this),
                new RemoveCommand(),
                new ReorderCommand(),
                new RepeatCommand(),
                new RestartCommand(),
                new SearchCommand(this),
                new ShuffleCommand(),
                new SkipCommand(),
                new SoundCloudCommand(this),
                new StopCommand(),
                new VolumeCommand()
        );
    }

    public Map<String, Command> getCommands() {
        return commands;
    }

    public Map<Member, Selection<AudioTrack, String>> getSelectors() {
        return selectors;
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
