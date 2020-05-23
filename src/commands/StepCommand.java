package commands;

import simulation.AliasManager;
import simulation.SimulationManager;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

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
        List<String> constraints = new ArrayList<String>();

        if (input.length > 1) {
            String params = String.join(" ", Arrays.copyOfRange(input, 1, input.length)).trim();
            // Check if a list of constraints is specified.
            if (params.matches("\\[.*\\]")) {
                params = params.substring(1, params.length() - 1);

                AliasManager am = simulationManager.getAliasManager();
                for (String constraint : params.split(",", -1)) {
                    constraint = constraint.trim();
                    if (constraint.matches("\".*\"")) {
                        constraint = constraint.substring(1, constraint.length() - 1);
                    }

                    if (am.isAlias(constraint)) {
                        constraint = am.getFormula(constraint);
                    }

                    if (simulationManager.validateConstraint(constraint)) {
                        constraints.add(constraint);
                    } else {
                        System.out.println(String.format(CommandConstants.INVALID_CONSTRAINT, constraint));
                        return;
                    }
                }

                steps = constraints.size();
            } else {
                try {
                    steps = Integer.parseInt(input[1]);
                } catch (NumberFormatException e) {
                    System.out.println(CommandConstants.INTEGER_ERROR);
                    return;
                }
            }
        }

        if (steps < 1) {
            System.out.println(CommandConstants.GR_ONE_ERROR);
            return;
        }

        if (!simulationManager.performStep(steps, constraints)) {
            return;
        }

        if (simulationManager.isDiffMode()) {
            System.out.println(simulationManager.getCurrentStateDiffStringByDelta(steps));
        } else {
            System.out.println(simulationManager.getCurrentStateString());

        }
    }
}
