package core;

import core.SessionLog;
import simulation.SimulationManager;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.Rule;
import org.junit.rules.TemporaryFolder;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

public class TestSessionLog {
    @Rule
    public TemporaryFolder tmpdir = new TemporaryFolder();

    @Test
    public void testIsInitialized() throws IOException {
        SessionLog log = new SessionLog(null);
        assertFalse(log.isInitialized());

        File tmpfile = tmpdir.newFile("testIsInitialized");
        SessionLog prevLog = new SessionLog(tmpfile.getAbsolutePath());
        assertTrue(prevLog.isInitialized());
    }

    @Test
    public void testAppend() throws IOException {
        File tmpfile = tmpdir.newFile("testAppend");
        SessionLog log = new SessionLog(tmpfile.getAbsolutePath());
        log.append(new String[]{"help"});
        log.append(new String[]{"load", "models.als"});
        log.append(new String[]{"step", "5"});
        assertEquals(
            "help\nload models.als\nstep 5\n",
            new String(Files.readAllBytes(Paths.get(tmpfile.getAbsolutePath())))
        );

    }

    @Test
    public void testRestore() throws IOException {
        File tmpfile1 = tmpdir.newFile("testRestore1");
        SessionLog prevLog = new SessionLog(tmpfile1.getAbsolutePath());
        prevLog.append(new String[]{"foo", "bar"});
        prevLog.append(new String[]{"baz"});

        File tmpfile2 = tmpdir.newFile("testRestore2");
        SessionLog log = new SessionLog(tmpfile2.getAbsolutePath());

        SimulationManager simulationManager = new SimulationManager();
        prevLog.restore(simulationManager, log);

        assertEquals(
            "foo bar\nbaz\n",
            new String(Files.readAllBytes(Paths.get(tmpfile2.getAbsolutePath())))
        );
    }
}
