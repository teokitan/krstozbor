import javax.crypto.spec.PSource;
import java.util.*;
import java.util.concurrent.atomic.AtomicReference;

public class CSP <T, U> {
    private List<T> variables;
    private Map<T, List<U>> domains;
    private Map<T, List<Constraint<T, U>>> constraints;
    private  Map<Integer, Map<Character, Map<Integer, Set<String>>>> zboroviMapirani;

    public CSP(List<T> variables, Map<T, List<U>> domains,  Map<Integer, Map<Character, Map<Integer, Set<String>>>> zboroviMapirani) {
        this.variables = variables;
        this.domains = domains;
        constraints = new HashMap<>();
        variables.forEach(var -> {
            constraints.putIfAbsent(var, new ArrayList<>());
        });
        this.zboroviMapirani = zboroviMapirani;
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

        T var = variables.stream().filter(x -> !solution.containsKey(x)).sorted(Comparator.comparingInt(x -> domains.get(x).size()).thenComparing(x -> -((Position) x).intersecting.values().stream().filter(y -> y.position.length >= 4).count())).findFirst().get();
//        T var = variables.stream().filter(x -> !solution.containsKey(x)).sorted(Comparator.comparingInt(x -> -((Position) x).intersecting.size())).findFirst().get();
//        List<U> shuffled =  new ArrayList<>(domains.get(var));
//        Collections.shuffle(shuffled);
        for(U dom : domains.get(var))
        {
            Map<T, U> temp = new HashMap<>(solution);
            temp.put(var, dom);
            Map<T, List<U>> domainsBckp = new HashMap<>(domains);
            if(solutionValid(var, temp))
            {

//                for (T intersectingEmpty : variables.stream().filter(x ->
//                        (!solution.containsKey(x) && ((Position)x).intersecting.values().stream().anyMatch(y -> y.position == (Position)var))).toList()) {
//                    for (U dom2 : domains.get(var)) {
//                        temp.put(intersectingEmpty, dom2);
//                        if (!solutionValid(intersectingEmpty, temp)) {
//                            domainsErased.get(intersectingEmpty).remove(dom2);
//                        }
//                        temp.remove(intersectingEmpty, dom2);
//                    }
//                }
//
//                Map<T, List<U>> domainsBckp = new HashMap<>(domains);
//                domains = domainsErased;

                Position variable = (Position) var;
                for(Map.Entry<Integer, PositionIntersect> entry : ((Position) var).intersecting.entrySet())
                {
                    int position = entry.getKey();
                    Position var2 = entry.getValue().position;
                    if(var2.length < 4)
                    {
                        continue;
                    }
                    int position2 = entry.getValue().point;
                    try {
                        Set<String> newDomain = zboroviMapirani.get(var2.length).get(temp.get(var).toString().charAt(position)).get(position2);
                        if(!temp.containsKey(var2))
                        {
//                            List<U> nd = (List<U>) new ArrayList<>(newDomain);
                            domains.put((T) var2, (List<U>) new ArrayList<>(newDomain));
                            if(domains.values().stream().anyMatch(List::isEmpty))
                            {
                                domains = domainsBckp;
                                return null;
                            }
                        }
                    }
                    catch (Exception ex)
                    {
                        domains = domainsBckp;
                        return null;
                    }
                }

//                domains = domainsErased;

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
