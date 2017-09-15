package ovh.not.javamusicbot.command.base;

import ovh.not.javamusicbot.CommandContext;

import java.util.function.Function;

public class PipelineHandlers {
    public static Function<CommandContext, Boolean> argumentCheckHandler(String message, int... requirements) {
        int min = requirements[0];

        // because max has to be effectively final for a lambda
        int nonFinalMax = -1;
        if (requirements.length > 1) {
            nonFinalMax = requirements[1];
        }
        int max = nonFinalMax;

        return context -> {
            int args = context.getArgs().size();

            if (context.getArgs().size() < min) {
                context.reply(message);
                return false;
            }

            if (max != -1 && args > max) {
                context.reply(message);
                return false;
            }

            return true;
        };
    }
}
