import java.util.List;
import java.util.Map;

public abstract class Constraint<T, U> {
    List<T> variables;

    public Constraint(List<T> variables) {
        this.variables = variables;
    }

    public abstract boolean checkConstraint(Map<T, U> solution);
}
