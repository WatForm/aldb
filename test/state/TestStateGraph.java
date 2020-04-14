package state;

import edu.mit.csail.sdg.ast.Sig.*;

import alloy.SigData;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

public class TestStateGraph {
    private StateGraph stateGraph = new StateGraph();

    @Test
    public void testInitWithNodes() {
        List<StateNode> nodes = createNewStateNodeList(3);
        String expected = String.join("\n",
            "digraph graphname {",
            "\tS1 -> S2",
            "\tS2 -> S3",
            "\tS3",
            "}",
            ""
        );
        stateGraph.initWithNodes(nodes);
        assertEquals(expected, stateGraph.getDOTString());
        assertEquals(3, stateGraph.size());
    }

    @Test
    public void testInitWithNodes_empty() {
        List<StateNode> nodes = new ArrayList<>();;
        String expected = String.join("\n",
            "digraph graphname {",
            "}",
            ""
        );
        stateGraph.initWithNodes(nodes);
        assertEquals(expected, stateGraph.getDOTString());
        assertEquals(0, stateGraph.size());
    }

    @Test
    public void testAddNodes_cycle() {
        List<StateNode> initialNodes = createNewStateNodeList(2);
        StateNode startNode = initialNodes.get(1);
        startNode.addValueToField("g", "diff_val");
        stateGraph.initWithNodes(initialNodes);
        List<StateNode> newNodes = createNewStateNodeList(1);
        String expected = String.join("\n",
            "digraph graphname {",
            "\tS1 -> S2",
            "\tS2 -> S1",
            "}",
            ""
        );
        stateGraph.addNodes(startNode, newNodes);
        assertEquals(expected, stateGraph.getDOTString());
        assertEquals(2, stateGraph.size());
    }

    @Test
    public void testAddNodes_existingNodes() {
        List<StateNode> initialNodes = createNewStateNodeList(2);
        stateGraph.initWithNodes(initialNodes);
        List<StateNode> newExistingNodes = createNewStateNodeList(4);
        StateNode startNode = initialNodes.get(0);
        String expected = String.join("\n",
            "digraph graphname {",
            "\tS1 -> S2",
            "\tS2",
            "}",
            ""
        );
        stateGraph.addNodes(startNode, newExistingNodes);
        assertEquals(expected, stateGraph.getDOTString());
        assertEquals(2, stateGraph.size());
    }

    @Test
    public void testAddNodes_newNodes() {
        List<StateNode> initialNodes = createNewStateNodeList(2);
        stateGraph.initWithNodes(initialNodes);
        List<StateNode> newNodes = new ArrayList<>();
        SigData sigData = new SigData(createNewSig());
        StateNode a = new StateNode(sigData, null);
        StateNode b = new StateNode(sigData, null);
        // Ensure that nodes a and b are not equal.
        a.addValueToField("g", "val1");
        b.addValueToField("g", "val2");
        newNodes.add(a);
        newNodes.add(b);
        StateNode startNode = initialNodes.get(0);
        String expected = String.join("\n",
            "digraph graphname {",
            "\tS1 -> S2",
            "\tS1 -> S3",
            "\tS2",
            "\tS3 -> S4",
            "\tS4",
            "}",
            ""
        );
        stateGraph.addNodes(startNode, newNodes);
        assertEquals(expected, stateGraph.getDOTString());
        assertEquals(4, stateGraph.size());
    }

    // Create a list with `amount` nodes that are all equal.
    private List<StateNode> createNewStateNodeList(int amount) {
        List<StateNode> nodes = new ArrayList<>();
        SigData sigData = new SigData(createNewSig());
        for (int i = 0; i < amount; i++) {
            nodes.add(new StateNode(sigData, null));
        }
        return nodes;
    }

    private PrimSig createNewSig() {
        PrimSig sigA = new PrimSig("A");
        PrimSig sigB = new PrimSig("B");
        Field f2 = sigA.addField("g", sigB);
        return sigA;
    }
}
