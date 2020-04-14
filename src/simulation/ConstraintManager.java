package simulation;

import java.util.ArrayList;
import java.util.List;
import java.util.HashMap;
import java.util.Map;

/**
 * ConstraintManager manages constraints to be applied to a model when traversing its states.
 */
public class ConstraintManager {
    private Map<Integer, String> constraints;
    private int nextConstraintID;
    private final static String NUM_HEADER = "Num";
    private final static String CONSTRAINT_HEADER = "Constraint";

    public ConstraintManager() {
        constraints = new HashMap<>();
        nextConstraintID = 1;
    }

    public void addConstraint(String constraint) {
        constraints.put(nextConstraintID, constraint);
        nextConstraintID++;
    }

    public boolean removeConstraint(int constraintID) {
        return constraints.remove(constraintID) != null;
    }

    public void clearConstraints() {
        constraints.clear();
    }

    public List<String> getConstraints() {
        return new ArrayList<String>(constraints.values());
    }

    public String getFormattedConstraints() {
        StringBuilder sb = new StringBuilder();
        sb.append(String.format("\n%-8s%s\n", NUM_HEADER, CONSTRAINT_HEADER));
        for (int ID : constraints.keySet()) {
            sb.append(String.format("%-8d%s\n", ID, constraints.get(ID)));
        }
        return sb.toString();
    }
}
