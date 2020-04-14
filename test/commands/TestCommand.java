package commands;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.Ignore;
import org.junit.Rule;
import org.junit.rules.TemporaryFolder;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.file.Files;

@Ignore("Base class for command testing")
public class TestCommand {
    @Rule
    public TemporaryFolder tempFolder = new TemporaryFolder();

    protected final ByteArrayOutputStream outContent = new ByteArrayOutputStream();
    protected final PrintStream originalOut = System.out;

    protected void setupStreams() {
        System.setOut(new PrintStream(outContent));
    }

    protected void restoreStreams() {
        System.setOut(originalOut);
    }

    protected File createFileWithContent(String content) throws IOException {
        File file = tempFolder.newFile("test");
        Files.write(file.toPath(), content.getBytes());
        return file;
    }
}
