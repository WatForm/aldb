package commands;

import simulation.AliasManager;
import simulation.SimulationManager;

import java.util.Arrays;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class AliasCommand extends Command {
    private final static String RM_FLAG = "-rm";
    private final static String L_FLAG = "-l";
    private final static String C_FLAG = "-c";

    public String getName() {
        return CommandConstants.ALIAS_NAME;
    }

    public String getDescription() {
        return CommandConstants.ALIAS_DESCRIPTION;
    }

    public String getHelp() {
        return CommandConstants.ALIAS_HELP;
    }

    public String[] getShorthand() {
        return CommandConstants.ALIAS_SHORTHAND;
    }

    public void execute(String[] input, SimulationManager simulationManager) {
        if (input.length == 1) {
            System.out.println(CommandConstants.ALIAS_HELP);
            return;
        }
        String arg = input[1];

        AliasManager am = simulationManager.getAliasManager();
        if (arg.equals(RM_FLAG)) {
            if (input.length != 3) {
                System.out.println(CommandConstants.ALIAS_HELP);
                return;
            }
            String alias = input[2];
            if (!am.removeAlias(alias)) {
                System.out.println(String.format(CommandConstants.ALIAS_DNE, alias));
            }
        } else if (arg.equals(L_FLAG)) {
            System.out.println(am.getFormattedAliases());
        } else if (arg.equals(C_FLAG)) {
            am.clearAliases();
        } else {
            if (input.length < 3) {
                System.out.println(CommandConstants.ALIAS_HELP);
                return;
            }

            String formula = String.join(" ", Arrays.copyOfRange(input, 2, input.length));
            Matcher m = Pattern.compile(CommandConstants.CONSTRAINT_REGEX).matcher(formula);

            if (m.find()) {
                // Only one formula should be specified.
                if (!m.hitEnd()) {
                    System.out.println(CommandConstants.ALIAS_HELP);
                    return;
                }
                formula = m.group(1).replace("\"", "");
                am.addAlias(arg, formula);
            }
        }
    }
}
