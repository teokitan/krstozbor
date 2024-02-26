import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

class Position {
    int x;
    int y;

    public Position(int x, int y) {
        this.x = x;
        this.y = y;
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Position position = (Position) o;
        return x == position.x && y == position.y;
    }

    @Override
    public int hashCode() {
        return Objects.hash(x, y);
    }
}

class CustomPositionSet {
    Set<Position> positions = new HashSet<>();

    public CustomPositionSet() {
        positions.addAll(IntStream.range(0, 19).mapToObj(x -> new Position(x, 0)).toList());
        positions.addAll(IntStream.range(0, 14).mapToObj(x -> new Position(0, x)).toList());
    }

    public boolean add(Position position) {
        if(positions.stream().noneMatch(x -> x.x == position.x && x.y == position.y + 1)  ||
                positions.stream().noneMatch(x -> x.x == position.x + 1 && x.y == position.y)) {
            if (position.x == 18) {
                if (positions.stream().noneMatch(x -> x.x == position.x && x.y == position.y - 1) &&
                        positions.stream().noneMatch(x -> x.x == position.x && x.y == position.y + 1)) {
                    return positions.add(position);
                }
                return false;
            }
            if (position.y == 13) {
                if (positions.stream().noneMatch(x -> x.x == position.x - 1 && x.y == position.y ) &&
                        positions.stream().noneMatch(x -> x.x == position.x + 1 && x.y == position.y)) {
                    return positions.add(position);
                }
                return false;
            }
            return positions.add(position);
        }
        return false;
    }
}


public class Main {
    public static void main(String[] args) {
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

        List<String> zborovi = List.of(
                "дом",   // dom (house)
                "животно",  // zhivotno (animal)
                "цвет", // cvet (color)
                "планина",  // planina (mountain)
                "сонце",  // sonce (sun)
                "месец",  // mesec (moon)
                "вода",  // voda (water)
                "оган",  // ogan (fire)
                "земја",  // zemja (earth)
                "ветер",  // veter (wind)
                "дърво",  // darvo (tree)
                "цвете",  // cvete (flower)
                "град",  // grad (city)
                "село",  // selo (village)
                "птица",  // ptica (bird)
                "риба",  // riba (fish)
                "плод",  // plod (fruit)
                "зеленчук",  // zelenchuk (vegetable)
                "живот",  // zhivot (life)
                "смрт",  // smrt (death)
                "љубов",  // ljubov (love)
                "среќа",  // srekja (happiness)
                "тага",  // taga (sadness)
                "смех",  // smeh (laughter)
                "солза",  // solza (tear)
                "небо",  // nebo (sky)
                "звезда",  // zvezda (star)
                "планета",  // planeta (planet)
                "универзум",  // univerzum (universe)
                "галаксија",  // galaksija (galaxy)
                "млечен пат",  // mlechen pat (Milky Way)
                "сателит",  // satelit (satellite)
                "комета",  // kometa (comet)
                "астронаут",  // astronaut (astronaut)
                "луѓе",  // lugje (people)
                "дете",  // dete (child)
                "родители",  // roditeli (parents)
                "брат",  // brat (brother)
                "сестра",  // sestra (sister)
                "маж",  // mazh (husband)
                "жена",  // zhena (wife)
                "мама",  // mama (mom)
                "татко",  // tatko (dad)
                "баба",  // baba (grandmother)
                "дедо",  // dedo (grandfather)
                "син",  // sin (son)
                "ќерка",  // kjerka (daughter)
                "бебе",  // bebe (baby)
                "пријател",  // prijatel (friend)
                "непријател",  // neprijatel (enemy)
                "радост",  // radost (joy)
                "печал",  // pechal (sorrow)
                "гордост",  // gordost (pride)
                "срам",  // sram (shame)
                "страв",  // strav (fear)
                "надеж",  // nadezh (hope)
                "вера",  // vera (faith)
                "двоместо",  // dvomesto (chair)
                "маса",  // masa (table)
                "соба",  // soba (room)
                "кујна",  // kujna (kitchen)
                "спална соба",  // spalna soba (bedroom)
                "бања",  // banja (bathroom)
                "дневна соба",  // dnevna soba (living room)
                "тераса",  // terasa (terrace)
                "балкон",  // balkon (balcony)
                "врати",  // vrat (door)
                "прозорци",  // prozorci (windows)
                "под",  // pod (floor)
                "таван",  // tavan (attic)
                "спрат",  // sprat (storey)
                "степеници",  // stepenici (stairs)
                "рампа",  // rampa (ramp)
                "порти",  // porti (gate)
                "ограда",  // ograda (fence)
                "сад",  // sad (garden)
                "бараба",  // baraba (drum)
                "траба",  // traba (trumpet)
                "цигулка",  // cigulka (violin)
                "китара",  // kitara (guitar)
                "клавир",  // klavir (piano)
                "флејта",  // flejta (flute)
                "саксофон",  // saksofon (saxophone)
                "бубњар",  // bubnjar (drummer)
                "пејач",  // pejach (singer)
                "глумец",  // glumec (actor)
                "глумица",  // glumica (actress)
                "режисер",  // reziser (director)
                "сценарист",  // scenarist (screenwriter)
                "камера",  // kamera (camera)
                "микрофон",  // mikrofon (microphone)
                "светло",  // svetlo (light)
                "звук",  // zvuk (sound)
                "боја",  // boja (paint)
                "кист",  // kist (brush)
                "платно",  // platno (canvas)
                "слика",  // slika (picture)
                "уметник",  // umetnik (artist)
                "галерија"  // galerija (gallery)
        );

        String azbuka = "абвгдѓеѕжзијклљмнњопрстќуфхцчџш";
        Integer maxDolzina = zborovi.stream().mapToInt(x -> x.length()).max().getAsInt();
        Map<Integer, Map<Character, Map<Integer, Set<String>>>> zboroviMapirani = new TreeMap<>();
        zborovi.stream().map(String::length).forEach(x -> zboroviMapirani.putIfAbsent(x, new TreeMap<>()));
        zboroviMapirani.values().stream().forEach(x -> {
            azbuka.chars().mapToObj(y -> (char) y).forEach(y -> x.putIfAbsent(y, new TreeMap<>()));
        });
        zboroviMapirani.values().stream().flatMap(x -> x.values().stream()).forEach(x -> {
            IntStream.range(0, maxDolzina).forEach(y -> x.putIfAbsent(y, new TreeSet<>()));
        });

        zboroviMapirani.entrySet().stream().forEach(x -> {
            Set<String> filterZborovi = zborovi.stream().filter(zbor -> zbor.length() == x.getKey()).collect(Collectors.toSet());
            x.getValue().entrySet().stream().forEach(y -> {
                Set<String> filterFilterZborovi = filterZborovi.stream().filter(z -> z.contains(String.valueOf(y.getKey()))).collect(Collectors.toSet());
                y.getValue().entrySet().stream().forEach(p -> p.setValue(filterFilterZborovi.stream().filter(zbor ->
                {
                    if(zbor.length() > p.getKey())
                    {
                        return zbor.charAt(p.getKey()) == y.getKey();
                    }
                    return false;
                }).collect(Collectors.toSet())).addAll(p.getValue()));
            });
        });

        System.out.println(zboroviMapirani.get(6).get('б').get(0));
        System.out.println(zboroviMapirani.get(6).get('а').get(1));
        Set<String> filtrirani = new HashSet<>(zboroviMapirani.get(6).get('б').get(0));
        filtrirani.retainAll(zboroviMapirani.get(6).get('а').get(1));
        System.out.println(filtrirani);
        System.out.println("orel");
    }
}