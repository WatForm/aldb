package commands;

import commands.DotCommand;
import commands.CommandConstants;
import simulation.SimulationManager;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import org.junit.Rule;
import org.junit.rules.TemporaryFolder;
import org.junit.Test;

import java.io.File;
import java.io.IOException;

public class TestDotCommand extends TestCommand {
    private final DotCommand dot = new DotCommand();
    SimulationManager simulationManager = mock(SimulationManager.class);

    @Rule
    public TemporaryFolder tmpdir = new TemporaryFolder();

    @Test
    public void testGetName() {
        assertEquals(dot.getName(), CommandConstants.DOT_NAME);
    }

    @Test
    public void testGetDescription() {
        assertEquals(dot.getDescription(), CommandConstants.DOT_DESCRIPTION);
    }

    @Test
    public void testGetHelp() {
        assertEquals(dot.getHelp(), CommandConstants.DOT_HELP);
    }

    @Test
    public void testGetShorthand() {
        assertEquals(dot.getShorthand(), CommandConstants.DOT_SHORTHAND);
    }

    @Test
    public void testRequiresFile() {
        assertFalse(dot.requiresFile());
    }

    @Test
    public void testExecute() {
        setupStreams();
        when(simulationManager.isInitialized()).thenReturn(true);
        when(simulationManager.getWorkingDirPath()).thenReturn(tmpdir.getRoot().getAbsolutePath());
        when(simulationManager.getDOTString()).thenReturn("foo");

        String[] input = {"dot"};
        dot.execute(input, simulationManager);
        verify(simulationManager).getDOTString();
        assertTrue(outContent.toString().contains(CommandConstants.DONE));
        assertFalse(outContent.toString().contains(CommandConstants.IO_FAILED));
        restoreStreams();
    }

    @Test
    public void testExecute_uninitialized() {
        setupStreams();
        when(simulationManager.isInitialized()).thenReturn(false);

        String[] input = {"dot"};
        dot.execute(input, simulationManager);
        assertEquals(CommandConstants.NO_MODEL_LOADED + "\n", outContent.toString());
        restoreStreams();
    }
}
