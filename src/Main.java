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

    public Position(int x, int y) {
        this.x = x;
        this.y = y;
        this.length = -1;
    }

    public Position(int x, int y, char direction) {
        this.x = x;
        this.y = y;
        this.direction = direction;
        this.length = -1;
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
        positions.addAll(IntStream.range(1, 19).mapToObj(x -> new Position(x, 0, 'h')).toList());
        positions.addAll(IntStream.range(1, 14).mapToObj(x -> new Position(0, x, 'v')).toList());
    }

    public char [][] tabla () {
        char [][] tabla = new char[19][14];
        positions.forEach(x -> tabla[x.x][x.y] = 'X');
        return tabla;
    }

    public boolean add(Position position)
    {

        if(position.x == 18 && position.y == 13)
        {
            return false;
        }

        char [][] tabla = tabla();
        tabla[position.x][position.y] = 'X';
        for(int i = 0; i < 18; i++)
        {
            for(int j = 0; j < 13; j++)
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

        for(int i = 0; i < 18; i++)
        {
            if(tabla[i][13] == 'X' && tabla[i + 1][13] == 'X')
            {
                return false;
            }
        }

        for(int i = 0; i < 13; i++)
        {
            if(tabla[18][i] == 'X' && tabla[18][i + 1] == 'X')
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
    public static void main(String[] args) throws FileNotFoundException {
        char [][] tabla = new char[19][14];


        CustomPositionSet customPositionSet = new CustomPositionSet();
        Random random = new Random();

        while(customPositionSet.positions.size() <= 66)
        {
            customPositionSet.add(new Position(random.nextInt(2, 19), random.nextInt(2, 14)));
        }
        customPositionSet.positions.forEach(x -> tabla[x.x][x.y] = 'X');

        for(int i = 0; i < 19; i++)
        {
            for (int j = 0; j < 14; j++)
            {
                System.out.print(tabla[i][j] + " ");
            }
            System.out.println();
        }

        BufferedReader br = new BufferedReader(new FileReader(".\\recnik.csv"));
        List<String> zborovi = br.lines().map(x -> x.split(",")[1]).toList();

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
                    if(zbor.length() > p.getKey())
                    {
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
            Iterator<Position> iter = poziciiVoRed.iterator();
            prevCoordinate.set(poziciiVoRed.get(0).y);
            for(int i = 1; i < poziciiVoRed.size(); i++)
            {
                Position p = poziciiVoRed.get(i - 1);
                Position p1 = poziciiVoRed.get(i);
                p.length = p1.y - p.y - 1;
                prevCoordinate.set(p.y);
            }
            {
                Position p = poziciiVoRed.get(poziciiVoRed.size() - 1);
                p.length = 13 - p.y;
                prevCoordinate.set(p.y);
            }
        });

        vertikalniZborovi.keySet().forEach(x -> {
            List<Position> poziciiVoRed = vertikalniZborovi.get(x).stream().toList();
            Iterator<Position> iter = poziciiVoRed.iterator();
            prevCoordinate.set(x);
            Boolean newPos = false;
            for(int i = 1; i < poziciiVoRed.size(); i++)
            {
                Position p = poziciiVoRed.get(i - 1);
                Position p1 = poziciiVoRed.get(i);
                p.length = p1.x - p.x - 1;
                prevCoordinate.set(p.x);
            }
            {
                Position p = poziciiVoRed.get(poziciiVoRed.size() - 1);
                p.length = 18 - p.x;
                prevCoordinate.set(p.y);
            }
        });


        System.out.println(zboroviMapirani.get(6).get('б').get(0));
        System.out.println(zboroviMapirani.get(6).get('а').get(1));
        Set<String> filtrirani = new HashSet<>(zboroviMapirani.get(6).get('б').get(0));
        filtrirani.retainAll(zboroviMapirani.get(6).get('а').get(1));
        System.out.println(filtrirani);
//        Map<Integer, Map<Character, Map<Integer, Set<String>>>>
        Map<Position, List<String>> domain = customPositionSet.positions.stream()
                .collect(Collectors.toMap(
                        x -> x,
                        x -> zboroviMapirani.get(x.length).values().stream().flatMap(val -> val.values().stream().flatMap(Collection::stream)).collect(Collectors.toList())));
        CSP<Position, String> krstozbor = new CSP<>(customPositionSet.positions.stream().toList(), domain);

    }
}