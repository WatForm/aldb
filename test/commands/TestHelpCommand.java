package commands;

import commands.HelpCommand;
import simulation.SimulationManager;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.Test;

public class TestHelpCommand extends TestCommand {
    private final HelpCommand help = new HelpCommand();
    private final SimulationManager simulationManager = new SimulationManager();
    private final String help_all_commands = String.join("\n",
        "Available commands:",
        "",
        "alias          -- Control the set of aliases used",
        "alt            -- Select an alternate execution path",
        "break          -- Control the set of constraints used",
        "current        -- Display the current state",
        "dot            -- Dump DOT graph to disk",
        "help           -- Display the list of available commands",
        "history        -- Display past states",
        "load           -- Load an Alloy model",
        "quit           -- Exit ALDB",
        "reverse-step   -- Go back n steps in the current state traversal path",
        "setconf        -- Set parsing options for the current session",
        "scope          -- Display scope set",
        "step           -- Perform a state transition of n steps",
        "trace          -- Load a saved Alloy XML instance",
        "until          -- Run until constraints are met",
        "",
        "Type \"help\" followed by a command name for full documentation."
    );

    @Test
    public void testGetName() {
        assertEquals(help.getName(), CommandConstants.HELP_NAME);
    }

    @Test
    public void testGetDescription() {
        assertEquals(help.getDescription(), CommandConstants.HELP_DESCRIPTION);
    }

    @Test
    public void testGetHelp() {
        assertEquals(help_all_commands, help.getHelp());
    }

    @Test
    public void testGetShorthand() {
        assertEquals(help.getShorthand(), CommandConstants.HELP_SHORTHAND);
    }

    @Test
    public void testRequiresFile() {
        assertFalse(help.requiresFile());
    }

    @Test
    public void testExecute_validCommands() {
        setupStreams();
        String[] sampleCommands = {"step", "load", "break"};
        for (String cmd : sampleCommands) {
            String[] input = {"help", cmd};
            help.execute(input, simulationManager);
        }
        String msg = String.join("\n",
            CommandConstants.STEP_HELP,
            CommandConstants.LOAD_HELP,
            CommandConstants.BREAK_HELP
        );
        assertEquals(msg + "\n", outContent.toString());
        restoreStreams();
    }

    @Test
    public void testExecute_invalidCommand() {
        setupStreams();
        String invalidCommand = "invalid";
        String[] input = {"help", invalidCommand};
        help.execute(input, simulationManager);
        assertEquals(help_all_commands + "\n", outContent.toString());
        restoreStreams();
    }

    @Test
    public void testExecute_justHelp() {
        setupStreams();
        String[] input = {"help"};
        help.execute(input, simulationManager);
        assertEquals(help_all_commands + "\n", outContent.toString());
        restoreStreams();
    }
}
