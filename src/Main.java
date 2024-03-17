import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
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
        if(this.x - o.x == 0)
        {
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
}

class CustomPositionSet {
    Set<Position> positions = new HashSet<>();

    public CustomPositionSet() {
        positions.addAll(IntStream.range(1, Main.length).mapToObj(x -> new Position(x, 0, 'h')).toList());
        positions.addAll(IntStream.range(1, Main.width).mapToObj(x -> new Position(0, x, 'v')).toList());
    }

    public char [][] tabla () {
        char [][] tabla = new char[Main.length][Main.width];
        positions.forEach(x -> tabla[x.x][x.y] = 'X');
        return tabla;
    }

    public boolean add(Position position)
    {

        if(position.x == Main.length-1 && position.y == Main.width-1)
        {
            return false;
        }

        if(position.x == 1)
        {
            Position toRemove = new Position(0, position.y);
            toRemove.direction = 'v';
            positions.remove(toRemove);
        }


        if(position.y == 1)
        {
            Position toRemove = new Position(position.x, 0);
            toRemove.direction = 'h';
            positions.remove(toRemove);
        }


        char [][] tabla = tabla();
        tabla[position.x][position.y] = 'X';
        for(int i = 0; i < Main.length-1; i++)
        {
            for(int j = 0; j < Main.width-1; j++)
            {
                if(i == 0 && j == 0)
                {
                    continue;
                }
                if(tabla[i][j] == 'X' && (tabla[i][j + 1] == 'X' && tabla[i + 1][j] == 'X'))
                {
                    return false;
                }
            }
        }

        for(int i = 0; i < Main.length-1; i++)
        {
            if(tabla[i][Main.width-1] == 'X' && tabla[i + 1][Main.width-1] == 'X')
            {
                return false;
            }
        }

        for(int i = 0; i < Main.width-1; i++)
        {
            if(tabla[Main.length-1][i] == 'X' && tabla[Main.length-1][i + 1] == 'X')
            {
                return false;
            }
        }

        try
        {
            if(tabla[position.x][position.y + 1] != 'X')
            {
                position.direction = 'h';
                positions.add(position);
            }
        }
        catch (Exception ex) {}

        try
        {
            if(tabla[position.x + 1][position.y] != 'X')
            {
                Position pos = new Position(position.x, position.y);
                pos.direction = 'v';
                positions.add(pos);
            }
        }
        catch (Exception ex) {}

        return true;
    }
}


public class Main {
    public static final int length = 14;
    public static final int width = 13;

