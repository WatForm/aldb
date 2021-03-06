package state;

import edu.mit.csail.sdg.ast.Sig.PrimSig;

import alloy.SigData;
import state.StateNode;
import state.StatePath;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

public class TestStatePath {
    StateNode n0;
    StateNode n1;
    StateNode n2;

    @Before
    public void init() {
        n0 = new StateNode(new SigData(new PrimSig("a")), null);
        n1 = new StateNode(new SigData(new PrimSig("b")), null);
        n2 = new StateNode(new SigData(new PrimSig("c")), null);

        n0.setIdentifier(0);
        n1.setIdentifier(1);
        n2.setIdentifier(2);
    }

    @Test
    public void testInitWithPath() {
        StatePath sp = new StatePath();
        List<StateNode> path = new ArrayList<StateNode>();
        path.add(n0);
        path.add(n1);
        sp.initWithPath(path);

        assertFalse(sp.isEmpty());
        assertTrue(sp.atEnd());
        assertEquals(n0, sp.getNode(0));
        assertEquals(n1, sp.getNode(1));
        assertEquals(1, sp.getPosition());

        // Should be idempotent with the same input path.
        sp.initWithPath(path);
        assertFalse(sp.isEmpty());
        assertTrue(sp.atEnd());
        assertEquals(n0, sp.getNode(0));
        assertEquals(n1, sp.getNode(1));
        assertEquals(1, sp.getPosition());
    }

    @Test
    public void testIsEmpty() {
        StatePath sp = new StatePath();
        assertTrue(sp.isEmpty());

        List<StateNode> path = new ArrayList<StateNode>();
        path.add(n0);
        sp.initWithPath(path);
        assertFalse(sp.isEmpty());
    }

    @Test
    public void testGetNode() {
        StatePath sp = new StatePath();
        assertEquals(null, sp.getNode(-1));
        assertEquals(null, sp.getNode(0));

        List<StateNode> path = new ArrayList<StateNode>();
        path.add(n0);
        sp.initWithPath(path);
        assertEquals(n0, sp.getNode(0));
        assertEquals(null, sp.getNode(1));
    }

    @Test
    public void testGetCurNode() {
        StatePath sp = new StatePath();
        assertEquals(null, sp.getCurNode());

        List<StateNode> path = new ArrayList<StateNode>();
        path.add(n0);
        path.add(n1);
        sp.initWithPath(path);

        assertEquals(n1, sp.getCurNode());
    }

    @Test
    public void testSetAndGetPosition() {
        StatePath sp = new StatePath();
        assertEquals(0, sp.getPosition());

        List<StateNode> path = new ArrayList<StateNode>();
        path.add(n0);
        path.add(n1);
        sp.initWithPath(path);
        assertEquals(1, sp.getPosition());

        sp.setPosition(0);
        assertEquals(0, sp.getPosition());
    }

    @Test
    public void testIncrementPosition() {
        StatePath sp = new StatePath();
        List<StateNode> path = new ArrayList<StateNode>();
        path.add(n0);
        path.add(n1);
        path.add(n2);

        sp.initWithPath(path);
        sp.setPosition(0);
        assertEquals(0, sp.getPosition());
        assertFalse(sp.atEnd());

        sp.incrementPosition(1);
        assertEquals(1, sp.getPosition());
        assertFalse(sp.atEnd());

        sp.incrementPosition(1);
        assertEquals(2, sp.getPosition());
        assertTrue(sp.atEnd());
    }

