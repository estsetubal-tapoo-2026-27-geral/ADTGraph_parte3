package tapoo.metro.model;

/** Station data; the application validates names and enforces uniqueness. */
public final class Station {
    private final String name;
    public Station(String name) { this.name = name; }
    public String getName() { return name; }
    @Override public String toString() { return name; }
}
