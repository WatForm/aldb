package state;

import java.util.ArrayList;
import java.util.List;

/**
 * StateGraph represents a directed graph containing all states visited in the active simulation.
 */
public class StateGraph {
    private List<StateNode> nodes;

    public StateGraph() {
        nodes = new ArrayList<>();
    }

    /**
     * initWithNodes initializes the graph with initial nodes.
     * @param List<StateNode> nodes
     */
    public void initWithNodes(List<StateNode> nodes) {
        this.nodes.clear();

        for (int i = 0; i < nodes.size(); i++) {
            StateNode node = nodes.get(i);
            addNode(node);

            if (i > 0) {
                StateNode prevNode = nodes.get(i - 1);
                prevNode.addStep(node);
            }
        }
    }

    /**
     * addNodes adds a list of nodes to the graph, beginning at a start node.
     * @param StateNode startNode
     * @param List<StateNode> nodes
     */
    public void addNodes(StateNode startNode, List<StateNode> nodes) {
        // For every new node added, if it has the same state as an existing
        // node, then add an outgoing edge from the current node to it, otherwise
        // add it to the nodes list and then add the edge. Set the current node
        // to the one we transitioned to.
        StateNode curNode = getExistingNode(startNode);
        for (StateNode node : nodes) {
            StateNode nextNode = getExistingNode(node);
            if (nextNode == null) {
                nextNode = node;
                addNode(nextNode);
            }

            if (!curNode.getSteps().contains(nextNode)) {
                curNode.addStep(nextNode);
            }

            curNode = nextNode;
        }
    }

    public int size() {
        return nodes.size();
    }

    /**
     * getDOTString returns a DOT (graph description language) representation of the graph.
     * @return String
     */
    public String getDOTString() {
        StringBuilder sb = new StringBuilder();

        sb.append("digraph graphname {\n");
        for (StateNode node : nodes) {
            List<StateNode> steps = node.getSteps();

            if (steps.isEmpty()) {
                sb.append(String.format("\tS%d\n", node.getIdentifier()));
                continue;
            }

            for (StateNode stepNode : steps) {
                sb.append(String.format("\tS%d -> S%d\n", node.getIdentifier(), stepNode.getIdentifier()));
            }
        }

        sb.append("}\n");
        return sb.toString();
    }

    private void addNode(StateNode node) {
        nodes.add(node);
        node.setIdentifier(size());
    }

    private StateNode getExistingNode(StateNode node) {
        for (StateNode existingNode : this.nodes) {
            if (existingNode.equals(node)) {
                return existingNode;
            }
        }

        return null;
    }
}
