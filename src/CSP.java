import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

public class CSP {
    private List<Position> variables;
    private Map<Position, HashSet<String>> domains;
    private Map<Position, List<Constraint<Position, String>>> constraints;
    private  Map<Integer, Map<Character, Map<Integer, Set<String>>>> zboroviMapirani;

    public CSP(List<Position> variables, Map<Position, HashSet<String>> domains,  Map<Integer, Map<Character, Map<Integer, Set<String>>>> zboroviMapirani) {
        this.variables = variables;
        this.domains = domains;
        constraints = new HashMap<>();
        variables.forEach(var -> {
            constraints.putIfAbsent(var, new ArrayList<>());
        });
        this.zboroviMapirani = zboroviMapirani;
    }

    @Override
    public String toString() {
        char[][] mat = new char[15][15];

        variables.forEach(orel -> mat[orel.x][orel.y] = 'X');

        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < mat.length; i++) {
            for (int j = 0; j < mat[i].length; j++) {
                if (mat[i][j] == 'X') {
                    sb.append(mat[i][j] + " ");
                } else {
                    sb.append("  ");
                }
            }
            sb.append("\n");
        }

        return sb.toString();
    }

    public void addConstraint(Constraint<Position, String> constraint)
    {
        constraint.variables.forEach(var -> constraints.get(var).add(constraint));
    }

    public boolean solutionValid(Position var, Map<Position, String> solution)
    {
        return constraints.get(var).stream().allMatch(x -> x.checkConstraint(solution));
    }

    public Map<Position, String> backtrack (Map<Position, String> solution)
    {
        if(solution.size() == variables.stream().filter(x -> x.length >= 4).count())
        {
            return solution;
        }

//        Position var = variables.stream().filter(x -> !solution.containsKey(x)).sorted(Comparator.comparingInt(x -> Math.toIntExact(-((Position) x).intersecting.values().stream().filter(y -> y.position.length >= 4).count()))).findFirst().get();
        Position var = variables.stream().filter(x -> x.length >= 4)
                .filter(x -> !solution.containsKey(x)).sorted(Comparator.comparingInt(x -> domains.get(x).size()).thenComparing(x -> -((Position) x).intersecting.values().stream().filter(y -> y.position.length >= 4).count())).findFirst().get();
//        Position var = variables.stream().filter(x -> !solution.containsKey(x)).sorted(Comparator.comparingInt(x -> -((Position) x).intersecting.size())).findFirst().get();
        List<String> shuffled =  new ArrayList<>(domains.get(var));
        Collections.shuffle(shuffled);

        for(String dom : shuffled)
        {
            Map<Position, String> temp = new HashMap<>(solution);
            temp.put(var, dom);
            Map<Position, HashSet<String>> domainsBckp = new HashMap<>(domains);
            if(solutionValid(var, temp))
            {

//                for (Position intersectingEmpty : variables.stream().filter(x ->
//                        (!solution.containsKey(x) && ((Position)x).intersecting.values().stream().anyMatch(y -> y.position == (Position)var))).toList()) {
//                    for (String dom2 : domains.get(var)) {
//                        temp.put(intersectingEmpty, dom2);
//                        if (!solutionValid(intersectingEmpty, temp)) {
//                            domainsErased.get(intersectingEmpty).remove(dom2);
//                        }
//                        temp.remove(intersectingEmpty, dom2);
//                    }
//                }
//
//                Map<Position, List<String>> domainsBckp = new HashMap<>(domains);
//                domains = domainsErased;

                Position variable = (Position) var;
                for(Map.Entry<Integer, PositionIntersect> entry : ((Position) var).intersecting.entrySet().stream().filter(orelorel -> orelorel.getValue().position.length >= 4).toList())
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
//                            List<String> nd = (List<String>) new ArrayList<>(newDomain);
                            domains.get(var2).retainAll(newDomain);
                            if(domains.values().stream().anyMatch(Set::isEmpty))
                            {
//                                domains = domainsBckp;
                                List<Integer> emptyPositions;

                                if(var2.direction == 'v')
                                {
                                   // System.out.println(this);
                                   // System.out.println("===============");

                                    emptyPositions = var2.intersecting.entrySet().stream().filter(x -> x.getKey() > 0 && x.getKey() < var2.length - 1 && !temp.containsKey(x.getValue().position)).map(x -> x.getKey()).sorted((x1, x2) -> {
                                        int distanceToMiddle = Math.abs(var2.y - var2.length) / 2;
                                        return Integer.compare(Math.abs(x1 - distanceToMiddle), Math.abs(x2 - distanceToMiddle));
                                    }).toList();

                                    emptyPositions.forEach(x -> {
                                        Position p = new Position(var2.x + x + 1, var2.y, var2.direction);
                                        p.length = var2.length - x - 1;
                                        var2.intersecting.entrySet().stream().filter(stara -> stara.getKey() > x).forEach(orel -> p.intersecting.put(orel.getKey(), orel.getValue()));
                                        List<Position> newVariables =  variables.stream().map(Position::copy).collect(Collectors.toCollection(ArrayList::new));
                                        newVariables.forEach(presek -> {
                                            Map<Integer, PositionIntersect> preseci = presek.intersecting;
                                            presek.intersecting = new HashMap<>();
                                            preseci.forEach((key, value) -> {
                                                Position intersectingPosition = value.position;
                                                Position newIntersectingPosition = newVariables.stream().filter(news -> news.x == intersectingPosition.x && news.y == intersectingPosition.y).findFirst().get();
                                                presek.intersecting.put(key, new PositionIntersect(newIntersectingPosition, value.point));
                                            });
                                        });
                                        newVariables.add(p);
                                        newVariables.stream().filter(stara -> stara.equals(var2)).findFirst().get().length = x;
                                        newVariables.stream().filter(stara -> stara.equals(var2)).findFirst().get().intersecting = newVariables.stream().filter(stara -> stara.equals(var2)).findFirst().get().intersecting.entrySet().stream().filter(orel -> orel.getKey() < x).collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));;
                                        Map<Position, HashSet<String>> novDomain = new HashMap<>(domains);
                                        novDomain.put(p, zboroviMapirani.get(p.length).values().stream().flatMap(orel -> orel.values().stream().flatMap(orel2 -> orel2.stream())).collect(Collectors.toCollection(HashSet::new)));
                                        novDomain.put(var2, zboroviMapirani.get(x).values().stream().flatMap(orel -> orel.values().stream().flatMap(orel2 -> orel2.stream())).collect(Collectors.toCollection(HashSet::new)));

                                        Iterator<Map.Entry<Integer, PositionIntersect>> iter = var2.intersecting.entrySet().iterator();
                                        AtomicInteger i = new AtomicInteger();
                                        var2.intersecting.entrySet().forEach(orel -> {
                                            if(i.get() > x)
                                            {
                                                orel.getValue().position.intersecting.entrySet().stream().filter(orel2 -> orel2.getValue().equals(var2))
                                                        .forEach(orel2 -> orel.getValue().position.intersecting.put(orel2.getKey(), new PositionIntersect(p, i.get() - x - 1)));
                                                i.getAndIncrement();
                                            }
                                            if(i.get() == x)
                                            {
                                                orel.getValue().position.intersecting.remove(var2.x - orel.getValue().position.x- 1);
                                            }
                                        });



                                        Position intersectingWord = var2.intersecting.get(x).position;
//                                        Position p2 = new Position(intersectingWord.x + var2.intersecting.get(x - 1).point, intersectingWord.y, 'h');
                                        Position p2 = new Position(var2.x + x + 1, var2.y, 'h');
                                        p2.length = intersectingWord.length - var2.intersecting.get(x - 1).point - 1;
                                        intersectingWord.intersecting.entrySet().stream().filter(stara -> stara.getKey() > var2.intersecting.get(x).point).forEach(orel -> p2.intersecting.put(orel.getKey(), orel.getValue()));
                                        newVariables.add(p2);
                                        newVariables.stream().filter(stara -> stara.equals(intersectingWord)).findFirst().get().length = var2.intersecting.get(x).point;
                                        newVariables.stream().filter(stara -> stara.equals(intersectingWord)).findFirst().get().intersecting =
                                                newVariables.stream()
                                                        .filter(stara -> stara.equals(intersectingWord)).findFirst().orElseThrow(() -> new RuntimeException("Majka ti")).intersecting
                                                        .entrySet().stream()
                                                        .filter(orel -> orel.getKey() < var2.intersecting.get(x).point)
                                                        .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));

                                        novDomain.put(p2, zboroviMapirani.get(p2.length).values().stream().flatMap(orel -> orel.values().stream().flatMap(orel2 -> orel2.stream())).collect(Collectors.toCollection(HashSet::new)));
                                        novDomain.put(intersectingWord, zboroviMapirani.get(var2.intersecting.get(x).point).values().stream().flatMap(orel -> orel.values().stream().flatMap(orel2 -> orel2.stream())).collect(Collectors.toCollection(HashSet::new)));

                                        i.set(p2.x);
                                        int start = var2.intersecting.get(x).point;
                                        var2.intersecting.get(x).position.intersecting.entrySet().forEach(orel -> {
                                            if(i.get() > start)
                                            {
                                                orel.getValue().position.intersecting.entrySet().stream().filter(orel2 -> orel2.getValue().equals(var2))
                                                        .forEach(orel2 -> orel.getValue().position.intersecting.put(orel2.getKey(), new PositionIntersect(p2, i.get() - start - 1)));
                                                i.getAndIncrement();
                                            }
                                            if(i.get() == x)
                                            {
                                                orel.getValue().position.intersecting.remove(var2.x - orel.getValue().position.x- 1);
                                            }
                                        });


                                        CSP krstozbor = new CSP(newVariables, novDomain, zboroviMapirani);
                                        krstozbor.addConstraint(new WordLengthConstraint(newVariables));
                                        krstozbor.addConstraint(new AllDifferentConstraint(newVariables));
                                        krstozbor.addConstraint(new IntersectionConstraint(newVariables));
                                        System.out.println(this);
                                        System.out.println("===============");
                                        System.out.println(krstozbor);
                                        System.out.println("^^^^^^^^^^");
                                        Map<Position, String> resenie = krstozbor.backtrack(temp);

                                        if (resenie != null) {
                                            System.out.println("TEMP RESHENIE");
                                            System.out.println("TEMP RESHENIE");
                                            System.out.println("TEMP RESHENIE");
                                            System.out.println("TEMP RESHENIE");
                                            System.out.println(krstozbor);
                                            System.out.println("TEMP RESHENIE");
                                            System.out.println(resenie);
                                        }
                                    });
                                 }
                                else
                                {
                                    emptyPositions = var2.intersecting.entrySet().stream().filter(x -> x.getKey() > 0 && x.getKey() < var2.length - 1 && !temp.containsKey(x.getValue().position)).map(x -> x.getKey()).sorted((x1, x2) -> {
                                        int distanceToMiddle = Math.abs(var2.x - var2.length) / 2;
                                        return Integer.compare(Math.abs(x1 - distanceToMiddle), Math.abs(x2 - distanceToMiddle));
                                    }).toList();
                                }




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

                Map<Position, String> finalResult = backtrack(temp);
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
