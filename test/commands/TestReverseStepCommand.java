package commands;

import commands.ReverseStepCommand;
import simulation.SimulationManager;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import org.junit.Test;

public class TestReverseStepCommand extends TestCommand {
    private final ReverseStepCommand reverseStep = new ReverseStepCommand();
    SimulationManager simulationManager = mock(SimulationManager.class);

    @Test
    public void testGetName() {
        assertEquals(reverseStep.getName(), CommandConstants.REVERSE_STEP_NAME);
    }

    @Test
    public void testGetDescription() {
        assertEquals(reverseStep.getDescription(), CommandConstants.REVERSE_STEP_DESCRIPTION);
    }

    @Test
    public void testGetHelp() {
        assertEquals(reverseStep.getHelp(), CommandConstants.REVERSE_STEP_HELP);
    }

    @Test
    public void testRequiresFile() {
        assertFalse(reverseStep.requiresFile());
    }

    @Test
    public void testExecute() {
        when(simulationManager.isInitialized()).thenReturn(true);
        doNothing().when(simulationManager).performReverseStep(isA(Integer.class));
        String[] input = {"rs", "3"};
        reverseStep.execute(input, simulationManager);
        verify(simulationManager).performReverseStep(3);
    }

    @Test
    public void testExecute_uninitialized() {
        setupStreams();
        when(simulationManager.isInitialized()).thenReturn(false);
        String[] input = {"rs", "0"};
        reverseStep.execute(input, simulationManager);
        assertEquals(CommandConstants.NO_MODEL_LOADED + "\n", outContent.toString());
        restoreStreams();
    }

    @Test
    public void testExecute_lessThanOne() {
        setupStreams();
        when(simulationManager.isInitialized()).thenReturn(true);
        String[] input = {"rs", "0"};
        reverseStep.execute(input, simulationManager);
        assertEquals(CommandConstants.GR_ONE_ERROR + "\n", outContent.toString());
        restoreStreams();
    }

    @Test
    public void testExecute_invalidInteger() throws NumberFormatException {
        setupStreams();
        when(simulationManager.isInitialized()).thenReturn(true);
        String[] input = {"rs", "not_an_integer"};
        reverseStep.execute(input, simulationManager);
        assertEquals(CommandConstants.INTEGER_ERROR + "\n", outContent.toString());
        restoreStreams();
    }
}
