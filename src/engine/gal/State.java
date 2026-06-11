package engine.gal;

public class State {

    public static final State DEAD = new State("", 0);

    String mode;
    int id;

    public State(String mode, int id) {
        assert mode != null;
        this.mode = mode;
        this.id = id;
    }

    public String mode() { return mode; }
    public int id() { return id; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof State)) return false;
        return equals((State) o);
    }

    public boolean equals(State s) {
        if (s == null) return false;
        return mode.equals(s.mode) && id == s.id;
    }

    @Override
    public int hashCode() {
        return 31 * mode.hashCode() + id;
    }
}