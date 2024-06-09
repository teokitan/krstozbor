import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

public class CSP {
    private List<Position> variables;
    private Map<Position, HashSet<Word>> domains;
    private Map<Position, List<Constraint<Position, Word>>> constraints;
    private  Map<Integer, Map<Character, Map<Integer, Set<Word>>>> zboroviMapirani;

    public CSP(List<Position> variables, Map<Position, HashSet<Word>> domains,  Map<Integer, Map<Character, Map<Integer, Set<Word>>>> zboroviMapirani) {
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
        char[][] mat = new char[Main.length][Main.width];

        variables.forEach(orel -> mat[orel.x][orel.y] = 'X');

        StringBuilder sb = new StringBuilder();
        sb.append("  0 1 2 3 4 5 6 7 8 9 0 1 2 3 4\n");
        for (int i = 0; i < mat.length; i++) {
            sb.append("" + (i % 10) + " ");
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

    public void addConstraint(Constraint<Position, Word> constraint)
    {
        constraint.variables.forEach(var -> constraints.get(var).add(constraint));
    }

    public boolean solutionValid(Position var, Map<Position, Word> solution)
    {
        return constraints.get(var).stream().allMatch(x -> x.checkConstraint(solution));
    }

    public Map<Position, Word> backtrack (Map<Position, Word> solution)
    {
        if(solution.size() == variables.stream().filter(x -> x.length >= 4).count())
        {
            return solution;
        }

        Position var = variables.stream().filter(x -> x.length >= 4)
                .filter(x -> !solution.containsKey(x)).sorted(Comparator.comparingInt(x -> domains.get(x).size()).thenComparing(x -> -((Position) x).intersecting.values().stream().filter(y -> y.position.length >= 4).count())).findFirst().get();
        List<Word> shuffled =  new ArrayList<>(domains.get(var));
        Collections.shuffle(shuffled);
        
        for(Word dom : shuffled)
        {
            Map<Position, Word> temp = new HashMap<>(solution);
            temp.put(var, dom);
            Map<Position, HashSet<Word>> domainsBckp = new HashMap<>(domains);
            boolean validAct = true;

            if(solutionValid(var, temp))
            {
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
                        Set<Word> newDomain = zboroviMapirani.get(var2.length).get(temp.get(var).word.toString().charAt(position)).get(position2);
                        if(!temp.containsKey(var2))
                        {
                            domains.get(var2).retainAll(newDomain);
                            if(domains.entrySet().stream().filter(orel -> orel.getKey().length >= 4).anyMatch(orel -> orel.getValue().isEmpty()))
                            {
                                domains = domainsBckp;
                                validAct = false;
                                break;
                            }
                        }
                    }
                    catch (Exception ex)
                    {
                        domains = domainsBckp;
                        validAct = false;
                        break;
                    }
                }

                if (validAct) {
                    Map<Position, Word> finalResult = backtrack(temp);
                    if(finalResult != null)
                    {
                        return finalResult;
                    }

                    domains = domainsBckp;
                }
            }
        }

        List<Integer> emptyPositions;
        AtomicReference<Map<Position, Word>> finalResenie = new AtomicReference<>();

        if(var.direction == 'v' && variables.stream().filter(orel -> orel.length >= 1).count() < 90)
        {
            emptyPositions = var.intersecting.entrySet().stream().filter(x -> x.getKey() > 0 && x.getKey() < var.length - 1 && !solution.containsKey(x.getValue().position)).map(x -> x.getKey()).sorted((x1, x2) -> {
                int distanceToMiddle = Math.abs(var.length / 2);
                return Integer.compare(Math.abs(x1 - distanceToMiddle), Math.abs(x2 - distanceToMiddle));
            }).toList();

            emptyPositions.forEach(x -> {
                if (finalResenie.get() != null) {
                    return;
                }

                Position p = new Position(var.x + x + 1, var.y, var.direction);
                p.length = var.length - x - 1;
                var.intersecting.entrySet().stream().filter(stara -> stara.getKey() > x).forEach(orel -> p.intersecting.put(orel.getKey()-x-1, orel.getValue()));
                List<Position> newVariables =  variables.stream().map(Position::copy).collect(Collectors.toCollection(ArrayList::new));
                newVariables.forEach(presek -> {
                    Map<Integer, PositionIntersect> preseci = variables.stream().filter(orel -> orel.equals(presek)).findFirst().get().intersecting;
                    presek.intersecting = new HashMap<>();
                    preseci.forEach((key, value) -> {
                        Position intersectingPosition = value.position;
                        Position newIntersectingPosition = newVariables.stream().filter(news -> news.x == intersectingPosition.x && news.y == intersectingPosition.y && news.direction != presek.direction).findFirst().get();
                        presek.intersecting.put(key, new PositionIntersect(newIntersectingPosition, value.point));
                    });
                });
                newVariables.add(p);
                newVariables.stream().filter(stara -> stara.equals(var)).findFirst().get().length = x;
                newVariables.stream().filter(stara -> stara.equals(var)).findFirst().get().intersecting = newVariables.stream().filter(stara -> stara.equals(var)).findFirst().get().intersecting.entrySet().stream().filter(orel -> orel.getKey() < x).collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));;
                Map<Position, HashSet<Word>> novDomain = new HashMap<>(domains);
                novDomain.put(p, zboroviMapirani.get(p.length).values().stream().flatMap(orel -> orel.values().stream().flatMap(orel2 -> orel2.stream())).collect(Collectors.toCollection(HashSet::new)));
                novDomain.put(var, zboroviMapirani.get(x).values().stream().flatMap(orel -> orel.values().stream().flatMap(orel2 -> orel2.stream())).collect(Collectors.toCollection(HashSet::new)));

                Iterator<Map.Entry<Integer, PositionIntersect>> iter = var.intersecting.entrySet().iterator();
                AtomicInteger i = new AtomicInteger();
                var.intersecting.entrySet().forEach(orel -> {
                    if(orel.getKey() > x)
                    {
                      //  int myKey = orel.getValue().position.intersecting.entrySet().stream().filter(orel2 -> orel2.getValue().position.equals(var)).findFirst().get().getKey();
                        newVariables.stream().filter(stara -> stara.equals(orel.getValue().position)).findFirst().get().intersecting.put(orel.getValue().point, new PositionIntersect(p, orel.getKey() - x - 1));
                        i.getAndIncrement();
                    }
                    if(orel.getKey() == x)
                    {
                        newVariables.stream().filter(stara -> stara.equals(orel.getValue().position)).findFirst().get().intersecting.remove(orel.getValue().point);
                    }
                });



                Position intersectingWord = var.intersecting.get(x).position;
//                                        Position p2 = new Position(intersectingWord.x + var.intersecting.get(x - 1).point, intersectingWord.y, 'h');
                Position p2 = new Position(var.x + x + 1, var.y, 'h');
                p2.length = intersectingWord.length - var.intersecting.get(x).point - 1;
                intersectingWord.intersecting.entrySet().stream().filter(stara -> stara.getKey() > var.intersecting.get(x).point).forEach(orel -> p2.intersecting.put(orel.getKey()-var.intersecting.get(x).point-1, orel.getValue()));
                newVariables.add(p2);
                newVariables.stream().filter(stara -> stara.equals(intersectingWord)).findFirst().get().length = var.intersecting.get(x).point;
                newVariables.stream().filter(stara -> stara.equals(intersectingWord)).findFirst().get().intersecting
                        .entrySet().removeIf(orel -> orel.getKey() >= var.intersecting.get(x).point);

                if (p2.length > 0) {
                    novDomain.put(p2, zboroviMapirani.get(p2.length).values().stream().flatMap(orel -> orel.values().stream().flatMap(orel2 -> orel2.stream())).collect(Collectors.toCollection(HashSet::new)));
                } else {
                    novDomain.put(p2, new HashSet<Word>());
                }

                if (var.intersecting.get(x).point != 0) {
                    novDomain.put(intersectingWord, zboroviMapirani.get(var.intersecting.get(x).point).values().stream().flatMap(orel -> orel.values().stream().flatMap(orel2 -> orel2.stream())).collect(Collectors.toCollection(HashSet::new)));
                } else {
                    novDomain.put(intersectingWord, new HashSet<Word>());
                }

                i.set(p2.x);
                int start = var.intersecting.get(x).point;
                var.intersecting.get(x).position.intersecting.entrySet().forEach(orel -> {
                    if(orel.getKey() > start)
                    {
                     //   orel.getValue().position.intersecting.entrySet().stream().filter(orel2 -> orel2.getValue().equals(var))
                     //           .forEach(orel2 -> orel.getValue().position.intersecting.put(orel2.getKey(), new PositionIntersect(p2, i.get() - start - 1)));
                        newVariables.stream().filter(stara -> stara.equals(orel.getValue().position)).findFirst().get().intersecting.put(orel.getValue().point, new PositionIntersect(p2, orel.getKey() - start - 1));
                        i.getAndIncrement();
                    }
                    if(orel.getKey() == start)
                    {
                        newVariables.stream().filter(stara -> stara.equals(orel.getValue().position)).findFirst().get().intersecting.remove(orel.getValue().point);
                    }
                });


                CSP krstozbor = new CSP(newVariables, novDomain, zboroviMapirani);
                krstozbor.addConstraint(new WordLengthConstraint(newVariables));
                krstozbor.addConstraint(new AllDifferentConstraint(newVariables));
                krstozbor.addConstraint(new IntersectionConstraint(newVariables));
                krstozbor.addConstraint(new DifficultyConstraint(newVariables));
                //System.out.println(this);
                //System.out.println("===============");
                //System.out.println(krstozbor);
                //System.out.println("^^^^^^^^^^");

                Map<Position, Word> solution2 = new TreeMap<>();
                solution.entrySet().forEach(varSolution -> solution2.put(newVariables.stream().filter(stara -> stara.equals(varSolution.getKey())).findFirst().get(), varSolution.getValue()));
                Map<Position, Word> resenie = krstozbor.backtrack(solution2);

                if (resenie != null) {
                    System.out.println("TEMP RESHENIE");
                    finalResenie.set(resenie);
                }
            });
        }

        if(var.direction == 'h' && variables.stream().filter(orel -> orel.length >= 1).count() < 90)
        {
            emptyPositions = var.intersecting.entrySet().stream().filter(y -> y.getKey() > 0 && y.getKey() < var.length - 1 && !solution.containsKey(y.getValue().position)).map(y -> y.getKey()).sorted((x1, x2) -> {
                int distanceToMiddle = Math.abs(var.length / 2);
                return Integer.compare(Math.abs(x1 - distanceToMiddle), Math.abs(x2 - distanceToMiddle));
            }).toList();

            emptyPositions.forEach(y -> {
                if (finalResenie.get() != null) {
                    return;
                }

                Position p = new Position(var.x, var.y + y + 1, var.direction);
                p.length = var.length - y - 1;
                var.intersecting.entrySet().stream().filter(stara -> stara.getKey() > y).forEach(orel -> p.intersecting.put(orel.getKey()-y-1, orel.getValue()));
                List<Position> newVariables =  variables.stream().map(Position::copy).collect(Collectors.toCollection(ArrayList::new));
                newVariables.forEach(presek -> {
                    Map<Integer, PositionIntersect> preseci = variables.stream().filter(orel -> orel.equals(presek)).findFirst().get().intersecting;
                    presek.intersecting = new HashMap<>();
                    preseci.forEach((key, value) -> {
                        Position intersectingPosition = value.position;
                        Position newIntersectingPosition = newVariables.stream().filter(news -> news.y == intersectingPosition.y && news.x == intersectingPosition.x && news.direction != presek.direction).findFirst().get();
                        presek.intersecting.put(key, new PositionIntersect(newIntersectingPosition, value.point));
                    });
                });
                newVariables.add(p);
                newVariables.stream().filter(stara -> stara.equals(var)).findFirst().get().length = y;
                newVariables.stream().filter(stara -> stara.equals(var)).findFirst().get().intersecting = newVariables.stream().filter(stara -> stara.equals(var)).findFirst().get().intersecting.entrySet().stream().filter(orel -> orel.getKey() < y).collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));;
                Map<Position, HashSet<Word>> novDomain = new HashMap<>(domains);
                novDomain.put(p, zboroviMapirani.get(p.length).values().stream().flatMap(orel -> orel.values().stream().flatMap(orel2 -> orel2.stream())).collect(Collectors.toCollection(HashSet::new)));
                novDomain.put(var, zboroviMapirani.get(y).values().stream().flatMap(orel -> orel.values().stream().flatMap(orel2 -> orel2.stream())).collect(Collectors.toCollection(HashSet::new)));

                AtomicInteger i = new AtomicInteger();
                var.intersecting.entrySet().forEach(orel -> {
                    if(orel.getKey() > y)
                    {
                        newVariables.stream().filter(stara -> stara.equals(orel.getValue().position)).findFirst().get().intersecting.put(orel.getValue().point, new PositionIntersect(p, orel.getKey() - y - 1));
                        i.getAndIncrement();
                    }
                    if(orel.getKey() == y)
                    {
                        //orel.getValue().position.intersecting.remove(var.y - orel.getValue().position.y- 1);
                        newVariables.stream().filter(stara -> stara.equals(orel.getValue().position)).findFirst().get().intersecting.remove(orel.getValue().point);
                    }
                });



                Position intersectingWord = var.intersecting.get(y).position;
//                                        Position p2 = new Position(intersectingWord.y + var.intersecting.get(y - 1).point, intersectingWord.x, 'v');
                Position p2 = new Position(var.x, var.y + y + 1, 'v');
                p2.length = intersectingWord.length - var.intersecting.get(y).point - 1;
                intersectingWord.intersecting.entrySet().stream().filter(stara -> stara.getKey() > var.intersecting.get(y).point).forEach(orel -> p2.intersecting.put(orel.getKey()-var.intersecting.get(y).point-1, orel.getValue()));
                newVariables.add(p2);
                newVariables.stream().filter(stara -> stara.equals(intersectingWord)).findFirst().get().length = var.intersecting.get(y).point;
                //newVariables.stream().filter(stara -> stara.equals(intersectingWord)).findFirst().get().intersecting =
                //        newVariables.stream()
                //                .filter(stara -> stara.equals(intersectingWord)).findFirst().orElseThrow(() -> new RuntimeException("Majka ti")).intersecting
                //                .entrySet().stream()
                //                .filter(orel -> orel.getKey() < var.intersecting.get(y).point)
                //                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));

                newVariables.stream().filter(stara -> stara.equals(intersectingWord)).findFirst().get().intersecting
                                .entrySet().removeIf(orel -> orel.getKey() >= var.intersecting.get(y).point);

                if (p2.length > 0) {
                    novDomain.put(p2, zboroviMapirani.get(p2.length).values().stream().flatMap(orel -> orel.values().stream().flatMap(orel2 -> orel2.stream())).collect(Collectors.toCollection(HashSet::new)));
                } else {
                    novDomain.put(p2, new HashSet<Word>());
                }

                if (var.intersecting.get(y).point != 0) {
                    novDomain.put(intersectingWord, zboroviMapirani.get(var.intersecting.get(y).point).values().stream().flatMap(orel -> orel.values().stream().flatMap(orel2 -> orel2.stream())).collect(Collectors.toCollection(HashSet::new)));
                } else {
                    novDomain.put(intersectingWord, new HashSet<Word>());
                }

                i.set(p2.y);
                int start = var.intersecting.get(y).point;
                var.intersecting.get(y).position.intersecting.entrySet().forEach(orel -> {
                    if(orel.getKey() > start)
                    {
                        //orel.getValue().position.intersecting.put(orel.getValue().point, new PositionIntersect(p2, orel.getKey() - start - 1));
                        newVariables.stream().filter(stara -> stara.equals(orel.getValue().position)).findFirst().get().intersecting.put(orel.getValue().point, new PositionIntersect(p2, orel.getKey() - start - 1));
                        i.getAndIncrement();
                    }
                    if(orel.getKey() == start)
                    {
                        newVariables.stream().filter(stara -> stara.equals(orel.getValue().position)).findFirst().get().intersecting.remove(orel.getValue().point);
                    }
                });


                CSP krstozbor = new CSP(newVariables, novDomain, zboroviMapirani);
                krstozbor.addConstraint(new WordLengthConstraint(newVariables));
                krstozbor.addConstraint(new AllDifferentConstraint(newVariables));
                krstozbor.addConstraint(new IntersectionConstraint(newVariables));
                krstozbor.addConstraint(new DifficultyConstraint(newVariables));
                //System.out.println(this);
                //System.out.println("===============");
                //System.out.println(krstozbor);
                //System.out.println("^^^^^^^^^^");

                Map<Position, Word> solution2 = new TreeMap<>();
                solution.entrySet().forEach(varSolution -> solution2.put(newVariables.stream().filter(stara -> stara.equals(varSolution.getKey())).findFirst().get(), varSolution.getValue()));
                Map<Position, Word> resenie = krstozbor.backtrack(solution2);

                if (resenie != null) {
                    System.out.println("TEMP RESHENIE");
                    finalResenie.set(resenie);
                }
            });
        }

        if (finalResenie.get() != null) {
            return finalResenie.get();
        } else {
            return null;
        }
    }

}
