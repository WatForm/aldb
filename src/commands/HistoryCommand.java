package commands;

import simulation.SimulationManager;

public class HistoryCommand extends Command {
    public String getName() {
        return CommandConstants.HISTORY_NAME;
    }

    public String getDescription() {
        return CommandConstants.HISTORY_DESCRIPTION;
    }

    public String getHelp() {
        return CommandConstants.HISTORY_HELP;
    }

    public void execute(String[] input, SimulationManager simulationManager) {
        if (!simulationManager.isInitialized()) {
            System.out.println(CommandConstants.NO_MODEL_LOADED);
            return;
        }

        int n = 3;
        if (input.length > 1) {
            try {
                n = Integer.parseInt(input[1]);
            } catch (NumberFormatException e) {
                System.out.println(CommandConstants.INTEGER_ERROR);
                return;
            }
        }

        if (n < 1) {
            System.out.println(CommandConstants.GR_ONE_ERROR);
            return;
        }

        System.out.println(simulationManager.getHistory(n));
    }
}
