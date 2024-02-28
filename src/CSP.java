import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CSP <T, U> {
    private List<T> variables;
    private Map<T, List<U>> domains;
    private Map<T, List<Constraint<T, U>>> constraints;

    public CSP(List<T> variables, Map<T, List<U>> domains) {
        this.variables = variables;
        this.domains = domains;
        constraints = new HashMap<>();
        variables.forEach(var -> {
            constraints.putIfAbsent(var, new ArrayList<>());
        });
    }

    public void addConstraint(Constraint<T, U> constraint)
    {
        constraint.variables.forEach(var -> constraints.get(var).add(constraint));
    }

    public boolean solutionValid(T var, Map<T, U> solution)
    {
        return constraints.get(var).stream().allMatch(x -> x.checkConstraint(solution));
    }



}
