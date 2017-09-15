package ovh.not.javamusicbot.command;

import ovh.not.javamusicbot.AbstractCommand;
import ovh.not.javamusicbot.CommandContext;
import ovh.not.javamusicbot.MusicBot;

public class InviteCommand extends AbstractCommand {
    public InviteCommand() {
        super("invite", "addbot");
    }

    @Override
    public void on(CommandContext context) {
        context.reply("Invite dabBot: " + MusicBot.getConfigs().config.invite);
    }
}
