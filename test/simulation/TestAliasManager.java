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

        am.addAlias(alias, formula);
        assertTrue(am.isAlias(alias));
        assertEquals(formula, am.getFormula(alias));
        assertEquals(null, am.getFormula(alias2));
        assertFalse(am.isAlias(alias2));

        am.addAlias(alias2, formula);
        assertTrue(am.isAlias(alias));
        assertTrue(am.isAlias(alias2));
        assertEquals(formula, am.getFormula(alias));
        assertEquals(formula, am.getFormula(alias2));

        // Test overwriting.
        String formula2 = "c=d";
        am.addAlias(alias2, formula2);
        assertTrue(am.isAlias(alias));
        assertTrue(am.isAlias(alias2));
        assertEquals(formula, am.getFormula(alias));
        assertEquals(formula2, am.getFormula(alias2));
    }

    @Test
    public void testRemoveAlias() {
        AliasManager am = new AliasManager();
        String alias = "f1";
        String formula = "a=b";

        assertFalse(am.removeAlias(alias));
        am.addAlias(alias, formula);
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

        am.addAlias(alias, formula);
        am.addAlias(alias2, formula);
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

        am.addAlias(alias, formula);
        am.addAlias(alias2, formula);
        String expected = "\nAlias           Formula\nf1              a=b\nf2              a=b\n";
        assertEquals(expected, am.getFormattedAliases());
    }
}
