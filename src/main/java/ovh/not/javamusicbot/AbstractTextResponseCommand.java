package ovh.not.javamusicbot;

public abstract class AbstractTextResponseCommand extends AbstractPipelineCommand {
    AbstractTextResponseCommand(String name, String... names) {
        super(name, names);
    }

    @Override
    public void on(CommandContext context) {
        context.reply(textResponse(context));
    }

    protected abstract String textResponse(CommandContext context);
}
