package commands;

import commands.TraceCommand;
import simulation.SimulationManager;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import org.junit.Test;
import java.io.File;
import java.io.IOException;

public class TestTraceCommand extends TestCommand {
    private final TraceCommand trace = new TraceCommand();
    private final SimulationManager simulationManager = mock(SimulationManager.class);

    @Test
    public void testGetName() {
        assertEquals(trace.getName(), CommandConstants.TRACE_NAME);
    }

    @Test
    public void testGetDescription() {
        assertEquals(trace.getDescription(), CommandConstants.TRACE_DESCRIPTION);
    }

    @Test
    public void testGetHelp() {
        assertEquals(trace.getHelp(), CommandConstants.TRACE_HELP);
    }

    @Test
    public void testRequiresFile() {
        assertTrue(trace.requiresFile());
    }

    @Test
    public void testExecute_invalidInputLength() {
        setupStreams();
        String[] input = {"t"};
        trace.execute(input, simulationManager);
        String errMsg = CommandConstants.NO_FILE_SPECIFIED + "\n";
        assertEquals(errMsg, outContent.toString());
        restoreStreams();
    }

    @Test
    public void testExecute_invalidFile() {
        setupStreams();
        String[] input = {"t", "does_not_exist.txt"};
        trace.execute(input, simulationManager);
        String errMsg = String.format(CommandConstants.NO_SUCH_FILE, input[1]);
        assertEquals(errMsg, outContent.toString());
        restoreStreams();
    }

    @Test
    public void testExecute() throws IOException {
        setupStreams();
        when(simulationManager.initialize(any(File.class), eq(true))).thenReturn(true);
        File file = createFileWithContent("");

        String[] input = {"t", file.getPath()};
        trace.execute(input, simulationManager);
        String msg = String.format(CommandConstants.READING_TRACE, input[1]);
        msg += CommandConstants.DONE + "\n";
        assertEquals(msg, outContent.toString());
        file.delete();
        restoreStreams();
    }

    @Test
    public void testExecute_invalidTrace() throws IOException {
        setupStreams();
        when(simulationManager.initialize(any(File.class), eq(true))).thenReturn(false);
        File file = createFileWithContent("");

        String[] input = {"t", file.getPath()};
        trace.execute(input, simulationManager);
        String msg = String.format(CommandConstants.READING_TRACE, input[1]);
        msg += CommandConstants.INVALID_TRACE + "\n";
        assertEquals(msg, outContent.toString());
        file.delete();
        restoreStreams();
    }
}
