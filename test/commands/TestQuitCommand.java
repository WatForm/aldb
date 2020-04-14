package commands;

import commands.QuitCommand;
import simulation.SimulationManager;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import org.junit.Rule;
import org.junit.Test;
import org.junit.contrib.java.lang.system.ExpectedSystemExit;

public class TestQuitCommand extends TestCommand {
    private final QuitCommand quit = new QuitCommand();
    private final SimulationManager simulationManager = mock(SimulationManager.class);

    @Rule
    public final ExpectedSystemExit exit = ExpectedSystemExit.none();

    @Test
    public void testGetName() {
        assertEquals(quit.getName(), CommandConstants.QUIT_NAME);
    }

    @Test
    public void testGetDescription() {
        assertEquals(quit.getDescription(), CommandConstants.QUIT_DESCRIPTION);
    }

    @Test
    public void testGetHelp() {
        assertEquals(quit.getHelp(), CommandConstants.QUIT_HELP);
    }

    @Test
    public void testGetShorthand() {
        assertEquals(quit.getShorthand(), CommandConstants.QUIT_SHORTHAND);
    }

    @Test
    public void testRequiresFile() {
        assertFalse(quit.requiresFile());
    }

    @Test
    public void testExecute() {
        exit.expectSystemExitWithStatus(0);
        String[] input = {"quit"};
        quit.execute(input, simulationManager);
    }
}
