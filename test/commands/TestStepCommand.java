package commands;

import commands.StepCommand;
import simulation.SimulationManager;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import org.junit.Test;

public class TestStepCommand extends TestCommand {
    private final StepCommand step = new StepCommand();
    private final SimulationManager simulationManager = mock(SimulationManager.class);

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
    public void testExecute() {
        setupStreams();
        String state = "state";
        when(simulationManager.isInitialized()).thenReturn(true);
        doNothing().when(simulationManager).performStep(isA(Integer.class));
        when(simulationManager.getCurrentStateString()).thenReturn(state);

        String[] input = {"s", "3"};
        step.execute(input, simulationManager);
        verify(simulationManager).performStep(3);
        verify(simulationManager).getCurrentStateString();
        assertEquals(state + "\n", outContent.toString());
        restoreStreams();
    }

    @Test
    public void testExecute_isTrace() {
        setupStreams();
        String trace = "trace";
        when(simulationManager.isInitialized()).thenReturn(true);
        when(simulationManager.isTrace()).thenReturn(true);
        doNothing().when(simulationManager).performStep(isA(Integer.class));
        when(simulationManager.getCurrentStateDiffString()).thenReturn(trace);

        String[] input = {"s", "3"};
        step.execute(input, simulationManager);
        verify(simulationManager).performStep(3);
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
