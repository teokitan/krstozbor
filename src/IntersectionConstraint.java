import java.util.List;
import java.util.Map;

public class IntersectionConstraint extends Constraint<Position, String> {
    public IntersectionConstraint(List<Position> variables) {
        super(variables);
    }

    @Override
    public boolean checkConstraint(Map<Position, String> solution) {
        for (Position variable : variables) {
            String sol =  solution.get(variable);
            if(sol == null)
            {
                continue;
            }
            if(!variable.intersecting.entrySet().stream().allMatch(var -> {
                if (solution.get(var.getValue().position) == null) {
                    return true;
                }
                int pos1 = var.getKey();
                int pos2 = var.getValue().point;
                return sol.charAt(pos1) == solution.get(var.getValue().position).charAt(pos2);
            }))
            {
                return false;
            }
        }
        return true;
    }
}
