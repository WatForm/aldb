package commands;

import simulation.SimulationManager;

public abstract class Command {
    private final static String[] SHORTHAND = {};

    public abstract String getName();
    public abstract String getDescription();
    public abstract String getHelp();

    public abstract void execute(String[] input, SimulationManager simulationManager);

    public String[] getShorthand() {
        return SHORTHAND;
    }

    public boolean requiresFile() {
        return false;
    }
}
