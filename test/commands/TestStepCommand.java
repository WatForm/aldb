package commands;

import commands.StepCommand;
import simulation.AliasManager;
import simulation.SimulationManager;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class TestStepCommand extends TestCommand {
    private final StepCommand step = new StepCommand();
    private final SimulationManager simulationManager = mock(SimulationManager.class);
    private final AliasManager am = mock(AliasManager.class);

    @Test
    public void testGetName() {
        assertEquals(step.getName(), CommandConstants.STEP_NAME);
    }

    @Test
    public void testGetDescription() {
        assertEquals(step.getDescription(), CommandConstants.STEP_DESCRIPTION);
    }

    @Test
    public void testGetHelp() {
        assertEquals(step.getHelp(), CommandConstants.STEP_HELP);
    }

    @Test
    public void testRequiresFile() {
        assertFalse(step.requiresFile());
    }

    @Test
    public void testExecute_integer() {
        setupStreams();
        String state = "state";
        when(simulationManager.isInitialized()).thenReturn(true);
        when(simulationManager.performStep(anyInt(), anyList())).thenReturn(true);
        when(simulationManager.getCurrentStateString()).thenReturn(state);

        String[] input = {"s", "3"};
        step.execute(input, simulationManager);
        verify(simulationManager).performStep(3, new ArrayList<String>());
        verify(simulationManager).getCurrentStateString();
        assertEquals(state + "\n", outContent.toString());
        restoreStreams();
    }

    @Test
    public void testExecute_constraints() {
        setupStreams();
        String state = "state";

        when(simulationManager.performStep(anyInt(), anyList())).thenReturn(true);
        when(simulationManager.isInitialized()).thenReturn(true);
        when(simulationManager.getAliasManager()).thenReturn(am);
        when(simulationManager.getCurrentStateString()).thenReturn(state);
        when(simulationManager.validateConstraint(anyString())).thenReturn(true);

        String rawInput = "s [a, \"b and c\", d, \"e\"]";
        int expectedSteps = 4;
        List<String> expectedConstraints = new ArrayList<String>(
            Arrays.asList("a", "b and c", "d", "e")
        );

        step.execute(rawInput.split(" "), simulationManager);
        verify(simulationManager).performStep(expectedSteps, expectedConstraints);
        verify(simulationManager).getCurrentStateString();
        assertEquals(state + "\n", outContent.toString());
        restoreStreams();
    }

    @Test
    public void testExecute_constraintsEmpty() {
        setupStreams();
        String state = "state";

        when(simulationManager.performStep(anyInt(), anyList())).thenReturn(true);
        when(simulationManager.isInitialized()).thenReturn(true);
        when(simulationManager.getAliasManager()).thenReturn(am);
        when(simulationManager.getCurrentStateString()).thenReturn(state);
        when(simulationManager.validateConstraint(anyString())).thenReturn(true);

        String rawInput = "s []";
        int expectedSteps = 1;
        List<String> expectedConstraints = new ArrayList<String>(
            Arrays.asList("")
        );

        step.execute(rawInput.split(" "), simulationManager);
        verify(simulationManager).performStep(expectedSteps, expectedConstraints);
        verify(simulationManager).getCurrentStateString();
        assertEquals(state + "\n", outContent.toString());
        restoreStreams();
    }

    @Test
    public void testExecute_constraintsBlank() {
        setupStreams();
        String state = "state";

        when(simulationManager.performStep(anyInt(), anyList())).thenReturn(true);
        when(simulationManager.isInitialized()).thenReturn(true);
        when(simulationManager.getAliasManager()).thenReturn(am);
        when(simulationManager.getCurrentStateString()).thenReturn(state);
        when(simulationManager.validateConstraint(anyString())).thenReturn(true);

        String rawInput = "s [\"\", ,, \"\",]";
        int expectedSteps = 5;
        List<String> expectedConstraints = new ArrayList<String>(
            Arrays.asList("", "", "", "", "")
        );

        step.execute(rawInput.split(" "), simulationManager);
        verify(simulationManager).performStep(expectedSteps, expectedConstraints);
        verify(simulationManager).getCurrentStateString();
        assertEquals(state + "\n", outContent.toString());
        restoreStreams();
    }

    @Test
    public void testExecute_constraintsAlias() {
        setupStreams();
        String state = "state";

        when(simulationManager.performStep(anyInt(), anyList())).thenReturn(true);
        when(simulationManager.isInitialized()).thenReturn(true);
        when(simulationManager.getAliasManager()).thenReturn(am);
        when(simulationManager.getCurrentStateString()).thenReturn(state);
        when(simulationManager.validateConstraint(anyString())).thenReturn(true);

        when(am.isAlias(anyString())).thenReturn(true);
        when(am.getFormula(anyString())).thenReturn("a");

        String rawInput = "s [f1]";
        int expectedSteps = 1;
        List<String> expectedConstraints = new ArrayList<String>(
            Arrays.asList("a")
        );

        step.execute(rawInput.split(" "), simulationManager);
        verify(simulationManager).performStep(expectedSteps, expectedConstraints);
        verify(simulationManager).getCurrentStateString();
        assertEquals(state + "\n", outContent.toString());
        restoreStreams();
    }

    @Test
    public void testExecute_constraintsInvalid() {
        setupStreams();
        String state = "state";

        when(simulationManager.performStep(anyInt(), anyList())).thenReturn(true);
        when(simulationManager.isInitialized()).thenReturn(true);
        when(simulationManager.getAliasManager()).thenReturn(am);
        when(simulationManager.getCurrentStateString()).thenReturn(state);
        when(simulationManager.validateConstraint(anyString())).thenReturn(false);

        String rawInput = "s [a]";
        String expectedOutput = String.format(CommandConstants.INVALID_CONSTRAINT, "a") + "\n";

        step.execute(rawInput.split(" "), simulationManager);
        assertEquals(expectedOutput, outContent.toString());
        restoreStreams();
    }

    @Test
    public void testExecute_isTrace() {
        setupStreams();
        String trace = "trace";
        when(simulationManager.isInitialized()).thenReturn(true);
        when(simulationManager.isTrace()).thenReturn(true);
        when(simulationManager.performStep(anyInt(), anyList())).thenReturn(true);
        when(simulationManager.getCurrentStateDiffString()).thenReturn(trace);

        String[] input = {"s", "3"};
        step.execute(input, simulationManager);
        verify(simulationManager).performStep(3, new ArrayList<String>());
        verify(simulationManager).getCurrentStateDiffString();
        assertEquals(trace + "\n", outContent.toString());
        restoreStreams();
    }

    @Test
    public void testExecute_uninitialized() {
        setupStreams();
        when(simulationManager.isInitialized()).thenReturn(false);

        String[] input = {"s", "0"};
        step.execute(input, simulationManager);
        assertEquals(CommandConstants.NO_MODEL_LOADED + "\n", outContent.toString());
        restoreStreams();
    }

    @Test
    public void testExecute_lessThanOne() {
        setupStreams();
        when(simulationManager.isInitialized()).thenReturn(true);

        String[] input = {"s", "0"};
        step.execute(input, simulationManager);
        assertEquals(CommandConstants.GR_ONE_ERROR + "\n", outContent.toString());
        restoreStreams();
    }

    @Test
    public void testExecute_invalidInteger() throws NumberFormatException {
        setupStreams();
        when(simulationManager.isInitialized()).thenReturn(true);

        String[] input = {"s", "not_an_integer"};
        step.execute(input, simulationManager);
        assertEquals(CommandConstants.INTEGER_ERROR + "\n", outContent.toString());
        restoreStreams();
    }
}
