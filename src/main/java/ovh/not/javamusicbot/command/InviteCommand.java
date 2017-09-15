package ovh.not.javamusicbot.command;

import ovh.not.javamusicbot.AbstractCachedTextResponseCommand;
import ovh.not.javamusicbot.CommandContext;
import ovh.not.javamusicbot.MusicBot;

public class InviteCommand extends AbstractCachedTextResponseCommand {
    public InviteCommand() {
        super("invite", "addbot");
    }

    @Override
    protected String cachedTextResponse(CommandContext context) {
        return "Invite dabBot: " + MusicBot.getConfigs().config.invite;
    }
}
