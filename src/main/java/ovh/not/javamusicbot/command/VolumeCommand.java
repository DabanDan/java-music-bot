package ovh.not.javamusicbot.command;

import ovh.not.javamusicbot.*;
import ovh.not.javamusicbot.command.base.AbstractTextResponseCommand;
import ovh.not.javamusicbot.command.base.PipelineHandlers;

public class VolumeCommand extends AbstractTextResponseCommand {
    public VolumeCommand() {
        super("volume", "v");

        // require music playing, patron bot enabled and super supporter role
        getPipeline()
                .before(PipelineHandlers.requiresMusicHandler())
                .before(PipelineHandlers.patronOnlyCommandHandler())
                .before(PipelineHandlers.requireSuperSupporterHandler());
    }

    @Override
    public String textResponse(CommandContext context) {
        if (!Utils.allowedSuperSupporterPatronAccess(context.getEvent().getGuild())) {
            return "**The volume command is dabBot premium only!**" +
                    "\nDonate for the `Super supporter` tier on Patreon at https://patreon.com/dabbot to gain access.";
        }

        MusicManager musicManager = GuildManager.getInstance().getMusicManager(context.getEvent().getGuild());

        if (context.getArgs().isEmpty()) {
            return String.format("Current volume: **%d**", musicManager.getPlayer().getVolume());
        }

        try {
            int newVolume = Math.max(1, Math.min(150, Integer.parseInt(context.getArgs().get(0))));
            musicManager.getPlayer().setVolume(newVolume);
            return String.format("Set volume to **%d**", newVolume);
        } catch (NumberFormatException e) {
            return "Invalid volume. Bounds: `10 - 100`";
        }
    }
}
