import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class AllDifferentConstraint extends Constraint<Position, Word>{
    public AllDifferentConstraint(List<Position> variables) {
        super(variables);
    }

    @Override
    public boolean checkConstraint(Map<Position, Word> solution) {
        Set<Word> checkSet = new HashSet<>(solution.values().stream().filter(x -> x.word.length() > 2).toList());
        return checkSet.size() == solution.values().stream().filter(x -> x.word.length() > 2).count();
    }
}
