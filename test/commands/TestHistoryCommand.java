package commands;

import commands.HistoryCommand;
import simulation.SimulationManager;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import org.junit.Test;

public class TestHistoryCommand extends TestCommand {
    private final HistoryCommand history = new HistoryCommand();
    private final SimulationManager simulationManager = mock(SimulationManager.class);

    @Test
    public void testGetName() {
        assertEquals(history.getName(), CommandConstants.HISTORY_NAME);
    }

    @Test
    public void testGetDescription() {
        assertEquals(history.getDescription(), CommandConstants.HISTORY_DESCRIPTION);
    }

    @Test
    public void testGetHelp() {
        assertEquals(history.getHelp(), CommandConstants.HISTORY_HELP);
    }

    @Test
    public void testRequiresFile() {
        assertFalse(history.requiresFile());
    }

    @Test
    public void testExecute() {
        setupStreams();
        String response = "history";
        when(simulationManager.isInitialized()).thenReturn(true);
        when(simulationManager.getHistory(anyInt())).thenReturn(response);
        String[] input = {"history", "5"};
        history.execute(input, simulationManager);
        verify(simulationManager).getHistory(5);
        assertEquals(response + "\n", outContent.toString());
        restoreStreams();
    }

    @Test
    public void testExecute_lessThanOne() {
        setupStreams();
        when(simulationManager.isInitialized()).thenReturn(true);
        String[] input = {"history", "0"};
        history.execute(input, simulationManager);
        assertEquals(CommandConstants.GR_ONE_ERROR + "\n", outContent.toString());
        restoreStreams();
    }

    @Test
    public void testExecute_invalidInteger() throws NumberFormatException {
        setupStreams();
        when(simulationManager.isInitialized()).thenReturn(true);
        String[] input = {"history", "not_an_integer"};
        history.execute(input, simulationManager);
        assertEquals(CommandConstants.INTEGER_ERROR + "\n", outContent.toString());
        restoreStreams();
    }

    @Test
    public void testExecute_uninitialized() {
        setupStreams();
        when(simulationManager.isInitialized()).thenReturn(false);

        String[] input = {"history"};
        history.execute(input, simulationManager);
        assertEquals(CommandConstants.NO_MODEL_LOADED + "\n", outContent.toString());
        restoreStreams();
    }
}
