package alloy;

import edu.mit.csail.sdg.ast.Sig;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.Test;

import java.io.IOException;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

public class TestAlloyUtils {
    @Test
    public void testGetEmptyRelation() {
        String expected = "none -> none -> none";
        assertEquals(expected, AlloyUtils.getEmptyRelation(3));
    }

    @Test
    public void testGetEmptyRelation_zero() {
        String expected = "none";
        assertEquals(expected, AlloyUtils.getEmptyRelation(0));
    }

    @Test
    public void testGetEmptyRelation_negative() {
        String expected = "none";
        assertEquals(expected, AlloyUtils.getEmptyRelation(-3));
    }

    @Test
    public void testAnnotatedTransitionSystem_untilFalse() {
        String model = "Some model";
        int steps = 5;
        boolean until = false;
        String expected = String.join("\n",
            "open util/ordering[State]",
            "",
            "Some model",
            "",
            "fact { init[first] }",
            "",
            "fact { all s: State, s': s.next { next[s, s'] } }",
            "",
            "run {} for exactly 6 State"
        );
        String result = AlloyUtils.annotatedTransitionSystem(
                            model,
                            new ParsingConf(),
                            steps,
                            until
                        );
        assertEquals(expected, result);
    }

    @Test
    public void testAnnotatedTransitionSystem_untilTrue() {
        String model = "Some model";
        int steps = 5;
        boolean until = true;
        String expected = String.join("\n",
            "open util/ordering[State]",
            "",
            "Some model",
            "",
            "fact { init[first] }",
            "",
            "fact { all s: State, s': s.next { next[s, s'] } }",
            "",
            "run {break[last]} for exactly 6 State"
        );
        String result = AlloyUtils.annotatedTransitionSystem(
                            model,
                            new ParsingConf(),
                            steps,
                            until
                        );
        assertEquals(expected, result);
    }

    @Test
    public void testGetBreakPredicate() {
        List<String> rawConstraints = new ArrayList<String>();
        rawConstraints.add("f");
        rawConstraints.add("f=g");
        SigData sigData = new SigData(createNewSig());
        String expected = String.join("\n",
            "",
            "pred break[s: A] {",
            "\t(s.f) or (s.f=s.g)",
            "}",
            ""
        );
        assertEquals(expected, AlloyUtils.getBreakPredicate(rawConstraints, sigData));
    }

    @Test
    public void testGetBreakPredicate_noConstraints() {
        List<String> rawConstraints = new ArrayList<String>();
        SigData sigData = new SigData(createNewSig());
        String expected = String.join("\n",
            "",
            "pred break[s: A] {",
            "\t",
            "}",
            ""
        );
        assertEquals(expected, AlloyUtils.getBreakPredicate(rawConstraints, sigData));
    }

    @Test
    public void testMakeStatePredicate() {
        String predicateName = "test";
        String stateSigName = "State";
        String contents = "...\n";
        String expected = String.join("\n",
            "",
            "pred test[s: State] {",
            "...",
            "}",
            ""
        );
        assertEquals(expected, AlloyUtils.makeStatePredicate(predicateName, stateSigName, contents));
    }

    @Test
    public void testGetConcreteSigsDefinition() {
        Map<String, Integer> sigScopes = new TreeMap<String, Integer>();
        sigScopes.put("Player", 4);
        sigScopes.put("Chair", 3);

        String expected = String.join("\n",
            "one sig Chair_0, Chair_1, Chair_2 extends Chair {}",
            "one sig Player_0, Player_1, Player_2, Player_3 extends Player {}",
            ""
        );
        assertEquals(expected, AlloyUtils.getConcreteSigsDefinition(sigScopes));
    }

    @Test
    public void testGetConcreteSigsDefinition_someZero() {
        Map<String, Integer> sigScopes = new TreeMap<String, Integer>();
        sigScopes.put("Player", 0);
        sigScopes.put("Chair", 3);

        String expected = String.join("\n",
            "one sig Chair_0, Chair_1, Chair_2 extends Chair {}",
            ""
        );
        assertEquals(expected, AlloyUtils.getConcreteSigsDefinition(sigScopes));
    }

    @Test
    public void testGetConcreteSigsDefinition_allZero() {
        Map<String, Integer> sigScopes = new TreeMap<String, Integer>();
        sigScopes.put("Player", 0);
        sigScopes.put("Chair", 0);

        String expected = "";
        assertEquals(expected, AlloyUtils.getConcreteSigsDefinition(sigScopes));
    }

    @Test
    public void testGetConcreteSigsDefinition_empty() {
        Map<String, Integer> sigScopes = new TreeMap<String, Integer>();

        String expected = "";
        assertEquals(expected, AlloyUtils.getConcreteSigsDefinition(sigScopes));
    }

    private Sig createNewSig() {
        Sig sigA = new Sig.PrimSig("A");
        Sig sigB = new Sig.PrimSig("B");
        Sig.Field f1 = sigA.addField("f", sigB.lone_arrow_lone(sigB));
        Sig.Field f2 = sigA.addField("g", sigB);
        return sigA;
    }
}
