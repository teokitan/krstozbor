import org.w3c.dom.ls.LSOutput;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

class Position implements Comparable<Position> {
    int x;
    int y;
    int length;
    char direction;
    Map<Integer, PositionIntersect> intersecting;

    public Position(int x, int y) {
        this.x = x;
        this.y = y;
        this.length = -1;
        this.intersecting = new HashMap<>();
    }

    public Position(int x, int y, char direction) {
        this.x = x;
        this.y = y;
        this.direction = direction;
        this.length = -1;
        this.intersecting = new HashMap<>();
    }

    public Position(int x, int y, int length, char direction) {
        this.x = x;
        this.y = y;
        this.direction = direction;
        this.length = length;
        this.intersecting = new HashMap<>();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Position position = (Position) o;
        return x == position.x && y == position.y && position.direction == direction;
    }

    @Override
    public int hashCode() {
        return Objects.hash(x, y);
    }

    @Override
    public int compareTo(Position o) {
        if (this.x - o.x == 0) {
            return this.y - o.y;
        }
        return this.x - o.x;
    }

    @Override
    public String toString() {
        return "Position{" +
                "x=" + x +
                ", y=" + y +
                ", length=" + length +
                ", direction=" + direction +
                '}';
    }

    public Position copy() {
        return new Position(this.x, this.y, this.length, this.direction);
    }
}

class CustomPositionSet {
    Set<Position> positions = new LinkedHashSet<>();
    public int countIksovi = Main.length + Main.width - 1;

    public CustomPositionSet() {
        positions.addAll(IntStream.range(1, Main.length).mapToObj(x -> new Position(x, 0, 'h')).toList());
        positions.addAll(IntStream.range(1, Main.width).mapToObj(x -> new Position(0, x, 'v')).toList());

//        for (int i = 0; i<5; i++) {
//            for (int j = 0; j<7; j++) {
//                positions.add(new Position(i,j));
//            }
//        }
    }

    public char[][] tabla() {
        char[][] tabla = new char[Main.length][Main.width];
        positions.forEach(x -> tabla[x.x][x.y] = 'X');
        return tabla;
    }

    public int iksovi() {
        int orelorel = 0;
        char[][] mat = tabla();

        for (int i = 0; i < Main.length; i++) {
            for (int j = 0; j < Main.width; j++) {
                if (mat[i][j] == 'X') orelorel++;
            }
        }

        return orelorel;
    }

    public boolean add(Position position) {
        if (positions.contains(position)) {
            return false;
        }

        if (position.x == 1) {
            Position toRemove = new Position(0, position.y);
            toRemove.direction = 'v';
            positions.remove(toRemove);
            countIksovi--;
        }


        if (position.y == 1) {
            Position toRemove = new Position(position.x, 0);
            toRemove.direction = 'h';
            positions.remove(toRemove);
            countIksovi--;
        }


        char[][] tabla = tabla();
        tabla[position.x][position.y] = 'X';
        for (int i = 0; i < Main.length - 1; i++) {
            for (int j = 0; j < Main.width - 1; j++) {
                // if (i < 5 && j < 7) continue;

                if (i == 0 && j == 0) {
                    continue;
                }
                if (tabla[i][j] == 'X' && (tabla[i][j + 1] == 'X' && tabla[i + 1][j] == 'X')) {
                    return false;
                }
            }
        }

        for (int i = 0; i < Main.length - 1; i++) {
            if (tabla[i][Main.width - 1] == 'X' && tabla[i + 1][Main.width - 1] == 'X') {
                return false;
            }
        }

        for (int i = 0; i < Main.width - 1; i++) {
            if (tabla[Main.length - 1][i] == 'X' && tabla[Main.length - 1][i + 1] == 'X') {
                return false;
            }
        }

        try {
            if (tabla[position.x][position.y + 1] != 'X') {
                countIksovi++;
            }
        } catch (Exception ex) {
        }

        try {
            if (tabla[position.x + 1][position.y] != 'X') {
                countIksovi++;
            }
        } catch (Exception ex) {
        }

        position.direction = 'h';
        positions.add(position);

        Position pos = new Position(position.x, position.y);
        pos.direction = 'v';
        positions.add(pos);

        return true;
    }
}

class Word implements Comparable<Word> {
    String word;
    int rarity;

    public Word(String word, int rarity) {
        this.word = word;
        this.rarity = rarity;
    }

    public Word(String word, String rarity) {
        this.word = word;
        this.rarity = Integer.valueOf(rarity);
    }

    public int compareTo(Word otherWord) {
        return this.word.compareTo(otherWord.word);
    }

    @Override
    public String toString() {
        return word;
    }
}


public class Main {
    public static final int length = 15;
    public static final int width = 15;
    public static final int iksoviCount = 120;
    //    public static Semaphore availableThreads = new Semaphore(15);
//    public static final ExecutorService executorService = Executors.newFixedThreadPool(15);
//    public static volatile boolean solutionFound = false;
    public static AtomicReference<Map<Position, Word>> finalResenie = new AtomicReference<>();
    public static TaskManager taskManager = new TaskManager();
    public static LocalDateTime startTime = LocalDateTime.now();


//    public static CompletionService<Map<Position, Word>> completionService = new ExecutorCompletionService<>(executorService);
//    public final static LinkedBlockingQueue<Callable<Map<Position, Word>>> taskQueue = new LinkedBlockingQueue<>();


