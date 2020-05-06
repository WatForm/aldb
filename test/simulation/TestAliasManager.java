package simulation;

import simulation.AliasManager;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.Test;

public class TestAliasManager {
    @Test
    public void testAddAlias() {
        AliasManager am = new AliasManager();
        String alias = "p1";
        String alias2 = "p2";
        String predicate = "a=b";

        am.addAlias(alias, predicate);
        assertTrue(am.isAlias(alias));
        assertFalse(am.isAlias(alias2));

        am.addAlias(alias2, predicate);
        assertTrue(am.isAlias(alias));
        assertTrue(am.isAlias(alias2));

        // Test overwriting.
        String predicate2 = "c=d";
        am.addAlias(alias, predicate2);
        assertTrue(am.isAlias(alias));
        assertTrue(am.isAlias(alias2));
    }

    @Test
    public void testRemoveAlias() {
        AliasManager am = new AliasManager();
        String alias = "p1";
        String predicate = "a=b";

        assertFalse(am.removeAlias(alias));
        am.addAlias(alias, predicate);
        assertTrue(am.removeAlias(alias));
        assertFalse(am.isAlias(alias));
        assertFalse(am.removeAlias(alias));
    }

    @Test
    public void testClearAliases() {
        AliasManager am = new AliasManager();
        String alias = "p1";
        String alias2 = "p2";
        String predicate = "a=b";

        am.addAlias(alias, predicate);
        am.addAlias(alias2, predicate);
        am.clearAliases();
        assertFalse(am.isAlias(alias));
        assertFalse(am.isAlias(alias2));
    }

    @Test
    public void testGetFormattedAliases() {
        AliasManager am = new AliasManager();
        String alias = "p1";
        String alias2 = "p2";
        String predicate = "a=b";

        am.addAlias(alias, predicate);
        am.addAlias(alias2, predicate);
        String expected = "\nAlias           Predicate\np1              a=b\np2              a=b\n";
        assertEquals(expected, am.getFormattedAliases());
    }
}
