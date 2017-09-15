package ovh.not.javamusicbot;

public abstract class Command {
    private String[] names;

    protected Command(String name, String... names) {
        this.names = new String[names.length + 1];
        this.names[0] = name;
        System.arraycopy(names, 0, this.names, 1, names.length);
    }

    public String[] getNames(){
        return names;
    }

    public abstract void on(CommandContext context);
}
