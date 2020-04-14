package commands;

import alloy.ParsingConf;
import commands.SetConfCommand;
import simulation.SimulationManager;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import org.junit.Test;
import org.yaml.snakeyaml.error.YAMLException;

public class TestSetConfCommand extends TestCommand {
    private final SetConfCommand setConf = new SetConfCommand();
    private final SimulationManager simulationManager = mock(SimulationManager.class);

    @Test
    public void testGetName() {
        assertEquals(setConf.getName(), CommandConstants.SETCONF_NAME);
    }

    @Test
    public void testGetDescription() {
        assertEquals(setConf.getDescription(), CommandConstants.SETCONF_DESCRIPTION);
    }

    @Test
    public void testGetHelp() {
        assertEquals(setConf.getHelp(), CommandConstants.SETCONF_HELP);
    }

    @Test
    public void testRequiresFile() {
        assertTrue(setConf.requiresFile());
    }

    @Test
    public void testExecute() throws IOException {
        setupStreams();
        File file = createFileWithContent("transitionConstraintName: trans");
        doNothing().when(simulationManager).setParsingConf(isA(ParsingConf.class));

        String[] input = {"setconf", file.getPath()};
        setConf.execute(input, simulationManager);
        verify(simulationManager).setParsingConf(any(ParsingConf.class));
        String msg = String.format(CommandConstants.SETTING_PARSING_OPTIONS_FROM, input[1]);
        msg += CommandConstants.DONE + "\n";
        assertEquals(msg, outContent.toString());
        file.delete();
        restoreStreams();
    }

    @Test
    public void testExecute_setDefault() {
        setupStreams();
        doNothing().when(simulationManager).setParsingConf(isA(ParsingConf.class));
        String[] input = {"setconf"};
        setConf.execute(input, simulationManager);
        verify(simulationManager).setParsingConf(any(ParsingConf.class));
        String msg = CommandConstants.SETTING_PARSING_OPTIONS;
        msg += CommandConstants.DONE + "\n";
        assertEquals(msg, outContent.toString());
        restoreStreams();
    }

    @Test
    public void testExecute_invalidFile() throws IOException {
        setupStreams();
        String[] input = {"setconf", "does_not_exist.txt"};
        setConf.execute(input, simulationManager);
        String errMsg = String.format(CommandConstants.SETTING_PARSING_OPTIONS_FROM, input[1]);
        errMsg += CommandConstants.FAILED_TO_READ_FILE + "\n";
        assertEquals(errMsg, outContent.toString());
        verifyZeroInteractions(simulationManager);
        restoreStreams();
    }

    @Test
    public void testExecute_invalidYaml() throws IOException, YAMLException {
        setupStreams();
        File file = createFileWithContent("invalid yaml");

        String[] input = {"setconf", file.getPath()};
        setConf.execute(input, simulationManager);
        String errMsg = String.format(CommandConstants.SETTING_PARSING_OPTIONS_FROM, input[1]);
        errMsg += CommandConstants.FAILED_TO_READ_CONF + "\n";
        assertEquals(errMsg, outContent.toString());
        verifyZeroInteractions(simulationManager);
        file.delete();
        restoreStreams();
    }
}
