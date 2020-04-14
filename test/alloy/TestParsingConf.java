package alloy;

import alloy.ParsingConf;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.Test;

public class TestParsingConf {
    @Test
    public void testGetConfStringFromFileString() {
        String content = String.join("\n",
            "/*  BEGIN_ALDB_CONF",
            " *  transitionRelationName: trans",
            " *  END_ALDB_CONF",
            " */",
            "some alloy model here"
        );
        String result = "transitionRelationName: trans";
        String confString = ParsingConf.getConfStringFromFileString(content).trim();
        assertEquals(result, confString);
    }

    @Test
    public void testGetConfStringFromFileString_invalidConf() {
        String content = String.join("\n",
            "/*  BEGIN_ALDB_CONF",
            " *  transitionRelationName: trans",
            " */",
            "some alloy model here"
        );
        String result = "";
        String confString = ParsingConf.getConfStringFromFileString(content).trim();
        assertEquals(result, confString);
    }

    @Test
    public void testInitializeWithYaml() {
        String content = String.join("\n",
            "stateSigName: name",
            "initPredicateName: init",
            "transitionRelationName: trans",
            "additionalSigScopes:",
            "  Chair: 1",
            "  Player: 2"
        );
        ParsingConf pc = ParsingConf.initializeWithYaml(content);
        Map<String, Integer> ass = pc.getAdditionalSigScopes();
        assertTrue(ass.containsKey("Chair"));
        assertTrue(ass.containsKey("Player"));
        assertEquals(1, ass.get("Chair"));
        assertEquals(2, ass.get("Player"));
        assertEquals("name", pc.getStateSigName());
        assertEquals("init", pc.getInitPredicateName());
        assertEquals("trans", pc.getTransitionRelationName());
    }
}
