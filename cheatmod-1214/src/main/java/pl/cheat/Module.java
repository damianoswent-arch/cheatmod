package pl.cheat;

public class Module {
    public final String name;
    public boolean enabled;

    public Module(String name) { this.name = name; }

    public void toggle() { enabled = !enabled; }
}
