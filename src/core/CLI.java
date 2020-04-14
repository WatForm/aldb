package core;

import commands.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.HashMap;
import java.util.Map;

import org.jline.builtins.Completers.FileNameCompleter;
import org.jline.builtins.Completers.RegexCompleter;
import org.jline.reader.Completer;
import org.jline.reader.EndOfFileException;
import org.jline.reader.LineReader;
import org.jline.reader.LineReaderBuilder;
import org.jline.reader.UserInterruptException;
import org.jline.reader.impl.completer.StringsCompleter;
import org.jline.reader.impl.DefaultParser;
import org.jline.terminal.*;

class CLI {
    private static final String PROMPT = "(aldb) ";

    private LineReader reader;

    public CLI() {
        Terminal terminal = null;
        try {
            terminal = TerminalBuilder.builder().system(true).build();
        } catch (IOException e) {
            System.exit(0);
        }

        Completer completer = createCompleter();

        DefaultParser parser = new DefaultParser();
        parser.setEofOnUnclosedQuote(true);

        reader = LineReaderBuilder
          .builder()
          .terminal(terminal)
          .completer(completer)
          .parser(parser)
          .build();
    }

    public String[] getInput() {
        String rawInput = null;
        try {
            rawInput = reader.readLine(PROMPT);
        } catch (UserInterruptException e) {
            System.exit(0);
        } catch (EndOfFileException e) {
            System.exit(0);
        }

        String[] input = {};
        if (rawInput != null) {
            input = rawInput.trim().split(" ");
        }

        return input;
    }

    private Completer createCompleter() {
        Map<String, Completer> completers = new HashMap<>();
        List<String> regex = new ArrayList<String>();

        int commandID = 1;
        completers.put("file", new FileNameCompleter());
        for (Command command : CommandRegistry.getAllCommands()) {
            // The completer name is different from the command name to allow
            // for commands containing special characters (such as hyphens)
            // to be understood by the RegexCompleter.
            String commandName = command.getName();
            String completerName = String.format("C%d", commandID);
            completers.put(completerName, new StringsCompleter(commandName));

            if (command.requiresFile()) {
                regex.add(String.format("%s file", completerName));
            } else {
                regex.add(completerName);
            }

            commandID++;
        }

        return new RegexCompleter(String.join("|", regex), completers::get);
    }
}
