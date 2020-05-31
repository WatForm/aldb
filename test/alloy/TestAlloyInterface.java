package alloy;

import edu.mit.csail.sdg.alloy4.Err;
import edu.mit.csail.sdg.ast.Sig;
import edu.mit.csail.sdg.translator.A4Solution;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.Rule;
import org.junit.rules.TemporaryFolder;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;
import java.util.HashMap;
import java.util.Map;

public class TestAlloyInterface {
    @Rule
    public TemporaryFolder tempFolder = new TemporaryFolder();

    @Test
    public void testCompile() throws IOException {
        File model = createModelForTesting();
        assertNotNull(AlloyInterface.compile(model.getPath()));
    }

    @Test
    public void testCompile_failureSyntax() throws IOException {
        File model = createModelForTesting();

        // Append invalid Alloy code to file.
        String invalidCode = "}{";
        appendToFile(model, invalidCode);

        assertThrows(Err.class, () -> {
            AlloyInterface.compile(model.getPath());
        });
    }

    @Test
    public void testCompile_failureNoFile() {
        assertThrows(Err.class, () -> {
            AlloyInterface.compile("non-existant-file");
        });
    }

    @Test
    public void testRun() throws IOException {
        File model = createModelForTesting();
        String runCommand = "run {} for 2 State";
        appendToFile(model, runCommand);

        A4Solution sol = AlloyInterface.run(AlloyInterface.compile(model.getPath()));
        assertNotNull(sol);

        Sig sig = AlloyInterface.getSigFromA4Solution(sol, "State");
        assertEquals(2, sol.eval(sig).size());
    }

    @Test
    public void testRun_noCommands() throws IOException {
        File model = createModelForTesting();

        A4Solution sol = AlloyInterface.run(AlloyInterface.compile(model.getPath()));
        // Models contain a default command: 'Run Default for 4 but 4 int, 4 seq expect 0'.
        assertNotNull(sol);

        Sig sig = AlloyInterface.getSigFromA4Solution(sol, "State");
        assertEquals(4, sol.eval(sig).size());
    }

    @Test
    public void testRun_multipleCommands() throws IOException {
        File model = createModelForTesting();
        String runCommands = String.join("\n",
            "run {} for 3 State",
            "run {} for 2 State"
        );
        appendToFile(model, runCommands);

        A4Solution sol = AlloyInterface.run(AlloyInterface.compile(model.getPath()));
        assertNotNull(sol);

        // Only the last command should have run.
        Sig sig = AlloyInterface.getSigFromA4Solution(sol, "State");
        assertEquals(2, sol.eval(sig).size());
    }

    @Test
    public void testSolutionFromXMLFile() throws Exception {
        File model = createModelForTesting();
        String runCommand = "run {} for 3 State";
        appendToFile(model, runCommand);

        A4Solution sol = AlloyInterface.run(AlloyInterface.compile(model.getPath()));
        assertNotNull(sol);

        Sig sig = AlloyInterface.getSigFromA4Solution(sol, "State");
        assertEquals(3, sol.eval(sig).size());

        File xmlFile = tempFolder.newFile("test.xml");
        Map<String, String> sources = new HashMap<String, String>();
        sources.put(model.getPath(), new String(Files.readAllBytes(model.toPath())));
        sol.writeXML(xmlFile.getPath(), null, sources);
        A4Solution solFromXML = AlloyInterface.solutionFromXMLFile(xmlFile);

        sig = AlloyInterface.getSigFromA4Solution(solFromXML, "State");
        assertEquals(3, solFromXML.eval(sig).size());
    }

    @Test
    public void testSolutionFromXMLFile_invalidXML() throws IOException {
        File xmlFile = tempFolder.newFile("test.xml");

        appendToFile(xmlFile, "><");
        assertThrows(Exception.class, () -> {
            AlloyInterface.solutionFromXMLFile(xmlFile);
        });
    }

    @Test
    public void testSolutionFromXMLFile_noFile() throws IOException {
        assertThrows(Exception.class, () -> {
            AlloyInterface.solutionFromXMLFile(null);
        });
    }

    @Test
    public void testSolutionFromXMLFile_validXMLInvalidStructure() throws IOException {
        File xmlFile = tempFolder.newFile("test.xml");

        appendToFile(xmlFile, "<alloy></alloy>");
        assertThrows(Exception.class, () -> {
            AlloyInterface.solutionFromXMLFile(xmlFile);
        });
    }

    @Test
    public void testGetSigFromA4Solution() throws IOException {
        File model = createModelForTesting();
        String runCommand = "run {} for 2 State";
        appendToFile(model, runCommand);

        A4Solution sol = AlloyInterface.run(AlloyInterface.compile(model.getPath()));
        Sig sig = AlloyInterface.getSigFromA4Solution(sol, "State");
        assertNotNull(sig);
    }

    @Test
    public void testGetSigFromA4Solution_sigNotFound() throws IOException {
        File model = createModelForTesting();
        String runCommand = "run {} for 2 State";
        appendToFile(model, runCommand);

        A4Solution sol = AlloyInterface.run(AlloyInterface.compile(model.getPath()));
        Sig sig = AlloyInterface.getSigFromA4Solution(sol, "Sig");
        assertNull(sig);
    }

    private File createModelForTesting() throws IOException {
        String modelString = String.join("\n",
            "open util/ordering[State]",
            "",
            "abstract sig SwitchState {}",
            "",
            "one sig On, Off extends SwitchState {}",
            "",
            "sig State {",
            "    switch: SwitchState",
            "}",
            "",
            "pred init[s: State] {",
            "    s.switch = Off",
            "}",
            "",
            "pred next[s, s': State] {",
            "    s.switch = On implies s'.switch = Off",
            "    s.switch = Off implies s'.switch = On",
            "}",
            "",
            "fact { init[first] }",
            "",
            "fact { all s: State, s': s.next { next[s, s'] } }",
            ""
        );

        File tempFile = tempFolder.newFile("test.als");
        Files.write(tempFile.toPath(), modelString.getBytes());
        return tempFile;
    }

    private void appendToFile(File file, String string) throws IOException {
        Files.write(file.toPath(), string.getBytes(), StandardOpenOption.APPEND);
    }
}
