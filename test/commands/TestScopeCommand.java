package commands;

import commands.ScopeCommand;
import simulation.SimulationManager;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import org.junit.Test;

public class TestScopeCommand extends TestCommand {
    private final ScopeCommand scope = new ScopeCommand();
    private final SimulationManager simulationManager = mock(SimulationManager.class);

    @Test
    public void testGetName() {
        assertEquals(scope.getName(), CommandConstants.SCOPE_NAME);
    }

    @Test
    public void testGetDescription() {
        assertEquals(scope.getDescription(), CommandConstants.SCOPE_DESCRIPTION);
    }

    @Test
    public void testGetHelp() {
        assertEquals(scope.getHelp(), CommandConstants.SCOPE_HELP);
    }

    @Test
    public void testRequiresFile() {
        assertFalse(scope.requiresFile());
    }

    @Test
    public void testExecute() {
        setupStreams();
        when(simulationManager.isInitialized()).thenReturn(true);

        String[] input = {"scope"};
        scope.execute(input, simulationManager);
        verify(simulationManager).getScopes();
        restoreStreams();
    }

    @Test
    public void testExecute_sigInput() {
        setupStreams();
        List<String> res = new ArrayList<>();
        res.add("scope");
        when(simulationManager.isInitialized()).thenReturn(true);
        when(simulationManager.getScopeForSig(anyString())).thenReturn(res);

        String[] input = {"scope", "sig"};
        scope.execute(input, simulationManager);
        verify(simulationManager).getScopeForSig("sig");
        String msg = "\n{ scope }\n\n";
        assertEquals(msg, outContent.toString());
        restoreStreams();
    }

    @Test
    public void testExecute_sigInputEmpty() {
        setupStreams();
        List<String> res = new ArrayList<>();
        when(simulationManager.isInitialized()).thenReturn(true);
        when(simulationManager.getScopeForSig(anyString())).thenReturn(res);

        String[] input = {"scope", "sig"};
        scope.execute(input, simulationManager);
        verify(simulationManager).getScopeForSig("sig");
        String msg = "\n{  }\n\n";
        assertEquals(msg, outContent.toString());
        restoreStreams();
    }

    @Test
    public void testExecute_invalidScope() {
        setupStreams();
        when(simulationManager.isInitialized()).thenReturn(true);
        when(simulationManager.getScopeForSig(anyString())).thenReturn(null);
        String[] input = {"scope", "null"};
        scope.execute(input, simulationManager);
        String errMsg = CommandConstants.SIG_NOT_FOUND + "\n";
        assertEquals(errMsg, outContent.toString());
        restoreStreams();
    }

    @Test
    public void testExecute_uninitialized() {
        setupStreams();
        when(simulationManager.isInitialized()).thenReturn(false);

        String[] input = {"scope"};
        scope.execute(input, simulationManager);
        assertEquals(CommandConstants.NO_MODEL_LOADED + "\n", outContent.toString());
        restoreStreams();
    }
}
