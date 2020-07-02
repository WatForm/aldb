package simulation;

import simulation.AliasManager;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.Test;

public class TestAliasManager {
    @Test
    public void testAddAlias() {
        AliasManager am = new AliasManager();
        String alias = "f1";
        String alias2 = "f2";
        String formula = "a=b";

        assertTrue(am.addAlias(alias, formula));
        assertTrue(am.isAlias(alias));
        assertEquals(formula, am.getFormula(alias));
        assertEquals(null, am.getFormula(alias2));
        assertFalse(am.isAlias(alias2));

        assertTrue(am.addAlias(alias2, formula));
        assertTrue(am.isAlias(alias));
        assertTrue(am.isAlias(alias2));
        assertEquals(formula, am.getFormula(alias));
        assertEquals(formula, am.getFormula(alias2));

        // Test overwriting.
        String formula2 = "c=d";
        assertTrue(am.addAlias(alias2, formula2));
        assertTrue(am.isAlias(alias));
        assertTrue(am.isAlias(alias2));
        assertEquals(formula, am.getFormula(alias));
        assertEquals(formula2, am.getFormula(alias2));
    }

    @Test
    public void testAddAlias_nested() {
        AliasManager am = new AliasManager();

        assertTrue(am.addAlias("a", "1"));
        assertTrue(am.isAlias("a"));
        assertEquals("1", am.getFormula("a"));

        assertTrue(am.addAlias("b", "`a`+1"));
        assertTrue(am.isAlias("b"));
        assertEquals("1+1", am.getFormula("b"));

        assertTrue(am.addAlias("c", "`a`+`b`+1"));
        assertTrue(am.isAlias("c"));
        assertEquals("1+1+1+1", am.getFormula("c"));

        assertTrue(am.addAlias("d", "`a `+1"));
        assertTrue(am.isAlias("d"));
        assertEquals("1+1", am.getFormula("d"));

        assertTrue(am.addAlias("e", "` a`+1"));
        assertTrue(am.isAlias("e"));
        assertEquals("1+1", am.getFormula("e"));

        assertTrue(am.addAlias("f", "``+1"));
        assertTrue(am.isAlias("f"));
        assertEquals("+1", am.getFormula("f"));

        assertTrue(am.addAlias("abcd", "2"));
        assertTrue(am.isAlias("abcd"));
        assertEquals("2", am.getFormula("abcd"));

        assertTrue(am.addAlias("g", "`abcd`+1"));
        assertTrue(am.isAlias("g"));
        assertEquals("2+1", am.getFormula("g"));

        assertTrue(am.addAlias("h", "1+`abcd`"));
        assertTrue(am.isAlias("h"));
        assertEquals("1+2", am.getFormula("h"));

        assertTrue(am.addAlias("i", "`abcd`"));
        assertTrue(am.isAlias("i"));
        assertEquals("2", am.getFormula("i"));

        // Removing an alias used in the definition of a newer alias should not affect the newer alias.
        assertTrue(am.removeAlias("abcd"));
        assertFalse(am.isAlias("abcd"));
        assertTrue(am.isAlias("i"));
        assertEquals("2", am.getFormula("i"));

        assertFalse(am.addAlias("z", "`a``+1"));
        assertFalse(am.isAlias("z"));

        assertFalse(am.isAlias("foo"));
        assertFalse(am.addAlias("z", "`foo`+1"));
        assertFalse(am.isAlias("z"));

        assertFalse(am.addAlias("z", "``"));
        assertFalse(am.isAlias("z"));
    }

    @Test
    public void testRemoveAlias() {
        AliasManager am = new AliasManager();
        String alias = "f1";
        String formula = "a=b";

        assertFalse(am.removeAlias(alias));
        assertTrue(am.addAlias(alias, formula));
        assertTrue(am.removeAlias(alias));
        assertFalse(am.isAlias(alias));
        assertFalse(am.removeAlias(alias));
    }

    @Test
    public void testClearAliases() {
        AliasManager am = new AliasManager();
        String alias = "f1";
        String alias2 = "f2";
        String formula = "a=b";

        assertTrue(am.addAlias(alias, formula));
        assertTrue(am.addAlias(alias2, formula));
        am.clearAliases();
        assertFalse(am.isAlias(alias));
        assertFalse(am.isAlias(alias2));
    }

    @Test
    public void testGetFormattedAliases() {
        AliasManager am = new AliasManager();
        String alias = "f1";
        String alias2 = "f2";
        String formula = "a=b";

        assertTrue(am.addAlias(alias, formula));
        assertTrue(am.addAlias(alias2, formula));
        String expected = "\nAlias           Formula\nf1              a=b\nf2              a=b\n";
        assertEquals(expected, am.getFormattedAliases());
    }
}
