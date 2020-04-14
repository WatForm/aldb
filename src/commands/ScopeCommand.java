package commands;

import alloy.AlloyConstants;
import simulation.SimulationManager;

import java.util.List;
import java.util.Map;

public class ScopeCommand extends Command {
    public String getName() {
        return CommandConstants.SCOPE_NAME;
    }

    public String getDescription() {
        return CommandConstants.SCOPE_DESCRIPTION;
    }

    public String getHelp() {
        return CommandConstants.SCOPE_HELP;
    }

    public void execute(String[] input, SimulationManager simulationManager) {
        if (!simulationManager.isInitialized()) {
            System.out.println(CommandConstants.NO_MODEL_LOADED);
            return;
        }

        if (input.length == 1) {
            System.out.println();
            Map<String, List<String>> scopes = simulationManager.getScopes();
            for (String label : scopes.keySet()) {
                StringBuilder sb = new StringBuilder();
                sb.append(String.format("%s: %s ", label, AlloyConstants.BLOCK_INITIALIZER));
                sb.append(String.format("%s %s", String.join(", ", scopes.get(label)), AlloyConstants.BLOCK_TERMINATOR));
                System.out.println(sb.toString());
            }
            System.out.println();
            return;
        }
        String sigName = input[1];
        List<String> scope = simulationManager.getScopeForSig(sigName);
        if (scope == null) {
            System.out.println(CommandConstants.SIG_NOT_FOUND);
            return;
        }
        System.out.println();
        for (String s : scope) {
            System.out.println(s);
        }
        System.out.println();
    }
}
