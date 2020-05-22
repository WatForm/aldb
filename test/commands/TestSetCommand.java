package commands;

import alloy.ParsingConf;
import commands.SetCommand;
import simulation.SimulationManager;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import org.junit.Test;
import org.yaml.snakeyaml.error.YAMLException;

public class TestSetCommand extends TestCommand {
    private final SetCommand set = new SetCommand();
    private final SimulationManager simulationManager = mock(SimulationManager.class);

    @Test
    public void testGetName() {
        assertEquals(set.getName(), CommandConstants.SET_NAME);
    }

    @Test
    public void testGetDescription() {
        assertEquals(set.getDescription(), CommandConstants.SET_DESCRIPTION);
    }

    @Test
    public void testGetHelp() {
        assertEquals(set.getHelp(), CommandConstants.SET_HELP);
    }

    @Test
    public void testRequiresFile() {
        assertFalse(set.requiresFile());
    }

    @Test
    public void testExecute_helpNoArgs() throws IOException {
        setupStreams();

        String[] input = {"set"};
        set.execute(input, simulationManager);
        verifyZeroInteractions(simulationManager);
        assertEquals(set.getHelp() + "\n", outContent.toString());

        restoreStreams();
    }

    @Test
    public void testExecute_helpInvalidOption() throws IOException {
        setupStreams();

        String[] input = {"set", "invalid-option"};
        set.execute(input, simulationManager);
        verifyZeroInteractions(simulationManager);
        assertEquals(set.getHelp() + "\n", outContent.toString());

        restoreStreams();
    }

    @Test
    public void testExecute_helpTooManyArgs() throws IOException {
        setupStreams();

        String[] input = {"set", "too", "many", "args"};
        set.execute(input, simulationManager);
        verifyZeroInteractions(simulationManager);
        assertEquals(set.getHelp() + "\n", outContent.toString());

        restoreStreams();
    }

    @Test
    public void testExecute_setConf() throws IOException {
        setupStreams();
        File file = createFileWithContent("transitionConstraintName: trans");
        doNothing().when(simulationManager).setParsingConf(isA(ParsingConf.class));

        String[] input = {"set", "conf", file.getPath()};
        set.execute(input, simulationManager);
        verify(simulationManager).setParsingConf(any(ParsingConf.class));
        String msg = String.format(CommandConstants.SETTING_PARSING_OPTIONS_FROM, input[2]);
        msg += CommandConstants.DONE + "\n";
        assertEquals(msg, outContent.toString());
        file.delete();
        restoreStreams();
    }

    @Test
    public void testExecute_setConfDefault() {
        setupStreams();
        doNothing().when(simulationManager).setParsingConf(isA(ParsingConf.class));
        String[] input = {"set", "conf"};
        set.execute(input, simulationManager);
        verify(simulationManager).setParsingConf(any(ParsingConf.class));
        String msg = CommandConstants.SETTING_PARSING_OPTIONS;
        msg += CommandConstants.DONE + "\n";
        assertEquals(msg, outContent.toString());
        restoreStreams();
    }

    @Test
    public void testExecute_setConfInvalidFile() throws IOException {
        setupStreams();
        String[] input = {"set", "conf", "does_not_exist.txt"};
        set.execute(input, simulationManager);
        String errMsg = String.format(CommandConstants.SETTING_PARSING_OPTIONS_FROM, input[2]);
        errMsg += CommandConstants.FAILED_TO_READ_FILE + "\n";
        assertEquals(errMsg, outContent.toString());
        verifyZeroInteractions(simulationManager);
        restoreStreams();
    }

    @Test
    public void testExecute_setConfInvalidYaml() throws IOException, YAMLException {
        setupStreams();
        File file = createFileWithContent("invalid yaml");

        String[] input = {"set", "conf", file.getPath()};
        set.execute(input, simulationManager);
        String errMsg = String.format(CommandConstants.SETTING_PARSING_OPTIONS_FROM, input[2]);
        errMsg += CommandConstants.FAILED_TO_READ_CONF + "\n";
        assertEquals(errMsg, outContent.toString());
        verifyZeroInteractions(simulationManager);
        file.delete();
        restoreStreams();
    }

    @Test
    public void testExecute_setDiffModeNoValue() throws IOException {
        setupStreams();

        String[] input = {"set", "diff"};
        set.execute(input, simulationManager);
        verifyZeroInteractions(simulationManager);
        assertEquals(set.getHelp() + "\n", outContent.toString());

        restoreStreams();
    }

    @Test
    public void testExecute_setDiffModeInvalidValue() throws IOException {
        setupStreams();

        String[] input = {"set", "diff", "foo"};
        set.execute(input, simulationManager);
        verifyZeroInteractions(simulationManager);
        assertEquals(set.getHelp() + "\n", outContent.toString());

        restoreStreams();
    }

    @Test
    public void testExecute_setDiffModeOn() throws IOException {
        setupStreams();

        String[] input = {"set", "diff", "on"};
        set.execute(input, simulationManager);
        verify(simulationManager).setDiffMode(true);

        restoreStreams();
    }

    @Test
    public void testExecute_setDiffModeOff() throws IOException {
        setupStreams();

        String[] input = {"set", "diff", "off"};
        set.execute(input, simulationManager);
        verify(simulationManager).setDiffMode(false);

        restoreStreams();
    }
}
