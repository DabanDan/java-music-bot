package ovh.not.javamusicbot;

import java.util.function.BiConsumer;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class Selection<T> {
    private final T[] items;
    private final Formatter<T> formatter;

    private final BiConsumer<Boolean, T> callback;

    Selection(T[] items, Formatter<T> formatter, BiConsumer<Boolean, T> callback) {
        this.items = items;
        this.formatter = formatter;
        this.callback = callback;
    }

    public T[] getItems() {
        return items;
    }

    public BiConsumer<Boolean, T> getCallback() {
        return callback;
    }

    String createMessage() {
        return IntStream.range(0, items.length - 1)
                .mapToObj(i -> String.format("`%d` %s", i + 1, formatter.format(items[i])))
                .collect(Collectors.joining("\n"))
                + "\n\n**To choose**, use `{{prefix}}choose <number>`\nExample: `{{prefix}}choose 2` would pick the "
                + "second option.\n**To cancel**, use `{{prefix}}cancel`.";
    }

    public interface Formatter<T> {
        java.lang.String format(T t);
    }
}