import javax.crypto.spec.PSource;
import java.util.*;
import java.util.concurrent.atomic.AtomicReference;

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

        for(U dom : domains.get(var))
        {
            Map<T, U> temp = new HashMap<>(solution);
            temp.put(var, dom);

            if(solutionValid(var, temp))
            {
                Map<T,List<U>> domainsErased = new HashMap<>(domains);
                for (T intersectingEmpty : variables.stream().filter(x ->
                        (!solution.containsKey(x) && ((Position)x).intersecting.values().stream().anyMatch(y -> y.position == (Position)var))).toList()) {
                    for (U dom2 : domains.get(var)) {
                        temp.put(intersectingEmpty, dom2);
                        if (!solutionValid(intersectingEmpty, temp)) {
                            domainsErased.get(intersectingEmpty).remove(dom2);
                        }
                        temp.remove(intersectingEmpty, dom2);
                    }
                }

                Map<T, List<U>> domainsBckp = new HashMap<>(domains);
                domains = domainsErased;

                Map<T, U> finalResult = backtrack(temp);
                if(finalResult != null)
                {
                    return finalResult;
                }

                domains = domainsBckp;
            }
            //System.out.println(variables.stream().filter(solution::containsKey).count());
        }

        return null;
    }

}
