package core;

import commands.Command;
import commands.CommandRegistry;
import simulation.SimulationManager;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * SessionLog is an append-only log on disk of commands that the user has submitted.
 *
 * For example, the contents of the log can look like:
 *
 * help
 * load model.als
 * step
 * step 4
 * foo
 */
public class SessionLog {
    private File log;
    private BufferedReader br;
    private String FILE_PREFIX = "aldb.";
    private String DATE_FORMAT = "yyyy.MM.dd.HH.mm.ss";
    private String TMP_DIR_PROPERTY = "java.io.tmpdir";
    private String WHITESPACE_REGEX = "\\s+";

    public SessionLog(String prevLogPath) throws IOException {
        if (prevLogPath != null)  {
            log = new File(prevLogPath);
            br = new BufferedReader(new FileReader(prevLogPath));
            return;
        }
        String logName = FILE_PREFIX + new SimpleDateFormat(DATE_FORMAT).format(new Date());
        log = new File(System.getProperty(TMP_DIR_PROPERTY), logName);
    }

    public void create() throws IOException {
        log.createNewFile();
    }

    public boolean isInitialized() {
        return log.exists();
    }

    public void append(String[] input) throws IOException {
        String line = String.join(" ", input) + "\n";
        Files.write(Paths.get(log.getAbsolutePath()), line.getBytes(), StandardOpenOption.APPEND);
    }

    /**
     * restore tries to recover the session from this SessionLog and apply it to the provided SimulationManager.
     * @param SimulationManager simulationManager
     * @param SessionLog newLog
     * @throws IOException if reading the previous session log or writing to the new session log fails.
     */
    public void restore(SimulationManager simulationManager, SessionLog newLog) throws IOException {
        for (String line = br.readLine(); line != null; line = br.readLine()) {
            String[] input = line.split(WHITESPACE_REGEX);
            Command command = CommandRegistry.commandForString(input[0]);
            command.execute(input, simulationManager);
            if (newLog.isInitialized()) {
                newLog.append(input);
            }
        }
    }
}
