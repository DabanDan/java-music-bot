package ovh.not.javamusicbot.command.base;

import ovh.not.javamusicbot.CommandContext;

public abstract class AbstractNoResponsePipelineCommand extends AbstractPipelineCommand {
    protected AbstractNoResponsePipelineCommand(String name, String... names) {
        super(name, names);

        // only allow before handlers as the execution result wont be used
        super.setHandlersScope(BEFORE_HANDLERS_SCOPE);
    }

    @Override
    protected Object run(CommandContext context) {
        noResponse(context);
        return null;
    }

    protected abstract void noResponse(CommandContext context);
}
