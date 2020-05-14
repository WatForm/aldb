package commands;

import commands.InitCommand;
import simulation.SimulationManager;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import org.junit.Test;

public class TestInitCommand extends TestCommand {
    private final InitCommand initCommand = new InitCommand();
    private final SimulationManager simulationManager = mock(SimulationManager.class);

    @Test
    public void testGetName() {
        assertEquals(initCommand.getName(), CommandConstants.INIT_NAME);
    }

    @Test
    public void testGetDescription() {
        assertEquals(initCommand.getDescription(), CommandConstants.INIT_DESCRIPTION);
    }

    @Test
    public void testGetHelp() {
        assertEquals(initCommand.getHelp(), CommandConstants.INIT_HELP);
    }

    @Test
    public void testGetShorthand() {
        assertEquals(initCommand.getShorthand(), CommandConstants.INIT_SHORTHAND);
    }

    @Test
    public void testRequiresFile() {
        assertFalse(initCommand.requiresFile());
    }

    @Test
    public void testExecute_uninitialized() {
        setupStreams();
        when(simulationManager.isInitialized()).thenReturn(false);

        String[] input = {"init"};
        initCommand.execute(input, simulationManager);
        assertEquals(CommandConstants.NO_MODEL_LOADED + "\n", outContent.toString());
        verify(simulationManager, never()).setToInit();
        restoreStreams();
    }

    @Test
    public void testExecute() {
        setupStreams();
        when(simulationManager.isInitialized()).thenReturn(true);

        String[] input = {"init"};
        initCommand.execute(input, simulationManager);
        verify(simulationManager).setToInit();
        restoreStreams();
    }
}
