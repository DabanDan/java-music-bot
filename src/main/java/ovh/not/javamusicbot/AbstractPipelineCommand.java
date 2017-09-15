package ovh.not.javamusicbot;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Function;

public abstract class AbstractPipelineCommand extends AbstractCommand {
    private CommandPipeline pipeline = null;

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
            pipeline = new CommandPipeline();
        }
        return pipeline;
    }

    @SuppressWarnings("unchecked")
    public class CommandPipeline {
        private final List<Function<CommandContext, Boolean>> beforeHanlers = new ArrayList<>();
        private final List<BiFunction<CommandContext, Object, Boolean>> afterHandlers = new ArrayList<>();

        private CommandPipeline addHandler(List handlers, Object handler) {
            handlers.add(handler);
            return this;
        }

        public CommandPipeline before(Function<CommandContext, Boolean> handler) {
            return addHandler(beforeHanlers, handler);
        }

        public CommandPipeline after(BiFunction<CommandContext, Object, Boolean> handler) {
            return addHandler(afterHandlers, handler);
        }
    }
}
