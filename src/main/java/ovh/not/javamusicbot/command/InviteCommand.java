package ovh.not.javamusicbot.command;

import ovh.not.javamusicbot.command.base.CachedTextResponseCommand;
import ovh.not.javamusicbot.CommandContext;
import ovh.not.javamusicbot.MusicBot;

public class InviteCommand extends CachedTextResponseCommand {
    public InviteCommand() {
        super("invite", "addbot");
    }

    @Override
    protected String cachedTextResponse(CommandContext context) {
        return "Invite dabBot: " + MusicBot.getConfigs().config.invite;
    }
}
