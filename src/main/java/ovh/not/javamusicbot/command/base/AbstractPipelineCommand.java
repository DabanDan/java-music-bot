package ovh.not.javamusicbot.command.base;

import ovh.not.javamusicbot.CommandContext;
import ovh.not.javamusicbot.Command;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Function;

public abstract class AbstractPipelineCommand extends Command {
    protected static final byte BEFORE_HANDLERS_SCOPE = 0x1;
    protected static final byte AFTER_HANDLERS_SCOPE = 0x2;

    private CommandPipeline pipeline = null;
    private byte handlersScope = BEFORE_HANDLERS_SCOPE | AFTER_HANDLERS_SCOPE;

    protected AbstractPipelineCommand(String name, String... names) {
        super(name, names);
    }

    @Override
    public void on(CommandContext context) {
        boolean usingPipeline = pipeline != null;

        if (usingPipeline) {
            for (Function<CommandContext, Boolean> beforeHanler : pipeline.beforeHanlers) {
                if (!beforeHanler.apply(context)) return; // false = do not continue
            }
        }

        Object result = run(context);

        if (usingPipeline) {
            for (BiFunction<CommandContext, Object, Boolean> afterHandler : pipeline.afterHandlers) {
                if (!afterHandler.apply(context, result)) return; // false = do not continue
            }
        }
    }

    protected abstract Object run(CommandContext context);

    protected CommandPipeline getPipeline() {
        if (pipeline == null) {
            pipeline = new CommandPipeline(handlersScope);
        }
        return pipeline;
    }

    public void setHandlersScope(byte handlersScope) {
        this.handlersScope = handlersScope;
    }

    @SuppressWarnings("unchecked")
    public class CommandPipeline {
        private final List<Function<CommandContext, Boolean>> beforeHanlers = new ArrayList<>();
        private final List<BiFunction<CommandContext, Object, Boolean>> afterHandlers = new ArrayList<>();

        private final byte handlersScope;

        private CommandPipeline(byte handlersScope) {
            this.handlersScope = handlersScope;
        }

        private CommandPipeline addHandler(List handlers, Object handler, byte scope) {
            if ((handlersScope & scope) != scope) {
                throw new IllegalStateException("pipeline handler not in scope");
            }

            handlers.add(handler);
            return this;
        }

        public CommandPipeline before(Function<CommandContext, Boolean> handler) {
            return addHandler(beforeHanlers, handler, BEFORE_HANDLERS_SCOPE);
        }

        public CommandPipeline after(BiFunction<CommandContext, Object, Boolean> handler) {
            return addHandler(afterHandlers, handler, AFTER_HANDLERS_SCOPE);
        }
    }
}
