package ovh.not.javamusicbot.command.base;

import ovh.not.javamusicbot.CommandContext;

public abstract class AbstractTextResponseCommand extends AbstractPipelineCommand {
    protected AbstractTextResponseCommand(String name, String... names) {
        super(name, names);
    }

    @Override
    public Object run(CommandContext context) {
        String response = textResponse(context);
        if (response != null && response.length() > 0) {
            return context.reply(response);
        }
        return null;
    }

    protected abstract String textResponse(CommandContext context);
}
