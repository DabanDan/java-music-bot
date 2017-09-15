package ovh.not.javamusicbot;

public abstract class AbstractCommand {
    private String[] names;

    protected AbstractCommand(String name, String... names) {
        this.names = new String[names.length + 1];
        this.names[0] = name;
        System.arraycopy(names, 0, this.names, 1, names.length);
    }

    public String[] getNames(){
        return names;
    }

    public abstract void on(CommandContext context);
}
