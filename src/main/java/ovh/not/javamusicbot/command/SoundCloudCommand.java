package ovh.not.javamusicbot.command;

import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager;
import ovh.not.javamusicbot.CommandManager;

import java.util.List;

public class SoundCloudCommand extends ovh.not.javamusicbot.command.base.PlayCommand {
    public SoundCloudCommand(CommandManager commandManager, AudioPlayerManager playerManager) {
        super(commandManager, playerManager, "soundcloud", "sc");
        // so that when the LoadResultHandler fails it doesn't try to search on youtube :ok_hand:
        this.allowSearch = false;
        this.isSearch = true;
    }

    @Override
    protected String noArgumentMessage() {
        return "Usage: `{{prefix}}soundcloud <song title>` - searches for a song from soundcloud\n\n" +
                "If you already have a link to a song, use `{{prefix}}play <link>`";
    }

    @Override
    protected void transformQuery(List<String> args) {
        args.add(0, "scsearch:");
    }
}
