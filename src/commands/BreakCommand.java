package commands;

import simulation.AliasManager;
import simulation.ConstraintManager;
import simulation.SimulationManager;

import java.util.Arrays;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class BreakCommand extends Command {
    private final static String RM_FLAG = "-rm";
    private final static String L_FLAG = "-l";
    private final static String C_FLAG = "-c";

    public String getName() {
        return CommandConstants.BREAK_NAME;
    }

    public String getDescription() {
        return CommandConstants.BREAK_DESCRIPTION;
    }

    public String getHelp() {
        return CommandConstants.BREAK_HELP;
    }

    public String[] getShorthand() {
        return CommandConstants.BREAK_SHORTHAND;
    }

    public void execute(String[] input, SimulationManager simulationManager) {
        if (input.length == 1) {
            System.out.println(CommandConstants.BREAK_HELP);
            return;
        }
        String arg = input[1];

        ConstraintManager cm = simulationManager.getConstraintManager();
        if (arg.equals(RM_FLAG)) {
            if (input.length != 3) {
                System.out.println(CommandConstants.BREAK_HELP);
                return;
            }
            int constraintID;
            try {
                constraintID = Integer.parseInt(input[2]);
            } catch (NumberFormatException e) {
                System.out.println(CommandConstants.INTEGER_ERROR);
                return;
            }
            if (!cm.removeConstraint(constraintID)) {
                System.out.println(String.format(CommandConstants.INVALID_CONSTRAINT_ID, constraintID));
            }
        } else if (arg.equals(L_FLAG)) {
            System.out.println(cm.getFormattedConstraints());
        } else if (arg.equals(C_FLAG)) {
            cm.clearConstraints();
        } else {
            if (!simulationManager.isInitialized()) {
                System.out.println(CommandConstants.NO_MODEL_LOADED);
                return;
            }
            // Use regex to find constraints delimited by spaces, and encapsulated by quotes.
            // Example: 'break "a = b" c' should add two constraints, "a = b" and "c".
            String allConstraints = String.join(" ", Arrays.copyOfRange(input, 1, input.length));
            Matcher m = Pattern.compile(CommandConstants.CONSTRAINT_REGEX).matcher(allConstraints);

            AliasManager am = simulationManager.getAliasManager();
            while (m.find()) {
                String constraint = m.group(1).replace("\"", "");

                if (am.isAlias(constraint)) {
                    constraint = am.getPredicate(constraint);
                }

                if (simulationManager.validateConstraint(constraint)) {
                    cm.addConstraint(constraint);
                } else {
                    System.out.println(String.format(CommandConstants.INVALID_CONSTRAINT, constraint));
                }
            }
        }
    }
}
