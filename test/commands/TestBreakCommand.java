package commands;

import commands.BreakCommand;
import simulation.AliasManager;
import simulation.ConstraintManager;
import simulation.SimulationManager;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import org.junit.Test;

public class TestBreakCommand extends TestCommand {
    private final BreakCommand breakCommand = new BreakCommand();
    private final SimulationManager simulationManager = mock(SimulationManager.class);
    private final AliasManager am = mock(AliasManager.class);
    private final ConstraintManager cm = mock(ConstraintManager.class);

    @Test
    public void testGetName() {
        assertEquals(breakCommand.getName(), CommandConstants.BREAK_NAME);
    }

    @Test
    public void testGetDescription() {
        assertEquals(breakCommand.getDescription(), CommandConstants.BREAK_DESCRIPTION);
    }

    @Test
    public void testGetHelp() {
        assertEquals(breakCommand.getHelp(), CommandConstants.BREAK_HELP);
    }

    @Test
    public void testGetShorthand() {
        assertEquals(breakCommand.getShorthand(), CommandConstants.BREAK_SHORTHAND);
    }

    @Test
    public void testRequiresFile() {
        assertFalse(breakCommand.requiresFile());
    }

    @Test
    public void testExecute_rm() {
        setupStreams();
        when(simulationManager.getConstraintManager()).thenReturn(cm);
        when(cm.removeConstraint(anyInt())).thenReturn(true);

        String[] input = {"break", "-rm", "3"};
        breakCommand.execute(input, simulationManager);
        verify(cm).removeConstraint(3);
        restoreStreams();
    }

    @Test
    public void testExecute_l() {
        setupStreams();
        String res = "formatted constraint";
        when(simulationManager.getConstraintManager()).thenReturn(cm);
        when(cm.getFormattedConstraints()).thenReturn(res);

        String[] input = {"break", "-l"};
        breakCommand.execute(input, simulationManager);
        verify(cm).getFormattedConstraints();
        assertEquals(res + "\n", outContent.toString());
        restoreStreams();
    }

    @Test
    public void testExecute_c() {
        when(simulationManager.getConstraintManager()).thenReturn(cm);
        String[] input = {"break", "-c"};
        breakCommand.execute(input, simulationManager);
        verify(cm).clearConstraints();
    }

    @Test
    public void testExecute_addConstraint() {
        when(simulationManager.isInitialized()).thenReturn(true);
        when(simulationManager.getAliasManager()).thenReturn(am);
        when(simulationManager.getConstraintManager()).thenReturn(cm);
        when(simulationManager.validateConstraint(anyString())).thenReturn(true);

        String[] input = {"break", "\"a = b\"", "c", "d=f"};
        breakCommand.execute(input, simulationManager);
        verify(cm).addConstraint("a = b");
        verify(cm).addConstraint("c");
        verify(cm).addConstraint("d=f");
    }

    @Test
    public void testExecute_addAliasedConstraint() {
        when(simulationManager.isInitialized()).thenReturn(true);
        when(simulationManager.getAliasManager()).thenReturn(am);
        when(simulationManager.getConstraintManager()).thenReturn(cm);
        when(simulationManager.validateConstraint(anyString())).thenReturn(true);

        String alias = "f1";
        String formula = "a=b";
        when(am.isAlias(alias)).thenReturn(true);
        when(am.getFormula(alias)).thenReturn(formula);
        String[] input = {"break", alias};

        breakCommand.execute(input, simulationManager);
        verify(cm).addConstraint(formula);
    }

    @Test
    public void testExecute_help() {
        setupStreams();
        String[] input = {"break"};
        String errMsg = CommandConstants.BREAK_HELP + "\n";
        breakCommand.execute(input, simulationManager);
        assertEquals(errMsg, outContent.toString());
        restoreStreams();
    }

    @Test
    public void testExecute_invalidInput_RM() {
        setupStreams();
        String[] input = {"break", "-rm"};
        String errMsg = CommandConstants.BREAK_HELP + "\n";
        breakCommand.execute(input, simulationManager);
        assertEquals(errMsg, outContent.toString());
        restoreStreams();
    }

    @Test
    public void testExecute_invalidConstraintIDType() throws NumberFormatException {
        setupStreams();
        String[] input = {"break", "-rm", "one"};
        String errMsg = CommandConstants.INTEGER_ERROR + "\n";
        breakCommand.execute(input, simulationManager);
        assertEquals(errMsg, outContent.toString());
        restoreStreams();
    }

    @Test
    public void testExecute_invalidConstraintID() {
        setupStreams();
        when(simulationManager.getConstraintManager()).thenReturn(cm);
        String[] input = {"break", "-rm", "99"};
        int constraintID = 99;
        String errMsg = String.format(CommandConstants.INVALID_CONSTRAINT_ID, constraintID) + "\n";
        breakCommand.execute(input, simulationManager);
        assertEquals(errMsg, outContent.toString());
        restoreStreams();
    }

    @Test
    public void testExecute_invalidConstraint() {
        setupStreams();
        when(simulationManager.isInitialized()).thenReturn(true);
        when(simulationManager.validateConstraint(anyString())).thenReturn(false);
        when(simulationManager.getAliasManager()).thenReturn(am);
        String[] input = {"break", "invalid"};
        String errMsg = String.format(CommandConstants.INVALID_CONSTRAINT, input[1]) + "\n";
        breakCommand.execute(input, simulationManager);
        assertEquals(errMsg, outContent.toString());
        restoreStreams();
    }

    @Test
    public void testExecute_uninitialized() {
        setupStreams();
        when(simulationManager.isInitialized()).thenReturn(false);
        String[] input = {"break", "test"};
        breakCommand.execute(input, simulationManager);
        assertEquals(CommandConstants.NO_MODEL_LOADED + "\n", outContent.toString());
        restoreStreams();
    }
}
