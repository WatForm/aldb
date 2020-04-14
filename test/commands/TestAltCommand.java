package commands;

import commands.AltCommand;
import simulation.SimulationManager;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import org.junit.Test;

public class TestAltCommand extends TestCommand {
    private final AltCommand alt = new AltCommand();
    private final SimulationManager simulationManager = mock(SimulationManager.class);

    @Test
    public void testGetName() {
        assertEquals(alt.getName(), CommandConstants.ALT_NAME);
    }

    @Test
    public void testGetDescription() {
        assertEquals(alt.getDescription(), CommandConstants.ALT_DESCRIPTION);
    }

    @Test
    public void testGetHelp() {
        assertEquals(alt.getHelp(), CommandConstants.ALT_HELP);
    }

    @Test
    public void testGetShorthand() {
        assertEquals(alt.getShorthand(), CommandConstants.ALT_SHORTHAND);
    }

    @Test
    public void testRequiresFile() {
        assertFalse(alt.requiresFile());
    }

    @Test
    public void testExecute() {
        setupStreams();
        String res = "Response";
        when(simulationManager.isInitialized()).thenReturn(true);
        when(simulationManager.selectAlternatePath(anyBoolean())).thenReturn(true);
        when(simulationManager.getCurrentStateString()).thenReturn(res);

        String[] input = {"alt"};
        alt.execute(input, simulationManager);
        verify(simulationManager).getCurrentStateString();
        assertEquals(res + "\n", outContent.toString());
        restoreStreams();
    }

    @Test
    public void testExecute_invalidInput() {
        setupStreams();
        when(simulationManager.isInitialized()).thenReturn(true);
        String[] input = {"alt", "random"};
        String errMsg = CommandConstants.ALT_HELP + "\n";
        alt.execute(input, simulationManager);
        assertEquals(errMsg, outContent.toString());
        restoreStreams();
    }

    @Test
    public void testExecute_noAlternatePaths() {
        setupStreams();
        when(simulationManager.isInitialized()).thenReturn(true);
        when(simulationManager.selectAlternatePath(anyBoolean())).thenReturn(false);
        String[] input = {"alt", "-r"};
        String errMsg = CommandConstants.ALT_UNAVAILABLE + "\n";
        alt.execute(input, simulationManager);
        assertEquals(errMsg, outContent.toString());
        restoreStreams();
    }

    @Test
    public void testExecute_uninitialized() {
        setupStreams();
        when(simulationManager.isInitialized()).thenReturn(false);

        String[] input = {"alt"};
        alt.execute(input, simulationManager);
        assertEquals(CommandConstants.NO_MODEL_LOADED + "\n", outContent.toString());
        restoreStreams();
    }
}