    public static void main(String[] args) throws FileNotFoundException {
        SplittableRandom random = new SplittableRandom();
        String azbuka = "абвгдѓеѕжзијклљмнњопрстќуфхцчџш";

        BufferedReader br = new BufferedReader(new FileReader("allResults.txt"));
        List<Word> zborovi = new ArrayList<>(br.lines().map(x -> new Word(x.split(",")[1], x.split(",")[2])).toList());


//            String azbuka = "abcdefghijklmnopqrstuvwxyz";
        int maxDolzina = zborovi.stream().mapToInt(x -> x.word.length()).max().orElse(0);
        Map<Object, Long> dolzhina = zborovi.stream().collect(Collectors.groupingBy(x -> x.word.length(), Collectors.counting()));
        Map<Integer, Map<Character, Map<Integer, Set<Word>>>> zboroviMapirani = new TreeMap<>();
        zborovi.stream().map(x -> x.word.length()).forEach(x -> zboroviMapirani.putIfAbsent(x, new TreeMap<>()));
        zboroviMapirani.values().stream().forEach(x -> {
            azbuka.chars().mapToObj(y -> (char) y).forEach(y -> x.putIfAbsent(y, new TreeMap<>()));
        });
        zboroviMapirani.values().stream().flatMap(x -> x.values().stream()).forEach(x -> {
            IntStream.range(0, maxDolzina).forEach(y -> x.putIfAbsent(y, new TreeSet<>()));
        });

        zboroviMapirani.entrySet().forEach(x -> {
            Set<Word> filterZborovi = zborovi.stream().filter(zbor -> zbor.word.length() == x.getKey()).collect(Collectors.toSet());
            x.getValue().entrySet().forEach(y -> {
                Set<Word> filterFilterZborovi = filterZborovi.stream().filter(z -> z.word.contains(String.valueOf(y.getKey()))).collect(Collectors.toSet());
                y.getValue().entrySet().forEach(p -> p.setValue(filterFilterZborovi.stream().filter(zbor ->
                {
                    if (zbor.word.length() > p.getKey()) {
                        return zbor.word.charAt(p.getKey()) == y.getKey();
                    }
                    return false;
                }).collect(Collectors.toSet())).addAll(p.getValue()));
            });
        });
        Main.startTime = LocalDateTime.now();
        for (int ii = 0; ii <= 0; ii++) {
            while (true) {
                char[][] tabla = new char[length][width];

                CustomPositionSet customPositionSet = new CustomPositionSet();

                while (customPositionSet.positions.size() <= 115) {
                    customPositionSet.add(new Position(random.nextInt(1, length), random.nextInt(1, width)));
                }
//                System.out.println(customPositionSet.countIksovi);

                Map<Integer, Set<Position>> horizontalniZborovi = customPositionSet.positions.stream().filter(x -> x.direction == 'h').collect(Collectors.groupingBy(x -> x.x, TreeMap::new, Collectors.toCollection(TreeSet::new)));
                Map<Integer, Set<Position>> vertikalniZborovi = customPositionSet.positions.stream().filter(x -> x.direction == 'v').collect(Collectors.groupingBy(x -> x.y, TreeMap::new, Collectors.toCollection(TreeSet::new)));


                AtomicInteger prevCoordinate = new AtomicInteger();
                horizontalniZborovi.keySet().forEach(x -> {
                    List<Position> poziciiVoRed = horizontalniZborovi.get(x).stream().toList();
                    prevCoordinate.set(poziciiVoRed.get(0).y);
                    for (int i = 1; i < poziciiVoRed.size(); i++) {
                        Position p = poziciiVoRed.get(i - 1);
                        Position p1 = poziciiVoRed.get(i);
                        p.length = p1.y - p.y - 1;
                        prevCoordinate.set(p.y);
                    }
                    {
                        Position p = poziciiVoRed.get(poziciiVoRed.size() - 1);
                        p.length = width - 1 - p.y;
                        prevCoordinate.set(p.y);
                    }
                });

                vertikalniZborovi.keySet().forEach(x -> {
                    List<Position> poziciiVoRed = vertikalniZborovi.get(x).stream().toList();
                    Iterator<Position> iter = poziciiVoRed.iterator();
                    prevCoordinate.set(x);
                    Boolean newPos = false;
                    for (int i = 1; i < poziciiVoRed.size(); i++) {
                        Position p = poziciiVoRed.get(i - 1);
                        Position p1 = poziciiVoRed.get(i);
                        p.length = p1.x - p.x - 1;
                        prevCoordinate.set(p.x);
                    }
                    {
                        Position p = poziciiVoRed.getLast();
                        p.length = length - 1 - p.x;
                        prevCoordinate.set(p.y);
                    }


                    poziciiVoRed.forEach(pos -> {
                        try {
                            List<Position> intersecting = IntStream.range(pos.x + 1, pos.x + pos.length + 1)
                                    .mapToObj(horizontalniZborovi::get)
                                    .flatMap(Collection::stream)
                                    .filter(w -> w.y < pos.y && w.length + w.y >= pos.y)
                                    .toList();

                            pos.intersecting = intersecting.stream()
                                    .collect(Collectors.toMap(intsct -> intsct.x - pos.x - 1, intsct -> new PositionIntersect(intsct, pos.y - intsct.y - 1)));
                        } catch (Exception e) {
                            pos.intersecting = new HashMap<>();
                        }

                        // System.out.println(pos);
                    });
                });

                horizontalniZborovi.keySet().forEach(x -> {
                    List<Position> poziciiVoRed = horizontalniZborovi.get(x).stream().toList();

                    poziciiVoRed.forEach(pos -> {
                        try {
                            List<Position> intersecting = IntStream.range(pos.y + 1, pos.y + pos.length + 1)
                                    .mapToObj(vertikalniZborovi::get)
                                    .flatMap(Collection::stream)
                                    .filter(w -> w.x < pos.x && w.length + w.x >= pos.x)
                                    .toList();

                            pos.intersecting = intersecting.stream()
                                    .collect(Collectors.toMap(intsct -> intsct.y - pos.y - 1, intsct -> new PositionIntersect(intsct, pos.x - intsct.x - 1)));
                        } catch (Exception e) {
                            pos.intersecting = new HashMap<>();
                        }
                    });
                });

//        List<Position> listaPozicii = customPositionSet.positions.stream().filter(x -> x.length != 0).toList();
//                List<Position> listaPozicii = customPositionSet.positions.stream().filter(x -> x.length >= 4).toList();
                List<Position> listaPozicii = customPositionSet.positions.stream().filter(x -> x.length != 0).toList();

                Map<Position, HashSet<Word>> domain = listaPozicii.stream()
                        .collect(Collectors.toMap(
                                x -> x,
                                x -> (HashSet<Word>) zboroviMapirani.getOrDefault(x.length, new HashMap<>()).values().stream().flatMap(val -> val.values().stream().flatMap(Collection::stream)).distinct().collect(Collectors.toSet())));
//                                x -> (HashSet<String>) zboroviMapirani.getOrDefault(x.length, new HashMap<>()).values().stream().flatMap(val -> val.values().stream().flatMap(Collection::stream)).distinct().collect(Collectors.toSet())));

                CSP krstozbor = new CSP(listaPozicii, domain, zboroviMapirani);
                krstozbor.addConstraint(new WordLengthConstraint(listaPozicii));
                krstozbor.addConstraint(new AllDifferentConstraint(listaPozicii));
                krstozbor.addConstraint(new IntersectionConstraint(listaPozicii));
                krstozbor.addConstraint(new DifficultyConstraint(listaPozicii));
                char[][] orelorel = customPositionSet.tabla();
//                Map<Position, Word> resenie = krstozbor.backtrack(new TreeMap<>());

                Map<Position, Word> resenie = null;
                taskManager.addTask(krstozbor);
                taskManager.startResutlsHandling();
//                try {
//                    // Start initial tasks
//                    while (!taskQueue.isEmpty() && !solutionFound) {
//                        Callable<Map<Position, Word>> task = taskQueue.poll();
//                        if (task != null) {
//                            completionService.submit(task);
//                        }
//                    }
//
//                    while (!solutionFound) {
//                        Future<Map<Position, Word>> future = completionService.take(); // Blocks until a result is available
//                        Map<Position, Word> solution = future.get();
//                        if (solution != null) {
//                            solutionFound = true;
//                            finalResenie.set(solution);
//                            System.out.println("Solution found: " + solution);
//                            break;
//                        }
//                    }
//                } catch (InterruptedException | ExecutionException e) {
//                    e.printStackTrace();
//                } finally {
//                    executorService.shutdownNow(); // Shutdown all threads immediately
//                }
                ;
                tabla = customPositionSet.tabla();


                if (finalResenie.get() != null) {
                    System.out.println("Vreme main: " + Duration.between(Main.startTime, LocalDateTime.now()).toMillis());
                    for (Position pos : finalResenie.get().keySet()) {
                        if (pos.direction == 'h') {
                            for (int i = pos.y + 1; i <= pos.y + pos.length; i++) {
                                tabla[pos.x][i] = finalResenie.get().get(pos).word.charAt(i - pos.y - 1);
                            }
                        } else {
                            for (int i = pos.x + 1; i <= pos.x + pos.length; i++) {
                                tabla[i][pos.y] = finalResenie.get().get(pos).word.charAt(i - pos.x - 1);
                            }
                        }

                        tabla[pos.x][pos.y] = 'X';
                    }

                    for (int i = 0; i < length; i++) {
                        for (int j = 0; j < width; j++) {
                            System.out.print(tabla[i][j]);
                        }
                        System.out.println();
                    }
                    System.out.println(finalResenie.get());
                    break;
                } else System.out.println("nema");
//                break;
            }
            System.out.println("KRAJ FOR");

//            System.out.println("Milisekudni: " + milliseconds);
        }
        System.out.println("KRAJ MAIN");
    }
}