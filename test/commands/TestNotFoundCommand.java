package commands;

import commands.NotFoundCommand;
import simulation.SimulationManager;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.Test;

public class TestNotFoundCommand extends TestCommand {
    private final NotFoundCommand notFound = new NotFoundCommand();
    private final SimulationManager simulationManager = new SimulationManager();

    @Test
    public void testGetName() {
        assertNull(notFound.getName());
    }

    @Test
    public void testGetDescription() {
        assertNull(notFound.getDescription());
    }

    @Test
    public void testGetHelp() {
        assertNull(notFound.getHelp());
    }

    @Test
    public void testRequiresFile() {
        assertFalse(notFound.requiresFile());
    }

    @Test
    public void testExecute() {
        setupStreams();
        String[] input = {"fydp"};
        String errMsg = String.format(CommandConstants.UNDEFINED_COMMAND, input[0]);
        notFound.execute(input, simulationManager);
        assertEquals(errMsg, outContent.toString());
        restoreStreams();
    }
}
