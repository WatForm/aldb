package simulation;

import alloy.AlloyInterface;
import alloy.ParsingConf;

import edu.mit.csail.sdg.translator.A4Solution;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.Before;
import org.junit.Rule;
import org.junit.rules.TemporaryFolder;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TestSimulationManager {
    private SimulationManager sm;
    private File modelFile;

    @Rule
    public TemporaryFolder tempFolder = new TemporaryFolder();

    @Before
    public void init() {
        sm = new SimulationManager();
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
        assertTrue(sm.initializeWithModel(modelFile));
        assertTrue(sm.isInitialized());
        assertFalse(sm.isTrace());
        assertEquals(expectedDOTString, sm.getDOTString());
    }

    @Test
    public void testInitializeWithModel_nonexistantFile() {
        File nonexistantFile = new File("nonexistant-file");
        assertFalse(sm.initializeWithModel(nonexistantFile));
        assertFalse(sm.isInitialized());
    }

    @Test
    public void testInitializeWithModel_noInitPredicate() throws IOException {
        String noInitModel = String.join("\n",
            "sig State {}",
            "pred next[s, s': State] {}",
            ""
        );
        initializeTestWithModelString(noInitModel);
        assertFalse(sm.initializeWithModel(modelFile));
        assertFalse(sm.isInitialized());
    }

    @Test
    public void testInitializeWithModel_noNextPredicate() throws IOException {
        String noNextModel = String.join("\n",
            "sig State {}",
            "pred init[s: State] {}",
            ""
        );
        initializeTestWithModelString(noNextModel);
        assertFalse(sm.initializeWithModel(modelFile));
        assertFalse(sm.isInitialized());
    }

    @Test
    public void testInitializeWithModel_syntaxError_incompletePredicate() throws IOException {
        String invalidModel = String.join("\n",
            "sig State {}",
            "pred init[s: State] {",
            ""
        );
        initializeTestWithModelString(invalidModel);
        assertFalse(sm.initializeWithModel(modelFile));
        assertFalse(sm.isInitialized());
    }

    @Test
    public void testInitializeWithModel_historyDeleted() throws IOException {
        initializeTestWithModelPath("models/switch.als");
        assertTrue(sm.initializeWithModel(modelFile));
        assertTrue(sm.performStep(1));

        initializeTestWithModelPath("models/even_odd.als");
        assertTrue(sm.initializeWithModel(modelFile));
        assertEquals("", sm.getHistory(3));
    }

    @Test
    public void testInitializeWithTrace() throws IOException {
        File traceFile = createTrace();

        assertTrue(sm.initializeWithTrace(traceFile));
        assertTrue(sm.isTrace());
    }

    @Test
    public void testInitializeWithTrace_invalidXML() throws IOException {
        initializeTestWithModelPath("models/switch.als");
        assertFalse(sm.initializeWithTrace(modelFile));
        assertFalse(sm.isTrace());
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
            "(-2)",
            "----",
            "S1",
            "----",
            "a: { On }",
            "b: { Off }",
            "",
            "(-1)",
            "----",
            "S2",
            "----",
            "a: { Off }",
            "b: { On }",
            ""
        );
        sm.initializeWithModel(modelFile);
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
            "(-2)",
            "----",
            "S1",
            "----",
            "chairs: { Chair_0, Chair_1, Chair_2 }",
            "mode: { start }",
            "occupied: {  }",
            "players: { Player_0, Player_1, Player_2, Player_3 }",
            "",
            "(-1)",
            "----",
            "S2",
            "----",
            "chairs: { Chair_0, Chair_1, Chair_2 }",
            "mode: { walking }",
            "occupied: {  }",
            "players: { Player_0, Player_1, Player_2, Player_3 }",
            ""
        );

        sm.setParsingConf(pc);
        assertTrue(sm.initializeWithModel(modelFile));
        sm.performStep(2);
        assertEquals(expectedDOTString, sm.getDOTString());
        assertEquals(expectedCurrentState, sm.getCurrentStateString());
        assertEquals(expectedHistory, sm.getHistory(2));
    }

    @Test
    public void testPerformStep_modelWithIntScope() throws IOException {
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
            "(-2)",
            "----",
            "S9",
            "----",
            "i: { 16 }",
            "",
            "(-1)",
            "----",
            "S10",
            "----",
            "i: { 18 }",
            ""
        );

        assertTrue(sm.initializeWithModel(modelFile));
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
            "(-1)",
            "----",
            "S1",
            "----",
            "a: { Off }",
            "b: { On }",
            ""
        );

        assertTrue(sm.initializeWithTrace(traceFile));
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
            "(-2)",
            "----",
            "S1",
            "----",
            "far: {  }",
            "near: { Chicken, Farmer, Fox, Grain }",
            "",
            "(-1)",
            "----",
            "S2",
            "----",
            "far: { Chicken, Farmer }",
            "near: { Fox, Grain }",
            ""
        );

        List<String> constraints = new ArrayList<String>(
            Arrays.asList("Chicken in far", "(Farmer in near) and (Chicken in far)")
        );
        sm.initializeWithModel(modelFile);
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
        sm.initializeWithModel(modelFile);
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
            "(-2)",
            "----",
            "S1",
            "----",
            "a: { On }",
            "b: { Off }",
            "",
            "(-1)",
            "----",
            "S2",
            "----",
            "a: { Off }",
            "b: { On }",
            ""
        );
        sm.initializeWithModel(modelFile);
        sm.performStep(4);
        sm.performReverseStep(2);
        assertEquals(expectedDOTString, sm.getDOTString());
        assertEquals(expectedCurrentState, sm.getCurrentStateString());
        assertEquals(expectedHistory, sm.getHistory(2));
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

        assertTrue(sm.initializeWithTrace(traceFile));
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

        sm.initializeWithModel(modelFile);
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
            "(-1)",
            "----",
            "S2",
            "----",
            "i: { 1 }",
            ""
        );

        sm.initializeWithModel(modelFile);
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

        sm.initializeWithModel(modelFile);
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
        sm.initializeWithModel(modelFile);
        assertFalse(sm.selectAlternatePath(false));
    }

    @Test
    public void testSelectAlternatePath_noValidAlternates() throws IOException {
        initializeTestWithModelPath("models/switch.als");
        sm.initializeWithModel(modelFile);
        sm.performStep(2);
        assertFalse(sm.selectAlternatePath(false));
    }

    @Test
    public void testSelectAlternatePath_reverse_noValidAlternates() throws IOException {
        initializeTestWithModelPath("models/switch.als");
        sm.initializeWithModel(modelFile);
        sm.performStep(2);
        sm.performReverseStep(1);
        assertFalse(sm.selectAlternatePath(true));
    }

    @Test
    public void testPerformUntil() throws IOException {
        initializeTestWithModelPath("models/switch.als");
        sm.initializeWithModel(modelFile);
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

        assertTrue(sm.initializeWithModel(modelFile));
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

        assertTrue(sm.initializeWithModel(modelFile));
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

        assertTrue(sm.initializeWithTrace(traceFile));
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
        sm.initializeWithModel(modelFile);
        String constraint = "a = Off";
        assertTrue(sm.validateConstraint(constraint));
    }

    @Test
    public void testValidateConstraint_invalid() throws IOException {
        initializeTestWithModelPath("models/switch.als");
        sm.initializeWithModel(modelFile);
        String constraint = "ab = Off";
        assertFalse(sm.validateConstraint(constraint));
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
