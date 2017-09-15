package ovh.not.javamusicbot.command;

import ovh.not.javamusicbot.command.base.CachedTextResponseCommand;
import ovh.not.javamusicbot.CommandContext;
import ovh.not.javamusicbot.MusicBot;

public class AboutCommand extends CachedTextResponseCommand {
    public AboutCommand() {
        super("about", "info", "support");
    }

    @Override
    protected String cachedTextResponse(CommandContext context) {
        return MusicBot.getConfigs().config.about;
    }
}
