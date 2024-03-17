import java.util.*;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

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

    public Map<T, U> backtrack (Map<T, U> solution)
    {
        if(solution.size() == variables.size())
        {
            return solution;
        }

        T var = variables.stream().filter(x -> !solution.containsKey(x)).findFirst().get();
        List<U> domain = new ArrayList<>(domains.get(var).stream().toList());
        Collections.shuffle(domain);
        for(U dom : domain)
        {
            Map<T, U> temp = new HashMap<>(solution);
            temp.put(var, dom);
            if(solutionValid(var, temp))
            {
                Map<T, U> finalResult = backtrack(temp);
                if(finalResult != null)
                {
                    return finalResult;
                }
            }
        }
        return null;
    }

}
