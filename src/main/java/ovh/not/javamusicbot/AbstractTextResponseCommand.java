package ovh.not.javamusicbot;

public abstract class AbstractTextResponseCommand extends AbstractPipelineCommand {
    protected AbstractTextResponseCommand(String name, String... names) {
        super(name, names);

        super.getPipeline().after((context, result) -> {
            context.reply(result.toString());
            return true;
        });
    }

    @Override
    protected Object run(CommandContext context) {
        return textResponse(context);
    }

    protected abstract String textResponse(CommandContext context);
}
