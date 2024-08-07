package simulation;

import alloy.AlloyInterface;
import alloy.ParsingConf;

import edu.mit.csail.sdg.translator.A4Solution;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.rules.TemporaryFolder;
import org.junit.Test;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TestSimulationManager {
    private final ByteArrayOutputStream outContent = new ByteArrayOutputStream();
    private final PrintStream originalOut = System.out;

    private SimulationManager sm;
    private File modelFile;

    @Rule
    public TemporaryFolder tempFolder = new TemporaryFolder();

    @Before
    public void init() {
        System.setOut(new PrintStream(outContent));
        sm = new SimulationManager();
    }

    @After
    public void cleanUp() {
        System.setOut(originalOut);
    }

    @Test
    public void testInitializeWithModel() throws IOException {
        initializeTestWithModelPath("models/switch.als");
        String expectedDOTString = String.join("\n",
            "digraph graphname {",
            "\tS1",
            "}",
            ""
        );
        assertTrue(sm.initialize(modelFile, false));
        assertTrue(sm.isInitialized());
        assertFalse(sm.isTrace());
        assertEquals(expectedDOTString, sm.getDOTString());
    }

    @Test
    public void testInitializeWithModel_nonexistantFile() {
        File nonexistantFile = new File("nonexistant-file");
        assertFalse(sm.initialize(nonexistantFile, false));
        assertFalse(sm.isInitialized());
        assertTrue(outContent.toString().contains("Syntax error"));
    }

    @Test
    public void testInitializeWithModel_noInitPredicate() throws IOException {
        String noInitModel = String.join("\n",
            "sig State {}",
            "pred next[s, sprime: State] {}",
            ""
        );
        initializeTestWithModelString(noInitModel);
        assertFalse(sm.initialize(modelFile, false));
        assertFalse(sm.isInitialized());
        assertTrue(outContent.toString().contains("init not found"));
    }

    @Test
    public void testInitializeWithModel_noNextPredicate() throws IOException {
        String noNextModel = String.join("\n",
            "sig State {}",
            "pred init[s: State] {}",
            ""
        );
        initializeTestWithModelString(noNextModel);
        assertFalse(sm.initialize(modelFile, false));
        assertFalse(sm.isInitialized());
        assertTrue(outContent.toString().contains("next not found"));
    }

    @Test
    public void testInitializeWithModel_syntaxError_incompletePredicate() throws IOException {
        String invalidModel = String.join("\n",
            "sig State {}",
            "pred init[s: State] {",
            ""
        );
        initializeTestWithModelString(invalidModel);
        assertFalse(sm.initialize(modelFile, false));
        assertFalse(sm.isInitialized());
        assertTrue(outContent.toString().contains("Syntax error"));
    }

    @Test
    public void testInitializeWithModel_missingScopes() throws IOException {
        String model = String.join("\n",
            "sig Foo {}",
            "sig State { x: set Foo }",
            "pred init[s: State] {}",
            "pred next[s, sprime: State] {}",
            ""
        );
        initializeTestWithModelString(model);
        assertFalse(sm.initialize(modelFile, false));
        assertFalse(sm.isInitialized());
        assertTrue(outContent.toString().contains("must specify a scope for sig \"this/Foo\""));
    }

    @Test
    public void testInitializeWithModel_unsat() throws IOException {
        String unsatModel = String.join("\n",
            "sig State { x: Int }",
            "pred init[s: State] { s.x = none }",
            "pred next[s, sprime: State] {}",
            ""
        );
        initializeTestWithModelString(unsatModel);
        assertFalse(sm.initialize(modelFile, false));
        assertFalse(sm.isInitialized());
        assertTrue(outContent.toString().contains("No instance found"));
    }

    @Test
    public void testInitializeWithModel_historyDeleted() throws IOException {
        initializeTestWithModelPath("models/switch.als");
        assertTrue(sm.initialize(modelFile, false));
        assertTrue(sm.performStep(1));

        initializeTestWithModelPath("models/even_odd.als");
        assertTrue(sm.initialize(modelFile, false));
        assertEquals("", sm.getHistory(3));
    }

    @Test
    public void testInitializeWithTrace() throws IOException {
        File traceFile = createTrace();

        assertTrue(sm.initialize(traceFile, true));
        assertTrue(sm.isTrace());
    }

    @Test
    public void testInitializeWithTrace_invalidXML() throws IOException {
        initializeTestWithModelPath("models/switch.als");
        assertFalse(sm.initialize(modelFile, true));
        assertFalse(sm.isTrace());
        assertTrue(outContent.toString().contains("error. Could not read XML file."));
    }

    @Test
    public void testPerformStep_model() throws IOException {
        initializeTestWithModelPath("models/switch.als");
        String expectedDOTString = String.join("\n",
            "digraph graphname {",
            "\tS1 -> S2",
            "\tS2 -> S1",
            "}",
            ""
        );
        String expectedCurrentState = String.join("\n",
            "",
            "S1",
            "----",
            "a: { On }",
            "b: { Off }",
            ""
        );
        String expectedHistory = String.join("\n",
            "",
            "S1 (-2)",
            "---------",
            "a: { On }",
            "b: { Off }",
            "",
            "S2 (-1)",
            "---------",
            "a: { Off }",
            "b: { On }",
            ""
        );
        sm.initialize(modelFile, false);
        sm.performStep(2);
        assertEquals(expectedDOTString, sm.getDOTString());
        assertEquals(expectedCurrentState, sm.getCurrentStateString());
        assertEquals(expectedHistory, sm.getHistory(2));
    }

    @Test
    public void testPerformStep_modelWithScopes() throws IOException {
        String content = String.join("\n",
            "transitionRelationName: trans",
            "additionalSigScopes:",
            "  Chair: 3",
            "  Player: 4"
        );
        ParsingConf pc = ParsingConf.initializeWithYaml(content);

        initializeTestWithModelPath("models/musical_chairs.als");
        String expectedDOTString = String.join("\n",
            "digraph graphname {",
            "\tS1 -> S2",
            "\tS2 -> S3",
            "\tS3",
            "}",
            ""
        );
        String expectedCurrentState = String.join("\n",
            "",
            "S3",
            "----",
            "chairs: { Chair_0, Chair_1, Chair_2 }",
            "mode: { sitting }",
            "occupied: { Chair_0->Player_2, Chair_1->Player_1, Chair_2->Player_0 }",
            "players: { Player_0, Player_1, Player_2, Player_3 }",
            ""
        );
        String expectedHistory = String.join("\n",
            "",
            "S1 (-2)",
            "---------",
            "chairs: { Chair_0, Chair_1, Chair_2 }",
            "mode: { start }",
            "occupied: {  }",
            "players: { Player_0, Player_1, Player_2, Player_3 }",
            "",
            "S2 (-1)",
            "---------",
            "chairs: { Chair_0, Chair_1, Chair_2 }",
            "mode: { walking }",
            "occupied: {  }",
            "players: { Player_0, Player_1, Player_2, Player_3 }",
            ""
        );

        sm.setParsingConf(pc);
        assertTrue(sm.initialize(modelFile, false));
        sm.performStep(2);
        assertEquals(expectedDOTString, sm.getDOTString());
        assertEquals(expectedCurrentState, sm.getCurrentStateString());
        assertEquals(expectedHistory, sm.getHistory(2));
    }

    @Test
    public void testPerformStep_modelWithBitwidthScope() throws IOException {
        initializeTestWithModelPath("models/even_odd.als");
        String expectedDOTString = String.join("\n",
            "digraph graphname {",
            "\tS1 -> S2",
            "\tS2 -> S3",
            "\tS3 -> S4",
            "\tS4 -> S5",
            "\tS5 -> S6",
            "\tS6 -> S7",
            "\tS7 -> S8",
            "\tS8 -> S9",
            "\tS9 -> S10",
            "\tS10 -> S11",
            "\tS11",
            "}",
            ""
        );
        String expectedCurrentState = String.join("\n",
            "",
            "S11",
            "----",
            "i: { 20 }",
            ""
        );
        String expectedHistory = String.join("\n",
            "",
            "S9 (-2)",
            "---------",
            "i: { 16 }",
            "",
            "S10 (-1)",
            "---------",
            "i: { 18 }",
            ""
        );

        assertTrue(sm.initialize(modelFile, false));
        sm.performStep(10);
        assertEquals(expectedDOTString, sm.getDOTString());
        assertEquals(expectedCurrentState, sm.getCurrentStateString());
        assertEquals(expectedHistory, sm.getHistory(2));
    }

    @Test
    public void testPerformStep_trace() throws IOException {
        File traceFile = createTrace();
        String expectedDOTString = String.join("\n",
            "digraph graphname {",
            "\tS1 -> S2",
            "\tS2",
            "}",
            ""
        );
        String expectedCurrentState = String.join("\n",
            "",
            "S2",
            "----",
            "a: { On }",
            "b: { Off }",
            ""
        );
        String expectedHistory = String.join("\n",
            "",
            "S1 (-1)",
            "---------",
            "a: { Off }",
            "b: { On }",
            ""
        );

        assertTrue(sm.initialize(traceFile, true));
        assertTrue(sm.isTrace());
        assertTrue(sm.performStep(1));
        assertEquals(expectedDOTString, sm.getDOTString());
        assertEquals(expectedCurrentState, sm.getCurrentStateString());
        assertEquals(expectedHistory, sm.getHistory(1));

        // End of trace reached.
        assertFalse(sm.performStep(1));
        assertFalse(sm.performStep(2));
    }

    @Test
    public void testPerformStep_withConstraintsSatisfiable() throws IOException {
        initializeTestWithModelPath("models/river_crossing.als");
        String expectedDOTString = String.join("\n",
            "digraph graphname {",
            "\tS1 -> S2",
            "\tS2 -> S3",
            "\tS3",
            "}",
            ""
        );
        String expectedCurrentState = String.join("\n",
            "",
            "S3",
            "----",
            "far: { Chicken }",
            "near: { Farmer, Fox, Grain }",
            ""
        );
        String expectedHistory = String.join("\n",
            "",
            "S1 (-2)",
            "---------",
            "far: {  }",
            "near: { Chicken, Farmer, Fox, Grain }",
            "",
            "S2 (-1)",
            "---------",
            "far: { Chicken, Farmer }",
            "near: { Fox, Grain }",
            ""
        );

        List<String> constraints = new ArrayList<String>(
            Arrays.asList("Chicken in far", "(Farmer in near) and (Chicken in far)")
        );
        sm.initialize(modelFile, false);
        sm.performStep(constraints.size(), constraints);
        assertEquals(expectedDOTString, sm.getDOTString());
        assertEquals(expectedCurrentState, sm.getCurrentStateString());
        assertEquals(expectedHistory, sm.getHistory(2));
    }

    @Test
    public void testPerformStep_withConstraintsUnsatisfiable() throws IOException {
        initializeTestWithModelPath("models/river_crossing.als");
        String expectedDOTString = String.join("\n",
            "digraph graphname {",
            "\tS1",
            "}",
            ""
        );
        String expectedCurrentState = String.join("\n",
            "",
            "S1",
            "----",
            "far: {  }",
            "near: { Chicken, Farmer, Fox, Grain }",
            ""
        );
        String expectedHistory = String.join("\n",
            ""
        );

        List<String> constraints = new ArrayList<String>(
            Arrays.asList("(Farmer in near) and (Farmer in far)")
        );
        sm.initialize(modelFile, false);
        sm.performStep(constraints.size(), constraints);
        assertEquals(expectedDOTString, sm.getDOTString());
        assertEquals(expectedCurrentState, sm.getCurrentStateString());
        assertEquals(expectedHistory, sm.getHistory(2));
    }

    @Test
    public void testPerformReverseStep_model() throws IOException {
        initializeTestWithModelPath("models/switch.als");
        String expectedDOTString = String.join("\n",
            "digraph graphname {",
            "\tS1 -> S2",
            "\tS2 -> S1",
            "}",
            ""
        );
        String expectedCurrentState = String.join("\n",
            "",
            "S1",
            "----",
            "a: { On }",
            "b: { Off }",
            ""
        );
        String expectedHistory = String.join("\n",
            "",
            "S1 (-2)",
            "---------",
            "a: { On }",
            "b: { Off }",
            "",
            "S2 (-1)",
            "---------",
            "a: { Off }",
            "b: { On }",
            ""
        );
        sm.initialize(modelFile, false);
        sm.performStep(4);
        sm.performReverseStep(2);
        assertEquals(expectedDOTString, sm.getDOTString());
        assertEquals(expectedCurrentState, sm.getCurrentStateString());
        assertEquals(expectedHistory, sm.getHistory(2));
    }

    @Test
    public void testPerformReverseStep_reverseThenAltAtInit() throws IOException {
        // Tests the sequence: s 2, r 2, a, s
        initializeTestWithModelPath("models/even_odd.als");
        String dotString1 = String.join("\n",
            "digraph graphname {",
            "\tS1 -> S2",
            "\tS2 -> S3",
            "\tS3",
            "}",
            ""
        );
        String dotString2 = String.join("\n",
            "digraph graphname {",
            "\tS1 -> S2",
            "\tS2 -> S3",
            "\tS3",
            "\tS4",
            "}",
            ""
        );
        String dotString3 = String.join("\n",
            "digraph graphname {",
            "\tS1 -> S2",
            "\tS2 -> S3",
            "\tS3",
            "\tS4 -> S5",
            "\tS5",
            "}",
            ""
        );
        String state1 = String.join("\n",
            "",
            "S1",
            "----",
            "i: { 0 }",
            ""
        );
        String state2 = String.join("\n",
            "",
            "S4",
            "----",
            "i: { 1 }",
            ""
        );
        String state3 = String.join("\n",
            "",
            "S5",
            "----",
            "i: { 3 }",
            ""
        );
        String history = String.join("\n",
            "",
            "S4 (-1)",
            "---------",
            "i: { 1 }",
            ""
        );

        assertTrue(sm.initialize(modelFile, false));
        assertTrue(sm.performStep(2));
        sm.performReverseStep(2);
        assertEquals(state1, sm.getCurrentStateString());
        assertEquals("", sm.getHistory(3));
        assertEquals(dotString1, sm.getDOTString());

        assertTrue(sm.selectAlternatePath(false));
        assertEquals(state2, sm.getCurrentStateString());
        assertEquals("", sm.getHistory(3));
        assertEquals(dotString2, sm.getDOTString());

        assertTrue(sm.performStep(1));
        assertEquals(state3, sm.getCurrentStateString());
        assertEquals(history, sm.getHistory(3));
        assertEquals(dotString3, sm.getDOTString());
    }

    @Test
    public void testPerformReverseStep_altThenReverseToInit() throws IOException {
        // Tests the sequence: a, s 2, r 2, s 3
        initializeTestWithModelPath("models/even_odd.als");
        String dotString1 = String.join("\n",
            "digraph graphname {",
            "\tS1",
            "\tS2 -> S3",
            "\tS3 -> S4",
            "\tS4",
            "}",
            ""
        );
        String dotString2 = String.join("\n",
            "digraph graphname {",
            "\tS1",
            "\tS2 -> S3",
            "\tS3 -> S4",
            "\tS4 -> S5",
            "\tS5",
            "}",
            ""
        );
        String state1 = String.join("\n",
            "",
            "S2",
            "----",
            "i: { 1 }",
            ""
        );
        String state2 = String.join("\n",
            "",
            "S5",
            "----",
            "i: { 7 }",
            ""
        );
        String history = String.join("\n",
            "",
            "S2 (-3)",
            "---------",
            "i: { 1 }",
            "",
            "S3 (-2)",
            "---------",
            "i: { 3 }",
            "",
            "S4 (-1)",
            "---------",
            "i: { 5 }",
            ""
        );

        assertTrue(sm.initialize(modelFile, false));
        assertTrue(sm.selectAlternatePath(false));
        assertTrue(sm.performStep(2));
        sm.performReverseStep(2);
        assertEquals(state1, sm.getCurrentStateString());
        assertEquals("", sm.getHistory(3));
        assertEquals(dotString1, sm.getDOTString());

        assertTrue(sm.performStep(3));
        assertEquals(state2, sm.getCurrentStateString());
        assertEquals(history, sm.getHistory(4));
        assertEquals(dotString2, sm.getDOTString());
    }

    @Test
    public void testPerformReverseStep_trace() throws IOException {
        File traceFile = createTrace();
        String expectedDOTString = String.join("\n",
            "digraph graphname {",
            "\tS1 -> S2",
            "\tS2",
            "}",
            ""
        );
        String expectedCurrentState = String.join("\n",
            "",
            "S1",
            "----",
            "a: { Off }",
            "b: { On }",
            ""
        );

        assertTrue(sm.initialize(traceFile, true));
        assertTrue(sm.isTrace());
        sm.performStep(1);
        sm.performReverseStep(1);
        assertEquals(expectedDOTString, sm.getDOTString());
        assertEquals(expectedCurrentState, sm.getCurrentStateString());
    }

    @Test
    public void testSelectAlternatePath() throws IOException {
        initializeTestWithModelPath("models/river_crossing.als");
        String expectedCurrentState = String.join("\n",
            "",
            "S3",
            "----",
            "far: {  }",
            "near: { Farmer, Fox, Grain }",
            ""
        );
        String expectedAlternateState = String.join("\n",
            "",
            "S4",
            "----",
            "far: { Grain }",
            "near: { Farmer, Fox }",
            ""
        );

        sm.initialize(modelFile, false);
        sm.performStep(2);
        assertEquals(expectedCurrentState, sm.getCurrentStateString());
        String history = sm.getHistory(3);
        assertTrue(sm.selectAlternatePath(false));
        assertEquals(expectedAlternateState, sm.getCurrentStateString());
        // History should still be unchanged.
        assertEquals(history, sm.getHistory(3));
    }

    @Test
    public void testSelectAlternatePath_atInit() throws IOException {
        initializeTestWithModelPath("models/even_odd.als");
        String expectedDOTString = String.join("\n",
            "digraph graphname {",
            "\tS1",
            "\tS2 -> S3",
            "\tS3",
            "}",
            ""
        );
        String expectedCurrentState = String.join("\n",
            "",
            "S1",
            "----",
            "i: { 0 }",
            ""
        );
        String expectedAlternateState = String.join("\n",
            "",
            "S2",
            "----",
            "i: { 1 }",
            ""
        );
        String expectedCurrentStateAfterStep = String.join("\n",
            "",
            "S3",
            "----",
            "i: { 3 }",
            ""
        );
        String expectedHistory = String.join("\n",
            "",
            "S2 (-1)",
            "---------",
            "i: { 1 }",
            ""
        );

        sm.initialize(modelFile, false);
        assertEquals(expectedCurrentState, sm.getCurrentStateString());
        assertTrue(sm.selectAlternatePath(false));
        assertEquals(expectedAlternateState, sm.getCurrentStateString());

        // No more alternate initial states.
        assertFalse(sm.selectAlternatePath(false));
        assertEquals(expectedAlternateState, sm.getCurrentStateString());
        assertTrue(sm.performStep(1));
        assertEquals(expectedDOTString, sm.getDOTString());
        assertEquals(expectedCurrentStateAfterStep, sm.getCurrentStateString());
        assertEquals(expectedHistory, sm.getHistory(3));
    }

    @Test
    public void testSelectAlternatePath_reverse() throws IOException {
        initializeTestWithModelPath("models/river_crossing.als");
        String expectedCurrentState = String.join("\n",
            "",
            "S3",
            "----",
            "far: {  }",
            "near: { Farmer, Fox, Grain }",
            ""
        );
        String expectedAlternateState = String.join("\n",
            "",
            "S4",
            "----",
            "far: { Grain }",
            "near: { Farmer, Fox }",
            ""
        );

        sm.initialize(modelFile, false);
        sm.performStep(2);
        assertEquals(expectedCurrentState, sm.getCurrentStateString());
        String history = sm.getHistory(3);
        assertTrue(sm.selectAlternatePath(false));
        assertTrue(sm.selectAlternatePath(false));
        assertTrue(sm.selectAlternatePath(true));
        assertEquals(expectedAlternateState, sm.getCurrentStateString());
        // History should still be unchanged.
        assertEquals(history, sm.getHistory(3));
    }

    @Test
    public void testSelectAlternatePath_noActiveSolutions() throws IOException {
        initializeTestWithModelPath("models/switch.als");
        sm.initialize(modelFile, false);
        assertFalse(sm.selectAlternatePath(false));
    }

    @Test
    public void testSelectAlternatePath_noValidAlternates() throws IOException {
        initializeTestWithModelPath("models/switch.als");
        sm.initialize(modelFile, false);
        sm.performStep(2);
        assertFalse(sm.selectAlternatePath(false));
    }

    @Test
    public void testSelectAlternatePath_reverse_noValidAlternates() throws IOException {
        initializeTestWithModelPath("models/switch.als");
        sm.initialize(modelFile, false);
        sm.performStep(2);
        sm.performReverseStep(1);
        assertFalse(sm.selectAlternatePath(true));
    }

    @Test
    public void testPerformUntil() throws IOException {
        initializeTestWithModelPath("models/switch.als");
        sm.initialize(modelFile, false);
        ConstraintManager cm = sm.getConstraintManager();
        String constraint = "a = Off";
        cm.addConstraint(constraint);
        String expectedCurrentState = String.join("\n",
            "",
            "S2",
            "----",
            "a: { Off }",
            "b: { On }",
            ""
        );

        assertTrue(sm.performUntil(10));
        assertEquals(expectedCurrentState, sm.getCurrentStateString());
    }

    @Test
    public void testSetToInit() throws IOException {
        initializeTestWithModelPath("models/even_odd.als");
        String dotString1 = String.join("\n",
            "digraph graphname {",
            "\tS1 -> S2",
            "\tS2 -> S3",
            "\tS3",
            "}",
            ""
        );
        String dotString2 = String.join("\n",
            "digraph graphname {",
            "\tS1 -> S2",
            "\tS2 -> S3",
            "\tS3",
            "\tS4",
            "}",
            ""
        );
        String dotString3 = String.join("\n",
            "digraph graphname {",
            "\tS1 -> S2",
            "\tS2 -> S3",
            "\tS3",
            "\tS4 -> S5",
            "\tS5",
            "}",
            ""
        );
        String state1 = String.join("\n",
            "",
            "S1",
            "----",
            "i: { 0 }",
            ""
        );
        String state2 = String.join("\n",
            "",
            "S4",
            "----",
            "i: { 1 }",
            ""
        );
        String state3 = String.join("\n",
            "",
            "S5",
            "----",
            "i: { 3 }",
            ""
        );

        assertTrue(sm.initialize(modelFile, false));
        assertEquals(state1, sm.getCurrentStateString());

        assertTrue(sm.performStep(2));
        assertTrue(sm.setToInit());
        assertEquals(state1, sm.getCurrentStateString());
        assertEquals("", sm.getHistory(3));
        assertEquals(dotString1, sm.getDOTString());

        assertTrue(sm.selectAlternatePath(false));
        assertEquals(state2, sm.getCurrentStateString());
        assertEquals("", sm.getHistory(3));
        assertEquals(dotString2, sm.getDOTString());

        assertTrue(sm.performStep(1));
        assertEquals(state3, sm.getCurrentStateString());
        assertEquals(dotString3, sm.getDOTString());
    }

    @Test
    public void testSetToInit_setToOriginalInitialState() throws IOException {
        initializeTestWithModelPath("models/even_odd.als");
        String dotString1 = String.join("\n",
            "digraph graphname {",
            "\tS1",
            "\tS2 -> S3",
            "\tS3",
            "}",
            ""
        );
        String state1 = String.join("\n",
            "",
            "S1",
            "----",
            "i: { 0 }",
            ""
        );
        String state2 = String.join("\n",
            "",
            "S2",
            "----",
            "i: { 1 }",
            ""
        );
        String state3 = String.join("\n",
            "",
            "S3",
            "----",
            "i: { 3 }",
            ""
        );

        assertTrue(sm.initialize(modelFile, false));
        assertEquals(state1, sm.getCurrentStateString());

        assertTrue(sm.selectAlternatePath(false));
        assertEquals(state2, sm.getCurrentStateString());

        assertTrue(sm.performStep(1));
        assertEquals(state3, sm.getCurrentStateString());

        assertTrue(sm.setToInit());
        assertEquals(state1, sm.getCurrentStateString());
        assertEquals("", sm.getHistory(3));
        assertEquals(dotString1, sm.getDOTString());
    }

    @Test
    public void testSetToInit_trace() throws IOException {
        File traceFile = createTrace();
        String expectedDOTString = String.join("\n",
            "digraph graphname {",
            "\tS1 -> S2",
            "\tS2",
            "}",
            ""
        );
        String state1 = String.join("\n",
            "",
            "S1",
            "----",
            "a: { Off }",
            "b: { On }",
            ""
        );
        String state2 = String.join("\n",
            "",
            "S2",
            "----",
            "a: { On }",
            "b: { Off }",
            ""
        );

        assertTrue(sm.initialize(traceFile, true));
        assertTrue(sm.isTrace());
        assertEquals(state1, sm.getCurrentStateString());

        assertTrue(sm.performStep(1));
        assertEquals(state2, sm.getCurrentStateString());

        assertTrue(sm.setToInit());
        assertEquals(state1, sm.getCurrentStateString());
        assertEquals("", sm.getHistory(3));
        assertEquals(expectedDOTString, sm.getDOTString());

        assertTrue(sm.performStep(1));
        assertEquals(state2, sm.getCurrentStateString());
    }

    @Test
    public void testValidateConstraint() throws IOException {
        initializeTestWithModelPath("models/switch.als");
        sm.initialize(modelFile, false);
        String constraint = "a = Off";
        assertTrue(sm.validateConstraint(constraint));
    }

    @Test
    public void testValidateConstraint_invalid() throws IOException {
        initializeTestWithModelPath("models/switch.als");
        sm.initialize(modelFile, false);
        String constraint = "ab = Off";
        assertFalse(sm.validateConstraint(constraint));
    }

    @Test
    public void testGetCurrentStateDiffStringFromLastCommit() throws IOException {
        initializeTestWithModelPath("models/musical_chairs.als");
        sm.initialize(modelFile, false);

        String expectedDiff = String.join("\n",
            "",
            "S1 -> S6",
            "------------",
            "chairs: { Chair_0, Chair_1 }",
            "players: { Player_0, Player_1, Player_3 }",
            ""
        );
        String expectedDiff2 = String.join("\n",
            "",
            "S6 -> S9",
            "------------",
            "mode: { sitting }",
            "occupied: { Chair_0->Player_3, Chair_1->Player_0 }",
            ""
        );
        String expectedDiff3 = String.join("\n",
            "",
            "S6 -> S10",
            "------------",
            "mode: { sitting }",
            "occupied: { Chair_0->Player_3, Chair_1->Player_1 }",
            ""
        );

        assertTrue(sm.performStep(3));
        assertTrue(sm.selectAlternatePath(false));
        assertEquals(expectedDiff, sm.getCurrentStateDiffStringFromLastCommit());

        assertTrue(sm.performStep(2));
        assertTrue(sm.selectAlternatePath(false));
        assertEquals(expectedDiff2, sm.getCurrentStateDiffStringFromLastCommit());

        assertTrue(sm.selectAlternatePath(false));
        assertEquals(expectedDiff3, sm.getCurrentStateDiffStringFromLastCommit());
    }

    @Test
    public void testGetCurrentStateDiffStringByDelta() throws IOException {
        initializeTestWithModelPath("models/musical_chairs.als");
        sm.initialize(modelFile, false);

        String expectedDiff = String.join("\n",
            "",
            "S1 -> S2",
            "------------",
            "mode: { walking }",
            ""
        );
        String expectedDiff2 = String.join("\n",
            "",
            "S2 -> S5",
            "------------",
            "chairs: { Chair_0, Chair_2 }",
            "players: { Player_0, Player_1, Player_3 }",
            ""
        );

        int steps = 1;
        assertTrue(sm.performStep(steps));
        assertEquals(expectedDiff, sm.getCurrentStateDiffStringByDelta(steps));

        steps = 3;
        assertTrue(sm.performStep(steps));
        assertEquals(expectedDiff2, sm.getCurrentStateDiffStringByDelta(steps));
    }

    private void initializeTestWithModelPath(String modelPath) throws IOException {
        File file = new File(modelPath);
        modelFile = tempFolder.newFile(String.format("test_%s.als", file.getName()));
        Files.copy(file.toPath(), modelFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
    }

    private void initializeTestWithModelString(String modelString) throws IOException {
        modelFile = tempFolder.newFile("test.als");
        Files.write(modelFile.toPath(), modelString.getBytes());
    }

    private File createTrace() throws IOException {
        initializeTestWithModelPath("models/switch.als");
        String alloyModelString = new String(Files.readAllBytes(modelFile.toPath()));

        String commandString = "\nrun {} for 4 State";
        Files.write(modelFile.toPath(), commandString.getBytes(), StandardOpenOption.APPEND);

        A4Solution sol = AlloyInterface.run(AlloyInterface.compile(modelFile.getPath()));
        File traceFile = tempFolder.newFile("test.xml");
        Map<String, String> sources = new HashMap<String, String>();
        sources.put(modelFile.getPath(), alloyModelString);
        sol.writeXML(traceFile.getPath(), null, sources);
        return traceFile;
    }
}
