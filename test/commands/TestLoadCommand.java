package commands;

import commands.LoadCommand;
import simulation.SimulationManager;

import java.io.File;
import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import org.junit.Test;

public class TestLoadCommand extends TestCommand {
    private final LoadCommand load = new LoadCommand();
    private final SimulationManager simulationManager = mock(SimulationManager.class);

    @Test
    public void testGetName() {
        assertEquals(load.getName(), CommandConstants.LOAD_NAME);
    }

    @Test
    public void testGetDescription() {
        assertEquals(load.getDescription(), CommandConstants.LOAD_DESCRIPTION);
    }

    @Test
    public void testGetHelp() {
        assertEquals(load.getHelp(), CommandConstants.LOAD_HELP);
    }

    @Test
    public void testGetShorthand() {
        assertEquals(load.getShorthand(), CommandConstants.LOAD_SHORTHAND);
    }

    @Test
    public void testRequiresFile() {
        assertTrue(load.requiresFile());
    }

    @Test
    public void testExecute() throws IOException {
        setupStreams();
        File file = createFileWithContent("content");
        when(simulationManager.initializeWithModel(any(File.class))).thenReturn(true);

        String[] input = {"load", file.getPath()};
        load.execute(input, simulationManager);
        String msg = String.format(CommandConstants.READING_MODEL, input[1]);
        msg += CommandConstants.DONE + "\n";
        assertEquals(msg, outContent.toString());
        verify(simulationManager).initializeWithModel(any(File.class));
        file.delete();
        restoreStreams();
    }

    @Test
    public void testExecute_embeddedConf() throws IOException {
        setupStreams();
        String content = String.join("\n",
            "/*  BEGIN_ALDB_CONF",
            " *  transitionRelationName: trans",
            " *  END_ALDB_CONF",
            " */",
            "some alloy model here"
        );
        File file = createFileWithContent(content);
        when(simulationManager.initializeWithModel(any(File.class))).thenReturn(true);

        String[] input = {"load", file.getPath()};
        load.execute(input, simulationManager);
        String msg = String.format(CommandConstants.READING_MODEL, input[1]);
        msg += CommandConstants.DONE + "\n";
        assertEquals(msg, outContent.toString());
        verify(simulationManager).initializeWithModel(any(File.class));
        file.delete();
        restoreStreams();
    }

    @Test
    public void testExecute_noFile() {
        setupStreams();
        String[] input = {"load", "no_file"};
        String errMsg = String.format(CommandConstants.NO_SUCH_FILE, input[1]);
        load.execute(input, simulationManager);
        assertEquals(errMsg, outContent.toString());
        verifyZeroInteractions(simulationManager);
        restoreStreams();
    }
}
