package ovh.not.javamusicbot.command;

import ovh.not.javamusicbot.*;

public class VolumeCommand extends Command {
    public VolumeCommand() {
        super("volume", "v");
    }

    @Override
    public void on(CommandContext context) {
        if (!MusicBot.getConfigs().config.patreon) {
            context.reply("**The volume command is dabBot premium only!**" +
                    "\nDonate for the `Super supporter` tier on Patreon at https://patreon.com/dabbot to gain access.");
            return;
        }

        MusicManager musicManager = GuildManager.getInstance().getMusicManager(context.getEvent().getGuild());
        if (!musicManager.isPlayingMusic()) {
            context.reply("No music is playing on this guild! To play a song use `{{prefix}}play`");
            return;
        }

        if (!Utils.allowedSuperSupporterPatronAccess(context.getEvent().getGuild())) {
            context.reply("**The volume command is dabBot premium only!**" +
                    "\nDonate for the `Super supporter` tier on Patreon at https://patreon.com/dabbot to gain access.");
            return;
        }

        if (context.getArgs().isEmpty()) {
            context.reply("Current volume: **%d**", musicManager.getPlayer().getVolume());
            return;
        }

        try {
            int newVolume = Math.max(1, Math.min(150, Integer.parseInt(context.getArgs().get(0))));
            musicManager.getPlayer().setVolume(newVolume);
            context.reply("Set volume to **%d**", newVolume);
        } catch (NumberFormatException e) {
            context.reply("Invalid volume. Bounds: `10 - 100`");
        }
    }
}
