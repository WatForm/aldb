package commands;

import simulation.SimulationManager;

import java.lang.StringBuilder;

public class HelpCommand extends Command {
    public String getName() {
        return CommandConstants.HELP_NAME;
    }

    public String getDescription() {
        return CommandConstants.HELP_DESCRIPTION;
    }

    public String[] getShorthand() {
        return CommandConstants.HELP_SHORTHAND;
    }

    public String getHelp() {
        StringBuilder sb = new StringBuilder();
        sb.append(CommandConstants.AVAILABLE_COMMANDS_STR);
        for (Command command : CommandRegistry.getAllCommands()) {
            sb.append(String.format(CommandConstants.COMMAND_HELP_DELIMITER, command.getName(), command.getDescription()));
        }
        sb.append(CommandConstants.HELP_COMMAND_END_STR);
        return sb.toString();
    }

    public void execute(String[] input, SimulationManager simulationManager) {
        if (input.length < 2) {
            System.out.println(getHelp());
        } else {
            Command command = CommandRegistry.commandForString(input[1]);
            if (command == CommandRegistry.NOT_FOUND) {
                System.out.println(getHelp());
            } else {
                System.out.println(command.getHelp());
            }
        }
    }
}
