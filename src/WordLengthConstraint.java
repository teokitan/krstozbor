import java.util.List;
import java.util.Map;

public class WordLengthConstraint extends Constraint<Position, Word>{
    public WordLengthConstraint(List<Position> variables) {
        super(variables);
    }

    @Override
    public boolean checkConstraint(Map<Position, Word> solution) {
        return solution.entrySet().stream().allMatch(x -> x.getKey().length == x.getValue().word.length());
    }
}
