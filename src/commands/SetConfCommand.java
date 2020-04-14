package commands;

import alloy.AlloyUtils;
import alloy.ParsingConf;
import simulation.SimulationManager;

import java.io.IOException;
import java.io.File;
import java.nio.file.Files;

import org.yaml.snakeyaml.error.YAMLException;

public class SetConfCommand extends Command {
    public SetConfCommand() {}

    public String getName() {
        return CommandConstants.SETCONF_NAME;
    }

    public String getDescription() {
        return CommandConstants.SETCONF_DESCRIPTION;
    }

    public String getHelp() {
        return CommandConstants.SETCONF_HELP;
    }

    public boolean requiresFile() {
        return true;
    }

    public void execute(String[] input, SimulationManager simulationManager) {
        if (input.length < 2) {
            System.out.print(CommandConstants.SETTING_PARSING_OPTIONS);
            simulationManager.setParsingConf(new ParsingConf());
            System.out.println(CommandConstants.DONE);
            return;
        }

        String filename = input[1];
        File file = new File(filename);

        System.out.printf(CommandConstants.SETTING_PARSING_OPTIONS_FROM, filename);

        String inputFileContents;
        try {
            inputFileContents = AlloyUtils.readFromFile(file);
        } catch (IOException e) {
            System.out.println(CommandConstants.FAILED_TO_READ_FILE);
            return;
        }
        try {
            ParsingConf conf = ParsingConf.initializeWithYaml(inputFileContents);
            simulationManager.setParsingConf(conf);
        } catch (YAMLException e) {
            System.out.println(CommandConstants.FAILED_TO_READ_CONF);
            return;
        }
        System.out.println(CommandConstants.DONE);
    }
}
