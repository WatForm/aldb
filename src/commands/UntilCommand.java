package commands;

import simulation.SimulationManager;

public class UntilCommand extends Command {
    public String getName() {
        return CommandConstants.UNTIL_NAME;
    }

    public String[] getShorthand() {
        return CommandConstants.UNTIL_SHORTHAND;
    }

    public String getDescription() {
        return CommandConstants.UNTIL_DESCRIPTION;
    }

    public String getHelp() {
        return CommandConstants.UNTIL_HELP;
    }

    public void execute(String[] input, SimulationManager simulationManager) {
        if (!simulationManager.isInitialized()) {
            System.out.println(CommandConstants.NO_MODEL_LOADED);
            return;
        }

        int limit = 10;
        if (input.length > 1) {
            try {
                limit = Integer.parseInt(input[1]);
            } catch (NumberFormatException e) {
                System.out.println(CommandConstants.INTEGER_ERROR);
                return;
            }
        }

        if (limit < 1) {
            System.out.println(CommandConstants.GR_ONE_ERROR);
            return;
        }

        if (!simulationManager.performUntil(limit)) {
            System.out.println(CommandConstants.UNTIL_FAILED);
            return;
        }

        System.out.println(simulationManager.getCurrentStateString());
    }
}
