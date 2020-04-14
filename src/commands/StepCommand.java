package commands;

import simulation.SimulationManager;

public class StepCommand extends Command {
    private final static String[] SHORTHAND = CommandConstants.STEP_SHORTHAND;

    public String getName() {
        return CommandConstants.STEP_NAME;
    }

    public String[] getShorthand() {
        return SHORTHAND;
    }

    public String getDescription() {
        return CommandConstants.STEP_DESCRIPTION;
    }

    public String getHelp() {
        return CommandConstants.STEP_HELP;
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

        simulationManager.performStep(steps);

        if (simulationManager.isTrace()) {
            System.out.println(simulationManager.getCurrentStateDiffString());
            return;
        }

        System.out.println(simulationManager.getCurrentStateString());
    }
}
