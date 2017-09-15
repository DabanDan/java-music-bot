package ovh.not.javamusicbot.command.base;

import ovh.not.javamusicbot.CommandContext;

public abstract class TextResponseCommand extends PipelineCommand {
    TextResponseCommand(String name, String... names) {
        super(name, names);
    }

    @Override
    public Object run(CommandContext context) {
        return context.reply(textResponse(context));
    }

    protected abstract String textResponse(CommandContext context);
}
