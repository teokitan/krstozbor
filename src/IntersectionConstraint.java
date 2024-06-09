import java.util.List;
import java.util.Map;

public class IntersectionConstraint extends Constraint<Position, Word> {
    public IntersectionConstraint(List<Position> variables) {
        super(variables);
    }

    @Override
    public boolean checkConstraint(Map<Position, Word> solution) {
        for (Position variable : solution.keySet()) {
            Word sol =  solution.get(variable);
            if(sol == null)
            {
                continue;
            }
            if(!variable.intersecting.entrySet().stream().allMatch(var -> {
                if(var.getValue().position.length < 4)
                {
                    return true;
                }
                if (solution.get(var.getValue().position) == null) {
                    return true;
                }
                int pos1 = var.getKey();
                int pos2 = var.getValue().point;

                if (pos1 >= sol.word.length()) {
                    System.out.println();
                }

                if (pos2 >= solution.get(var.getValue().position).word.length()) {
                    System.out.println();
                }

                return sol.word.charAt(pos1) == solution.get(var.getValue().position).word.charAt(pos2);
            }))
            {
                return false;
            }
        }
        return true;
    }
}
