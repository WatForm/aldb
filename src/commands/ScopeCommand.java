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
            Map<String, List<String>> scopes = simulationManager.getScopes();
            System.out.println(formattedScopes(scopes));
            return;
        }
        String sigName = input[1];
        List<String> scope = simulationManager.getScopeForSig(sigName);
        System.out.println(formattedScope(scope));
    }

    private String formattedScopes(Map<String, List<String>> scopes) {
        StringBuilder sb = new StringBuilder();
        for (String label : scopes.keySet()) {
            sb.append(String.format("\n%s: %s ", label, AlloyConstants.BLOCK_INITIALIZER));
            sb.append(String.format("%s %s", String.join(", ", scopes.get(label)), AlloyConstants.BLOCK_TERMINATOR));
        }
        sb.append("\n");
        return sb.toString();
    }

    private String formattedScope(List<String> scope) {
        if (scope == null) {
            return CommandConstants.SIG_NOT_FOUND;
        }
        return String.format(
            "\n%s %s %s\n",
            AlloyConstants.BLOCK_INITIALIZER,
            String.join(", ", scope),
            AlloyConstants.BLOCK_TERMINATOR
        );
    }
}
