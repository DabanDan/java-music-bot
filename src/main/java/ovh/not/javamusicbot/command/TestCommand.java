package ovh.not.javamusicbot.command;

import ovh.not.javamusicbot.AbstractPipelineCommand;
import ovh.not.javamusicbot.CommandContext;

public class TestCommand extends AbstractPipelineCommand {
    public TestCommand() {
        super("test", "ping");

        super.getPipeline().before(context -> {
            if (context.getArgs().isEmpty()) {
                context.reply("Usage: {{prefix}}test <some args here>");
                return false;
            }
            return true;
        });

        super.getPipeline().after((context, result) -> {
            context.reply(result.toString());
            return true;
        });
    }

    @Override
    public Object run(CommandContext context) {
        return String.format("got your command! args: %s", context.getArgs().toString());
    }
}
