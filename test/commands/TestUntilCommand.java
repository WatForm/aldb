package commands;

import commands.UntilCommand;
import simulation.SimulationManager;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import org.junit.Test;

public class TestUntilCommand extends TestCommand {
    private final UntilCommand until = new UntilCommand();
    SimulationManager simulationManager = mock(SimulationManager.class);

    @Test
    public void testGetName() {
        assertEquals(until.getName(), CommandConstants.UNTIL_NAME);
    }

    @Test
    public void testGetDescription() {
        assertEquals(until.getDescription(), CommandConstants.UNTIL_DESCRIPTION);
    }

    @Test
    public void testGetHelp() {
        assertEquals(until.getHelp(), CommandConstants.UNTIL_HELP);
    }

    @Test
    public void testRequiresFile() {
        assertFalse(until.requiresFile());
    }

    @Test
    public void testExecute() {
        setupStreams();
        String curState = "Current state";
        when(simulationManager.isInitialized()).thenReturn(true);
        when(simulationManager.performUntil(anyInt())).thenReturn(true);
        when(simulationManager.getCurrentStateString()).thenReturn(curState);

        String[] input = {"u", "3"};
        until.execute(input, simulationManager);
        verify(simulationManager).performUntil(3);
        verify(simulationManager).getCurrentStateString();
        assertEquals(curState + "\n", outContent.toString());
        restoreStreams();
    }

    @Test
    public void testExecute_failure() {
        setupStreams();
        when(simulationManager.isInitialized()).thenReturn(true);
        when(simulationManager.performUntil(anyInt())).thenReturn(false);

        String[] input = {"u", "3"};
        until.execute(input, simulationManager);
        verify(simulationManager).performUntil(3);
        assertEquals(CommandConstants.UNTIL_FAILED + "\n", outContent.toString());
        restoreStreams();
    }

    @Test
    public void testExecute_uninitialized() {
        setupStreams();
        when(simulationManager.isInitialized()).thenReturn(false);
        String[] input = {"u", "1"};
        until.execute(input, simulationManager);
        assertEquals(CommandConstants.NO_MODEL_LOADED + "\n", outContent.toString());
        restoreStreams();
    }

    @Test
    public void testExecute_lessThanOne() {
        setupStreams();
        when(simulationManager.isInitialized()).thenReturn(true);
        String[] input = {"u", "0"};
        until.execute(input, simulationManager);
        assertEquals(CommandConstants.GR_ONE_ERROR + "\n", outContent.toString());
        restoreStreams();
    }

    @Test
    public void testExecute_invalidInteger() throws NumberFormatException {
        setupStreams();
        when(simulationManager.isInitialized()).thenReturn(true);
        String[] input = {"u", "not_an_integer"};
        until.execute(input, simulationManager);
        assertEquals(CommandConstants.INTEGER_ERROR + "\n", outContent.toString());
        restoreStreams();
    }
}