    @Test
    public void testDecrementPosition() {
        // Decrement path size 3 by 1.
        StatePath sp = new StatePath();
        List<StateNode> path = new ArrayList<StateNode>();
        path.add(n0);
        path.add(n1);
        path.add(n2);
        sp.initWithPath(path);

        sp.decrementPosition(1, false);
        assertEquals(1, sp.getPosition());
        assertEquals(n1, sp.getCurNode());

        // Decrement path size 3 by 2.
        sp = new StatePath();
        sp.initWithPath(path);

        sp.decrementPosition(2, false);
        assertEquals(0, sp.getPosition());
        assertEquals(n0, sp.getCurNode());

        // Decrement path size 3 by 3.
        sp = new StatePath();
        sp.initWithPath(path);

        sp.decrementPosition(3, false);
        assertEquals(0, sp.getPosition());
        assertEquals(n0, sp.getCurNode());

        // Decrement path size 3 by 4.
        sp = new StatePath();
        sp.initWithPath(path);

        sp.decrementPosition(4, false);
        assertEquals(0, sp.getPosition());
        assertEquals(n0, sp.getCurNode());
    }

    @Test
    public void testSetTempPath() {
        StatePath sp = new StatePath();
        List<StateNode> tempPath = new ArrayList<StateNode>();
        tempPath.add(n0);
        tempPath.add(n1);
        sp.setTempPath(tempPath);
        assertEquals(1, sp.getPosition());
        assertEquals(n1, sp.getCurNode());
        assertEquals(tempPath.size(), sp.getTempPathSize());
    }

    @Test
    public void testClearTempPath() {
        // Clear temp path with no initial path set.
        StatePath sp = new StatePath();
        List<StateNode> tempPath = new ArrayList<StateNode>();
        tempPath.add(n0);
        tempPath.add(n1);
        sp.setTempPath(tempPath);

        sp.clearTempPath();
        assertEquals(-1, sp.getPosition());
        assertEquals(null, sp.getCurNode());
        assertEquals(0, sp.getTempPathSize());

        // Clear temp path with an initial path set.
        sp.initWithPath(tempPath);

        // Should be a no-op.
        sp.clearTempPath();
        assertEquals(1, sp.getPosition());
        assertEquals(n1, sp.getCurNode());
        assertEquals(0, sp.getTempPathSize());

        sp.setTempPath(tempPath);
        sp.clearTempPath();
        assertEquals(1, sp.getPosition());
        assertEquals(n1, sp.getCurNode());
        assertEquals(0, sp.getTempPathSize());
    }

    @Test
    public void testClearPath() {
        StatePath sp = new StatePath();
        sp.clearPath();
        assertTrue(sp.isEmpty());
        assertEquals(0, sp.getTempPathSize());

        List<StateNode> tempPath = new ArrayList<StateNode>();
        tempPath.add(n0);
        tempPath.add(n1);
        sp.setTempPath(tempPath);

        assertFalse(sp.isEmpty());
        sp.clearPath();
        assertTrue(sp.isEmpty());
        assertEquals(0, sp.getTempPathSize());
    }

    @Test
    public void testGetHistory() {
        StatePath sp = new StatePath();
        assertEquals("", sp.getHistory(0, false));
        assertEquals("", sp.getHistory(0, true));
        assertEquals("", sp.getHistory(1, false));
        assertEquals("", sp.getHistory(1, true));

        List<StateNode> path = new ArrayList<StateNode>();
        path.add(n0);
        path.add(n1);
        path.add(n2);
        sp.initWithPath(path);

        assertEquals("\nS0 (-2)\n---------\n\nS1 (-1)\n---------\n", sp.getHistory(3, false));
        assertEquals("\nS0 (-2)\n---------\n\nS0 -> S1 (-1)\n-----------------\n", sp.getHistory(3, true));
        assertEquals("\nS0 (-2)\n---------\n\nS1 (-1)\n---------\n", sp.getHistory(2, false));
        assertEquals("\nS0 (-2)\n---------\n\nS0 -> S1 (-1)\n-----------------\n", sp.getHistory(2, true));
        assertEquals("\nS1 (-1)\n---------\n", sp.getHistory(1, false));
        assertEquals("\nS0 -> S1 (-1)\n-----------------\n", sp.getHistory(1, true));
        assertEquals("", sp.getHistory(0, false));
        assertEquals("", sp.getHistory(0, true));
    }
}