    public static void main(String[] args) throws FileNotFoundException {
        while (true) {
            char[][] tabla = new char[length][width];


            CustomPositionSet customPositionSet = new CustomPositionSet();
            Random random = new Random();

            while (customPositionSet.positions.size() <= 80) {
                customPositionSet.add(new Position(random.nextInt(1, length), random.nextInt(1, width)));
            }
            customPositionSet.positions.forEach(x -> tabla[x.x][x.y] = 'X');

            for (int i = 0; i < length; i++) {
                for (int j = 0; j < width; j++) {
                    System.out.print(tabla[i][j] + " ");
                }
                System.out.println();
            }

            BufferedReader br = new BufferedReader(new FileReader("recnik.csv"));
            List<String> zborovi = br.lines().map(x -> x.split(",")[1]).map(String::toLowerCase).filter(x ->
            {
                for (int i = 0; i < x.length(); i++) {
                    if (!Character.isLetter(x.charAt(i))) {
                        return false;
                    }
                }
                return true;
            }).toList();

            String azbuka = "абвгдѓеѕжзијклљмнњопрстќуфхцчџш";
            int maxDolzina = zborovi.stream().mapToInt(String::length).max().orElse(0);
            Map<Integer, Map<Character, Map<Integer, Set<String>>>> zboroviMapirani = new TreeMap<>();
            zborovi.stream().map(String::length).forEach(x -> zboroviMapirani.putIfAbsent(x, new TreeMap<>()));
            zboroviMapirani.values().stream().forEach(x -> {
                azbuka.chars().mapToObj(y -> (char) y).forEach(y -> x.putIfAbsent(y, new TreeMap<>()));
            });
            zboroviMapirani.values().stream().flatMap(x -> x.values().stream()).forEach(x -> {
                IntStream.range(0, maxDolzina).forEach(y -> x.putIfAbsent(y, new TreeSet<>()));
            });

            zboroviMapirani.entrySet().forEach(x -> {
                Set<String> filterZborovi = zborovi.stream().filter(zbor -> zbor.length() == x.getKey()).collect(Collectors.toSet());
                x.getValue().entrySet().forEach(y -> {
                    Set<String> filterFilterZborovi = filterZborovi.stream().filter(z -> z.contains(String.valueOf(y.getKey()))).collect(Collectors.toSet());
                    y.getValue().entrySet().forEach(p -> p.setValue(filterFilterZborovi.stream().filter(zbor ->
                    {
                        if (zbor.length() > p.getKey()) {
                            return zbor.charAt(p.getKey()) == y.getKey();
                        }
                        return false;
                    }).collect(Collectors.toSet())).addAll(p.getValue()));
                });
            });

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
                    Position p = poziciiVoRed.get(poziciiVoRed.size() - 1);
                    p.length = length - 1 - p.x;
                    prevCoordinate.set(p.y);
                }

                poziciiVoRed.forEach(pos -> {
                            List<Position> intersecting = IntStream.range(pos.x + 1, pos.x + pos.length + 1)
                                    .mapToObj(horizontalniZborovi::get).flatMap(Collection::stream).toList();
                            System.out.println();
                        });

                poziciiVoRed.forEach(pos -> {
                    List<Position> intersecting = IntStream.range(pos.x + 1, pos.x + pos.length + 1)
                            .mapToObj(horizontalniZborovi::get)
                            .flatMap(Collection::stream)
                            .filter(w -> w.y < pos.y && w.length + w.y >= pos.y)
                            .toList();

                    pos.intersecting = intersecting.stream()
                            .collect(Collectors.toMap(intsct -> intsct.x - pos.x - 1, intsct -> new PositionIntersect(intsct, pos.y - intsct.y - 1)));

                    System.out.println(pos);
                });
            });

            horizontalniZborovi.keySet().forEach(x -> {
                List<Position> poziciiVoRed = horizontalniZborovi.get(x).stream().toList();

                poziciiVoRed.forEach(pos -> {
                    List<Position> intersecting = IntStream.range(pos.y + 1, pos.y + pos.length + 1)
                            .mapToObj(vertikalniZborovi::get)
                            .flatMap(Collection::stream)
                            .filter(w -> w.x < pos.x && w.length + w.x >= pos.x)
                            .toList();

                    pos.intersecting = intersecting.stream()
                            .collect(Collectors.toMap(intsct -> intsct.y - pos.y - 1, intsct -> new PositionIntersect(intsct, pos.x - intsct.x - 1)));
                });
            });


            System.out.println(zboroviMapirani.get(6).get('б').get(0));
            System.out.println(zboroviMapirani.get(6).get('а').get(1));
            Set<String> filtrirani = new HashSet<>(zboroviMapirani.get(6).get('б').get(0));
            filtrirani.retainAll(zboroviMapirani.get(6).get('а').get(1));
//        List<Position> listaPozicii = customPositionSet.positions.stream().filter(x -> x.length != 0).toList();
            List<Position> listaPozicii = customPositionSet.positions.stream().filter(x -> x.length >= 4).toList();
            System.out.println(filtrirani);
            Map<Position, List<String>> domain = listaPozicii.stream()
                    .collect(Collectors.toMap(
                            x -> x,
                            x -> zboroviMapirani.getOrDefault(x.length, new HashMap<>()).values().stream().flatMap(val -> val.values().stream().flatMap(Collection::stream)).distinct().collect(Collectors.toList())));
            CSP<Position, String> krstozbor = new CSP<>(listaPozicii, domain, zboroviMapirani);
            krstozbor.addConstraint(new WordLengthConstraint(listaPozicii));
            krstozbor.addConstraint(new AllDifferentConstraint(listaPozicii));
            krstozbor.addConstraint(new IntersectionConstraint(listaPozicii));
            Map<Position, String> resenie = krstozbor.backtrack(new TreeMap<>());

            System.out.println(resenie);
        }
    }
}