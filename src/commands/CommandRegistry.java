package commands;

public final class CommandRegistry {
    public final static Command NOT_FOUND = new NotFoundCommand();
    public final static Command EMPTY = new EmptyCommand();
    private final static Command[] commands = {
        new AltCommand(),
        new BreakCommand(),
        new CurrentCommand(),
        new DotCommand(),
        new HelpCommand(),
        new HistoryCommand(),
        new LoadCommand(),
        new QuitCommand(),
        new ReverseStepCommand(),
        new SetConfCommand(),
        new ScopeCommand(),
        new StepCommand(),
        new TraceCommand(),
        new UntilCommand(),
    };

    public static Command commandForString(String string) {
        if (string.isEmpty()) {
            return EMPTY;
        }
        for (Command command : commands) {
            if (command.getName().equals(string)) {
                return command;
            }

            String[] shorthand = command.getShorthand();
            for (String s : shorthand) {
                if (s.equals(string)) {
                    return command;
                }
            }
        }

        return NOT_FOUND;
    }

    public static Command[] getAllCommands() {
        return commands;
    }
}
