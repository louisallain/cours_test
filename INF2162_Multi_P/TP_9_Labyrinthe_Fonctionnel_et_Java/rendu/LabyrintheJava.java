import java.lang.reflect.Array;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class LabyrintheJava {

    private List<List<Integer>> init;
    public final static Integer[] coordsDebut = {0, 1};
    public static final String ANSI_RED_BACKGROUND = "\u001B[41m";
    public static final String ANSI_CYAN_BACKGROUND = "\u001B[46m";
    public static final String ANSI_YELLOW_BACKGROUND = "\u001B[43m";
    public static final String ANSI_RESET = "\u001B[0m";
    public static final String ANSI_BLACK_BACKGROUND = "\u001B[40m";

    public LabyrintheJava(List<List<Integer>> init_) {
        this.init = init_;
    }

    @Override
    public String toString() {

        String ret = "";

        for(List<Integer> ligne : this.init) {

            for(Integer cCase : ligne) {
                switch (cCase) {
                    case 1: ret = ret + ANSI_YELLOW_BACKGROUND + "   " + ANSI_RESET;
                        break;
                    case 2: ret = ret + ANSI_CYAN_BACKGROUND + "   " + ANSI_RESET;
                        break;
                    case 9: ret = ret + ANSI_RED_BACKGROUND + "   " + ANSI_RESET;
                        break;
                    default: ret = ret + ANSI_BLACK_BACKGROUND + "   " + ANSI_RESET;
                        break;
                }
            }
            ret = ret + "\n";
        }

        return ret;
    }

    public Set<List<List<Integer>>> cheminSortieAPartirDe(Integer[] coords_) {
        return this.cheminSortieAPartirDeUtils(coords_, this.init);
    }

    private Set<List<List<Integer>>> cheminSortieAPartirDeUtils(Integer[] coords_, List<List<Integer>> lab_) {

        Set<List<List<Integer>>> ret = new HashSet<>();
        int x = coords_[0], y = coords_[1];

        switch(lab_.get(x).get(y)) {

            case 0:
                Set<Integer[]> voisins =  Stream.of(new Integer[]{x-1,y}, new Integer[]{x+1,y}, new Integer[]{x,y-1}, new Integer[]{x,y+1})
                        .filter(c -> c[0]>=0 && c[0]<lab_.size() && c[1]>=0 && c[1]<lab_.get(c[0]).size())
                        .collect(Collectors.toSet());
                List<List<Integer>> labMarque = new ArrayList<>(lab_);
                List<Integer> ligneMarque = labMarque.get(x);
                ligneMarque.set(y, 2);
                labMarque.set(x, ligneMarque);

                ret = voisins.stream().map(ps -> this.cheminSortieAPartirDeUtils(ps, labMarque)).flatMap(s -> s.stream()).collect(Collectors.toSet());
                break;
            case 9:
                ret.add(lab_);
                break;
        }
        return ret;
    }


    public static void toStringTest() {
        List<List<Integer>> labTab1 = new ArrayList<>();
        List<Integer> tmp = Arrays.asList(1, 0, 1, 1, 1, 1, 1, 1, 1, 1, 1);

        labTab1.add(Arrays.asList(1, 0, 1, 1, 1, 1, 1, 1, 1, 1, 1));
        labTab1.add(Arrays.asList(1, 0, 1, 0, 0, 0, 0, 0, 0, 0, 1));
        labTab1.add(Arrays.asList(1, 0, 1, 0, 1, 0, 1, 1, 1, 0, 1));
        labTab1.add(Arrays.asList(1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1));
        labTab1.add(Arrays.asList(1, 1, 1, 1, 1, 0, 1, 1, 1, 0, 1));
        labTab1.add(Arrays.asList(1, 0, 0, 0, 1, 0, 0, 0, 1, 0, 1));
        labTab1.add(Arrays.asList(1, 0, 1, 0, 0, 0, 1, 0, 0, 0, 1));
        labTab1.add(Arrays.asList(1, 9, 1, 1, 1, 1, 1, 1, 1, 1, 1));

        LabyrintheJava laby = new LabyrintheJava(labTab1);
        System.out.println(laby);
    }

    public static void cheminTest() {
        List<List<Integer>> labTab1 = new ArrayList<>();
        List<Integer> tmp = Arrays.asList(1, 0, 1, 1, 1, 1, 1, 1, 1, 1, 1);

        labTab1.add(Arrays.asList(1, 0, 1, 1, 1, 1, 1, 1, 1, 1, 1));
        labTab1.add(Arrays.asList(1, 0, 1, 0, 0, 0, 0, 0, 0, 0, 1));
        labTab1.add(Arrays.asList(1, 0, 1, 0, 1, 0, 1, 1, 1, 0, 1));
        labTab1.add(Arrays.asList(1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1));
        labTab1.add(Arrays.asList(1, 1, 1, 1, 1, 0, 1, 1, 1, 0, 1));
        labTab1.add(Arrays.asList(1, 0, 0, 0, 1, 0, 0, 0, 1, 0, 1));
        labTab1.add(Arrays.asList(1, 0, 1, 0, 0, 0, 1, 0, 0, 0, 1));
        labTab1.add(Arrays.asList(1, 9, 1, 1, 1, 1, 1, 1, 1, 1, 1));

        LabyrintheJava laby = new LabyrintheJava(labTab1);
        System.out.println(laby);
        laby.cheminSortieAPartirDe(LabyrintheJava.coordsDebut).forEach(s -> System.out.println(new LabyrintheJava(s)));
    }

    public static void main(String[] args) {

        // LabyrintheJava.toStringTest();
        LabyrintheJava.cheminTest();

    }
}
