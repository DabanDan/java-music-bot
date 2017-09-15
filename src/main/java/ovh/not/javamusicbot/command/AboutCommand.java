package ovh.not.javamusicbot.command;

import ovh.not.javamusicbot.AbstractCommand;
import ovh.not.javamusicbot.CommandContext;
import ovh.not.javamusicbot.MusicBot;

public class AboutCommand extends AbstractCommand {
    private final String about;

    public AboutCommand() {
        super("about", "info", "support");
        this.about = MusicBot.getConfigs().config.about;
    }

    @Override
    public void on(CommandContext context) {
        context.reply(about);
    }
}
