package commands;

import simulation.SimulationManager;

public class ReverseStepCommand extends Command {
    public String getName() {
        return CommandConstants.REVERSE_STEP_NAME;
    }

    public String[] getShorthand() {
        return CommandConstants.REVERSE_STEP_SHORTHAND;
    }

    public String getDescription() {
        return CommandConstants.REVERSE_STEP_DESCRIPTION;
    }

    public String getHelp() {
        return CommandConstants.REVERSE_STEP_HELP;
    }

    public void execute(String[] input, SimulationManager simulationManager) {
        if (!simulationManager.isInitialized()) {
            System.out.println(CommandConstants.NO_MODEL_LOADED);
            return;
        }

        int steps = 1;
        if (input.length > 1) {
            try {
                steps = Integer.parseInt(input[1]);
            } catch (NumberFormatException e) {
                System.out.println(CommandConstants.INTEGER_ERROR);
                return;
            }
        }

        if (steps < 1) {
            System.out.println(CommandConstants.GR_ONE_ERROR);
            return;
        }

        simulationManager.performReverseStep(steps);
    }
}
