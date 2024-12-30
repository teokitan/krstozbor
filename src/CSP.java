import java.util.*;
import java.util.concurrent.*;
import java.util.stream.Collectors;

public class CSP implements Callable<Map<Position, Word>> {
    private List<Position> variables;
    private Map<Position, HashSet<Word>> domains;
    private Map<Position, List<Constraint<Position, Word>>> constraints;
    private Map<Integer, Map<Character, Map<Integer, Set<Word>>>> zboroviMapirani;

    public static final int wordSplitCount = 90;

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

        if (variables.stream().filter(orel -> orel.length >= 1).count() < wordSplitCount) {
            boolean isVertical = var.direction == 'v';

            emptyPositions = var.intersecting.entrySet().stream()
                    .filter(entry -> entry.getKey() > 0 && entry.getKey() < var.length - 1 && !solution.containsKey(entry.getValue().position))
                    .map(Map.Entry::getKey)
                    .sorted((key1, key2) -> {
                        int distanceToMiddle = Math.abs(var.length / 2);
                        return Integer.compare(Math.abs(key1 - distanceToMiddle), Math.abs(key2 - distanceToMiddle));
                    }).toList();

            emptyPositions.forEach(positionKey -> {
                if (Main.finalResenie.get() != null) {
                    return;
                }

                Position p = isVertical
                        ? new Position(var.x + positionKey + 1, var.y, var.direction)
                        : new Position(var.x, var.y + positionKey + 1, var.direction);
                p.length = var.length - positionKey - 1;

                var.intersecting.entrySet().stream()
                        .filter(entry -> entry.getKey() > positionKey)
                        .forEach(entry -> p.intersecting.put(entry.getKey() - positionKey - 1, entry.getValue()));

                List<Position> newVariables = variables.stream().map(Position::copy).collect(Collectors.toCollection(ArrayList::new));
                newVariables.forEach(presek -> {
                    Map<Integer, PositionIntersect> preseci = variables.stream()
                            .filter(orel -> orel.equals(presek))
                            .findFirst()
                            .get().intersecting;

                    presek.intersecting = new HashMap<>();
                    preseci.forEach((key, value) -> {
                        Position intersectingPosition = value.position;
                        Position newIntersectingPosition = newVariables.stream()
                                .filter(news -> (isVertical
                                        ? (news.x == intersectingPosition.x && news.y == intersectingPosition.y)
                                        : (news.y == intersectingPosition.y && news.x == intersectingPosition.x))
                                        && news.direction != presek.direction)
                                .findFirst()
                                .get();
                        presek.intersecting.put(key, new PositionIntersect(newIntersectingPosition, value.point));
                    });
                });

                newVariables.add(p);
                Position currentVar = newVariables.stream().filter(stara -> stara.equals(var)).findFirst().get();
                currentVar.length = positionKey;
                currentVar.intersecting = currentVar.intersecting.entrySet().stream()
                        .filter(entry -> entry.getKey() < positionKey)
                        .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));

                Map<Position, HashSet<Word>> novDomain = new HashMap<>(domains);
                novDomain.put(p, zboroviMapirani.get(p.length).values().stream()
                        .flatMap(map -> map.values().stream().flatMap(Collection::stream))
                        .collect(Collectors.toCollection(HashSet::new)));
                novDomain.put(currentVar, zboroviMapirani.get(positionKey).values().stream()
                        .flatMap(map -> map.values().stream().flatMap(Collection::stream))
                        .collect(Collectors.toCollection(HashSet::new)));

                Position intersectingWord = var.intersecting.get(positionKey).position;
                Position p2 = isVertical
                        ? new Position(var.x + positionKey + 1, var.y, 'h')
                        : new Position(var.x, var.y + positionKey + 1, 'v');
                p2.length = intersectingWord.length - var.intersecting.get(positionKey).point - 1;

                intersectingWord.intersecting.entrySet().stream()
                        .filter(entry -> entry.getKey() > var.intersecting.get(positionKey).point)
                        .forEach(entry -> p2.intersecting.put(entry.getKey() - var.intersecting.get(positionKey).point - 1, entry.getValue()));

                newVariables.add(p2);
                Position updatedIntersectingWord = newVariables.stream().filter(stara -> stara.equals(intersectingWord)).findFirst().get();
                updatedIntersectingWord.length = var.intersecting.get(positionKey).point;
                updatedIntersectingWord.intersecting.entrySet().removeIf(entry -> entry.getKey() >= var.intersecting.get(positionKey).point);

                if (p2.length > 0) {
                    novDomain.put(p2, zboroviMapirani.get(p2.length).values().stream()
                            .flatMap(map -> map.values().stream().flatMap(Collection::stream))
                            .collect(Collectors.toCollection(HashSet::new)));
                } else {
                    novDomain.put(p2, new HashSet<>());
                }

                if (var.intersecting.get(positionKey).point != 0) {
                    novDomain.put(intersectingWord, zboroviMapirani.get(var.intersecting.get(positionKey).point).values().stream()
                            .flatMap(map -> map.values().stream().flatMap(Collection::stream))
                            .collect(Collectors.toCollection(HashSet::new)));
                } else {
                    novDomain.put(intersectingWord, new HashSet<>());
                }

                CSP krstozbor = new CSP(newVariables, novDomain, zboroviMapirani);
                krstozbor.addConstraint(new WordLengthConstraint(newVariables));
                krstozbor.addConstraint(new AllDifferentConstraint(newVariables));
                krstozbor.addConstraint(new IntersectionConstraint(newVariables));
                krstozbor.addConstraint(new DifficultyConstraint(newVariables));

                Map<Position, Word> solution2 = new TreeMap<>();
                solution.entrySet().forEach(varSolution -> solution2.put(
                        newVariables.stream().filter(stara -> stara.equals(varSolution.getKey())).findFirst().get(),
                        varSolution.getValue()));

                if (Main.finalResenie.get() == null) {
                    Main.taskManager.addTask(krstozbor);
                }
            });
        }

        return null;
    }

    @Override
    public Map<Position, Word> call() throws Exception {
        Map<Position, Word> res = backtrack(new HashMap<>());
        return res;
    }
}
