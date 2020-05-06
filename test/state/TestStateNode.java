package state;

import edu.mit.csail.sdg.ast.Sig;

import alloy.SigData;
import alloy.ParsingConf;
import state.StateNode;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.Before;
import org.junit.Test;

public class TestStateNode {
    private StateNode stateNode;

    @Before
    public void init() {
        SigData sigData = new SigData(createNewSig());
        stateNode = new StateNode(sigData, new ParsingConf());
    }

    @Test
    public void testAddStep() {
        SigData sigData = new SigData(createNewSig());
        StateNode stateNode2 = new StateNode(sigData, new ParsingConf());
        stateNode2.setIdentifier(2);
        stateNode.setIdentifier(1);
        stateNode.addStep(stateNode2);
        assertEquals(2, stateNode.getSteps().get(0).getIdentifier());
    }

    @Test
    public void testToString() {
        String expected = String.join("\n",
            "",
            "f: { string1, string2 }",
            "g: { string }",
            ""
        );
        stateNode.addValueToField("f", "string1");
        stateNode.addValueToField("f", "string2");
        stateNode.addValueToField("g", "string");
        assertEquals(expected, stateNode.toString());
    }

    @Test
    public void testAddValueToField_sortOrder() {
        String expected = String.join("\n",
            "",
            "f: { A, B, R, a, b }",
            "g: { 1, 2, 3, 4, 5 }",
            ""
        );
        stateNode.addValueToField("f", "B");
        stateNode.addValueToField("f", "a");
        stateNode.addValueToField("f", "A");
        stateNode.addValueToField("f", "R");
        stateNode.addValueToField("f", "b");
        stateNode.addValueToField("g", "5");
        stateNode.addValueToField("g", "4");
        stateNode.addValueToField("g", "3");
        stateNode.addValueToField("g", "1");
        stateNode.addValueToField("g", "2");
        assertEquals(expected, stateNode.toString());
    }

    @Test
    public void testStringForProperty() {
        String expected = String.join("\n",
            "",
            "{ string1, string2, string3 }",
            ""
        );
        stateNode.addValueToField("f", "string1");
        stateNode.addValueToField("f", "string2");
        stateNode.addValueToField("f", "string3");
        assertEquals(expected, stateNode.stringForProperty("f"));
    }

    @Test
    public void testStringForProperty_empty() {
        String expected = String.join("\n",
            "",
            "{  }",
            ""
        );
        assertEquals(expected, stateNode.stringForProperty("f"));
    }

    @Test
    public void testStringForProperty_notFound() {
        String expected = "Property not found.";
        stateNode.addValueToField("f", "string1");
        assertEquals(expected, stateNode.stringForProperty("x"));
    }

    @Test
    public void testGetDiffString() {
        SigData sigData = new SigData(createNewSig());
        StateNode stateNode2 = new StateNode(sigData, new ParsingConf());
        String expected = String.join("\n",
            "",
            "f: { string1, string2 }",
            ""
        );
        stateNode.addValueToField("f", "string1");
        stateNode.addValueToField("f", "string2");
        assertEquals(expected, stateNode.getDiffString(stateNode2));
    }

    @Test
    public void testGetDiffString_null() {
        String expected = String.join("\n",
            "",
            "f: { string1, string2 }",
            "g: { string }",
            ""
        );
        stateNode.addValueToField("f", "string1");
        stateNode.addValueToField("f", "string2");
        stateNode.addValueToField("g", "string");
        assertEquals(expected, stateNode.getDiffString(null));
    }

    @Test
    public void testEquals() {
        SigData sigData = new SigData(createNewSig());
        StateNode stateNode2 = new StateNode(sigData, new ParsingConf());
        StateNode stateNode3 = new StateNode(sigData, new ParsingConf());
        // reflexive:
        assertTrue(stateNode.equals(stateNode));
        // symmetric:
        assertTrue(stateNode.equals(stateNode2));
        assertTrue(stateNode2.equals(stateNode));
        // transitive:
        assertTrue(stateNode.equals(stateNode2));
        assertTrue(stateNode2.equals(stateNode3));
        assertTrue(stateNode.equals(stateNode3));
        // consistent:
        assertTrue(stateNode.equals(stateNode2));
        assertTrue(stateNode.equals(stateNode2));
        assertTrue(stateNode.equals(stateNode2));
        // null values:
        assertFalse(stateNode.equals(null));
    }

    @Test
    public void testNotEquals_differentFieldValues() {
        SigData sigData = new SigData(createNewSig());
        StateNode stateNode2 = new StateNode(sigData, new ParsingConf());
        stateNode.addValueToField("f", "string1");
        assertFalse(stateNode.equals(stateNode2));
        assertFalse(stateNode2.equals(stateNode));
    }

    @Test
    public void testNotEquals_differentFields() {
        Sig sigA = createNewSig();
        Sig sigC = new Sig.PrimSig("C");
        Sig.Field f = sigA.addField("h", sigC);
        SigData sigData = new SigData(sigA);
        StateNode stateNode2 = new StateNode(sigData, new ParsingConf());
        assertFalse(stateNode.equals(stateNode2));
        assertFalse(stateNode2.equals(stateNode));
    }

    @Test
    public void testGetAlloyInitString() {
        String expected = String.join("\n",
            "",
            "pred init[s: State] {",
            "\ts.f = string1 + string2",
            "\ts.g = string",
            "}",
            ""
        );
        stateNode.addValueToField("f", "string1");
        stateNode.addValueToField("f", "string2");
        stateNode.addValueToField("g", "string");
        assertEquals(expected, stateNode.getAlloyInitString());
    }

    @Test
    public void testIdentifier() {
        stateNode.setIdentifier(5);
        assertEquals(5, stateNode.getIdentifier());
    }

    private Sig createNewSig() {
        Sig sigA = new Sig.PrimSig("A");
        Sig sigB = new Sig.PrimSig("B");
        Sig.Field f1 = sigA.addField("g", sigB);
        Sig.Field f2 = sigA.addField("f", sigB.lone_arrow_lone(sigB));
        return sigA;
    }
}
