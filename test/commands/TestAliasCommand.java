package commands;

import commands.AliasCommand;
import simulation.AliasManager;
import simulation.SimulationManager;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import org.junit.Test;

public class TestAliasCommand extends TestCommand {
    private final AliasCommand aliasCommand = new AliasCommand();
    private final SimulationManager simulationManager = mock(SimulationManager.class);
    private final AliasManager am = mock(AliasManager.class);

    @Test
    public void testGetName() {
        assertEquals(aliasCommand.getName(), CommandConstants.ALIAS_NAME);
    }

    @Test
    public void testGetDescription() {
        assertEquals(aliasCommand.getDescription(), CommandConstants.ALIAS_DESCRIPTION);
    }

    @Test
    public void testGetHelp() {
        assertEquals(aliasCommand.getHelp(), CommandConstants.ALIAS_HELP);
    }

    @Test
    public void testGetShorthand() {
        assertEquals(aliasCommand.getShorthand(), CommandConstants.ALIAS_SHORTHAND);
    }

    @Test
    public void testRequiresFile() {
        assertFalse(aliasCommand.requiresFile());
    }

    @Test
    public void testExecute_rm() {
        setupStreams();
        when(simulationManager.getAliasManager()).thenReturn(am);
        when(am.removeAlias(anyString())).thenReturn(true);

        String[] input = {"alias", "-rm", "f1"};
        aliasCommand.execute(input, simulationManager);
        verify(am).removeAlias("f1");
        restoreStreams();
    }

    @Test
    public void testExecute_rm_invalid() {
        setupStreams();
        String[] input = {"alias", "-rm"};
        String errMsg = CommandConstants.ALIAS_HELP + "\n";
        aliasCommand.execute(input, simulationManager);
        assertEquals(errMsg, outContent.toString());
        restoreStreams();
    }

    @Test
    public void testExecute_rm_alias_dne() {
        setupStreams();
        when(simulationManager.getAliasManager()).thenReturn(am);
        String[] input = {"alias", "-rm", "foo"};
        String errMsg = String.format(CommandConstants.ALIAS_DNE, "foo") + "\n";
        aliasCommand.execute(input, simulationManager);
        assertEquals(errMsg, outContent.toString());
        restoreStreams();
    }

    @Test
    public void testExecute_l() {
        setupStreams();
        String res = "foo";
        when(simulationManager.getAliasManager()).thenReturn(am);
        when(am.getFormattedAliases()).thenReturn(res);

        String[] input = {"alias", "-l"};
        aliasCommand.execute(input, simulationManager);
        verify(am).getFormattedAliases();
        assertEquals(res + "\n", outContent.toString());
        restoreStreams();
    }

    @Test
    public void testExecute_c() {
        setupStreams();
        when(simulationManager.getAliasManager()).thenReturn(am);
        String[] input = {"alias", "-c"};
        aliasCommand.execute(input, simulationManager);
        verify(am).clearAliases();
        restoreStreams();
    }

    @Test
    public void testExecute_addAlias() {
        setupStreams();
        when(simulationManager.isInitialized()).thenReturn(true);
        when(simulationManager.getAliasManager()).thenReturn(am);

        String[] input = {"alias", "f1", "a=b"};
        String[] input2 = {"alias", "f2", "\"c = d\""};
        aliasCommand.execute(input, simulationManager);
        verify(am).addAlias("f1", "a=b");
        aliasCommand.execute(input2, simulationManager);
        verify(am).addAlias("f2", "c = d");
        restoreStreams();
    }

    @Test
    public void testExecute_addAlias_no_formula() {
        setupStreams();
        String[] input = {"alias", "f1"};
        String errMsg = CommandConstants.ALIAS_HELP + "\n";
        aliasCommand.execute(input, simulationManager);
        assertEquals(errMsg, outContent.toString());
        restoreStreams();
    }

    @Test
    public void testExecute_addAlias_multiple_formulas() {
        setupStreams();
        String[] input = {"alias", "f1", "a=b", "c=d"};
        String errMsg = CommandConstants.ALIAS_HELP + "\n";
        aliasCommand.execute(input, simulationManager);
        assertEquals(errMsg, outContent.toString());
        restoreStreams();
    }

    @Test
    public void testExecute_help() {
        setupStreams();
        String[] input = {"alias"};
        String errMsg = CommandConstants.ALIAS_HELP + "\n";
        aliasCommand.execute(input, simulationManager);
        assertEquals(errMsg, outContent.toString());
        restoreStreams();
    }
}
