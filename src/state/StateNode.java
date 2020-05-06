package state;

import alloy.AlloyConstants;
import alloy.AlloyUtils;
import alloy.ParsingConf;
import alloy.SigData;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * StateNode represents a single execution state of an Alloy transition system.
 */
public class StateNode {
    private List<StateNode> steps; // outgoing edges (states that can be stepped to from this state)
    private Map<String, List<String>> state; // the state that this node represents
    private int id;
    private ParsingConf parsingConf;
    private SigData sigData;

    public StateNode(SigData data, ParsingConf conf) {
        sigData = data;
        parsingConf = conf;
        steps = new ArrayList<>();
        state = new HashMap<>();

        for (String field : sigData.getFields()) {
            state.put(field, new ArrayList<>());
        }
    }

    public void addValueToField(String field, String value) {
        if (!state.containsKey(field)) {
            return;
        }

        // Ensure values are sorted upon insertion. For the following reasons:
        // 1. For user display.
        // 2. Enables comparison between StateNodes.
        List<String> valuesForField = state.get(field);
        for (int i = 0; i < valuesForField.size(); i++) {
            if (value.compareTo(valuesForField.get(i)) > 0) {
                continue;
            }

            valuesForField.add(i, value);
            return;
        }

        valuesForField.add(value);
    }

    public List<StateNode> getSteps() {
        return steps;
    }

    public void addStep(StateNode node) {
        steps.add(node);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        for (String key : state.keySet()) {
            sb.append(String.format("\n%s: %s ", key, AlloyConstants.BLOCK_INITIALIZER));
            sb.append(String.format("%s %s", String.join(", ", state.get(key)), AlloyConstants.BLOCK_TERMINATOR));
        }
        sb.append("\n");
        return sb.toString();
    }

    public String stringForProperty(String property) {
        if (!state.containsKey(property)) {
            return "Property not found.";
        }

        return String.format(
            "\n%s %s %s\n",
            AlloyConstants.BLOCK_INITIALIZER,
            String.join(", ", state.get(property)),
            AlloyConstants.BLOCK_TERMINATOR
        );
    }

    /**
     * @param StateNode other
     * @return String representation of this, containing fields that are not equal to fields in other
     */
    public String getDiffString(StateNode other) {
        if (other == null) {
            return toString();
        }

        Map<String, List<String>> otherState = other.state;

        StringBuilder sb = new StringBuilder();
        for (String key : state.keySet()) {
            if (!otherState.containsKey(key)) {
                continue;
            }

            List<String> thisKeyContents = state.get(key);
            List<String> otherKeyContents = otherState.get(key);

            if (thisKeyContents.equals(otherKeyContents)) {
                continue;
            }

            sb.append(String.format("\n%s: %s ", key, AlloyConstants.BLOCK_INITIALIZER));
            sb.append(String.format("%s %s", String.join(", ", state.get(key)), AlloyConstants.BLOCK_TERMINATOR));
        }
        sb.append("\n");
        return sb.toString();
    }

    /**
     * Two StateNodes are considered to be equivalent if they represent the exact same state.
     * @param other
     * @return true if this is the same as other, else false
     */
    @Override
    public boolean equals(Object other) {
        if (other == null) {
            return false;
        }
        final StateNode otherNode = (StateNode) other;
        if (!state.keySet().equals(otherNode.state.keySet())) {
            return false;
        }
        for (String key : state.keySet()) {
            List<String> vals = state.get(key);
            List<String> otherVals = otherNode.state.get(key);
            if (vals == null && otherVals != null) {
                return false;
            }
            if (otherVals == null && vals != null) {
                return false;
            }
            if (vals == null) {
                break;
            }
            Collections.sort(vals);
            Collections.sort(otherVals);
            if (!vals.equals(otherVals)) {
                return false;
            }
        }
        return true;
    }

    /**
     * Generate an init predicate representing this instance's state in proper Alloy syntax.
     * @return String representation of the state in correct Alloy model syntax
     */
    public String getAlloyInitString() {
        StringBuilder sb = new StringBuilder();
        for (Map.Entry<String, List<String>> entry : state.entrySet()) {
            List<String> vals = entry.getValue();
            StringBuilder alloyFormattedValsBuilder = new StringBuilder();
            String prefix = "";
            for (String val : vals) {
                alloyFormattedValsBuilder.append(prefix);
                prefix = String.format(" %s ", AlloyConstants.PLUS);
                String[] values = val.split(AlloyConstants.SET_DELIMITER);
                alloyFormattedValsBuilder.append(values[0]);
                for (int i = 1; i < values.length; i++) {
                    alloyFormattedValsBuilder.append(AlloyConstants.SET_DELIMITER);
                    alloyFormattedValsBuilder.append(values[i]);
                }
            }

            String value = (alloyFormattedValsBuilder.length() == 0) ?
                               AlloyUtils.getEmptyRelation(sigData.getArityForField(entry.getKey())) :
                               alloyFormattedValsBuilder.toString();
            sb.append(
                String.format(
                   "\ts.%s = %s\n",
                    entry.getKey(),
                    value
                )
            );
        }

        return AlloyUtils.makeStatePredicate(parsingConf.getInitPredicateName(), parsingConf.getStateSigName(), sb.toString());
    }

    public int getIdentifier() {
        return id;
    }

    public void setIdentifier(int id) {
        this.id = id;
    }
}
