import java.util.List;
import java.util.Map;

public class DifficultyConstraint extends Constraint<Position, Word>{
    private final double MAX_PERCENTAGE_DIFFICULT = 0.3;

    public DifficultyConstraint(List<Position> variables) {
        super(variables);
    }

    @Override
    public boolean checkConstraint(Map<Position, Word> solution) {
        return ((double) solution.values().stream().filter(x -> x.rarity == 1).count() / variables.stream().filter(orel -> orel.length != 0).count()) <= MAX_PERCENTAGE_DIFFICULT;
    }
}
