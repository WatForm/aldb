package commands;

import commands.CurrentCommand;
import simulation.SimulationManager;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import org.junit.Test;

public class TestCurrentCommand extends TestCommand {
    private final CurrentCommand current = new CurrentCommand();
    SimulationManager simulationManager = mock(SimulationManager.class);

    @Test
    public void testGetName() {
        assertEquals(current.getName(), CommandConstants.CURRENT_NAME);
    }

    @Test
    public void testGetDescription() {
        assertEquals(current.getDescription(), CommandConstants.CURRENT_DESCRIPTION);
    }

    @Test
    public void testGetHelp() {
        assertEquals(current.getHelp(), CommandConstants.CURRENT_HELP);
    }

    @Test
    public void testGetShorthand() {
        assertEquals(current.getShorthand(), CommandConstants.CURRENT_SHORTHAND);
    }

    @Test
    public void testRequiresFile() {
        assertFalse(current.requiresFile());
    }

    @Test
    public void testExecute() {
        setupStreams();
        String res = "curr state";
        when(simulationManager.isInitialized()).thenReturn(true);
        when(simulationManager.getCurrentStateString()).thenReturn(res);

        String[] input = {"c"};
        current.execute(input, simulationManager);
        verify(simulationManager).getCurrentStateString();
        assertEquals(res + "\n", outContent.toString());
        restoreStreams();
    }

    @Test
    public void testExecute_specificProperty() {
        setupStreams();
        String res = "property response";
        when(simulationManager.isInitialized()).thenReturn(true);
        when(simulationManager.getCurrentStateStringForProperty(anyString())).thenReturn(res);

        String[] input = {"c", "property"};
        current.execute(input, simulationManager);
        verify(simulationManager).getCurrentStateStringForProperty(input[1]);
        assertEquals(res + "\n", outContent.toString());
        restoreStreams();
    }

    @Test
    public void testExecute_uninitialized() {
        setupStreams();
        when(simulationManager.isInitialized()).thenReturn(false);
        String[] input = {"c", "0"};
        current.execute(input, simulationManager);
        assertEquals(CommandConstants.NO_MODEL_LOADED + "\n", outContent.toString());
        restoreStreams();
    }
}
