package simulation;

import simulation.ConstraintManager;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

public class TestConstraintManager {
    @Test
    public void testAddConstraint() {
        ConstraintManager cm = new ConstraintManager();
        String c1 = "a=b";
        String c2 = "c=d";
        cm.addConstraint(c1);
        cm.addConstraint(c2);
        List<String> expected = new ArrayList<String>();
        expected.add(c1);
        expected.add(c2);
        assertEquals(expected, cm.getConstraints());
    }

    @Test
    public void testRemoveConstraint() {
        ConstraintManager cm = new ConstraintManager();
        String c1 = "a=b";
        String c2 = "c=d";
        cm.addConstraint(c1);
        cm.addConstraint(c2);
        assertTrue(cm.removeConstraint(1));
        assertTrue(cm.removeConstraint(2));
        assertFalse(cm.removeConstraint(3));
        assertEquals(new ArrayList<String>(), cm.getConstraints());
    }

    @Test
    public void testClearConstraints() {
        ConstraintManager cm = new ConstraintManager();
        String c1 = "a=b";
        String c2 = "c=d";
        cm.addConstraint(c1);
        cm.addConstraint(c2);
        cm.clearConstraints();
        assertEquals(new ArrayList<String>(), cm.getConstraints());
    }

    @Test
    public void testGetFormattedConstraints() {
        ConstraintManager cm = new ConstraintManager();
        String c1 = "a=b";
        String c2 = "c=d";
        cm.addConstraint(c1);
        cm.addConstraint(c2);
        String expected = "\nNum     Constraint\n1       a=b\n2       c=d\n";
        assertEquals(expected, cm.getFormattedConstraints());
    }
}
