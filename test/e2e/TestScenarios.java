package e2e;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.Before;
import org.junit.Rule;
import org.junit.rules.TestName;
import org.junit.Test;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;

public class TestScenarios {
    private StringBuilder aldbOutput;
    private final String PROMPT = "(aldb) ";

    @Rule
    public final TestName testName = new TestName();

    @Before
    public void init() {
        aldbOutput = new StringBuilder();
        System.out.printf("Running %s...\n", testName.getMethodName());
    }

    @Test
    public void testSolveRCP() throws Exception {
        runALDB(
            "load models/river_crossing.als",
            "current",
            "break \"far = Object && no near\"",
            "until",
            "history 10"
        );
        assertOutput(
            "Reading model from models/river_crossing.als...done.",
            "",
            "S1",
            "----",
            "far: {  }",
            "near: { Chicken, Farmer, Fox, Grain }",
            "\n",
            "S8",
            "----",
            "far: { Chicken, Farmer, Fox, Grain }",
            "near: {  }",
            "\n",
            "S1 (-7)",
            "---------",
            "far: {  }",
            "near: { Chicken, Farmer, Fox, Grain }",
            "",
            "S2 (-6)",
            "---------",
            "far: { Chicken, Farmer }",
            "near: { Fox, Grain }",
            "",
            "S3 (-5)",
            "---------",
            "far: { Chicken }",
            "near: { Farmer, Fox, Grain }",
            "",
            "S4 (-4)",
            "---------",
            "far: { Chicken, Farmer, Fox }",
            "near: { Grain }",
            "",
            "S5 (-3)",
            "---------",
            "far: { Fox }",
            "near: { Chicken, Farmer, Grain }",
            "",
            "S6 (-2)",
            "---------",
            "far: { Farmer, Fox, Grain }",
            "near: { Chicken }",
            "",
            "S7 (-1)",
            "---------",
            "far: { Fox, Grain }",
            "near: { Chicken, Farmer }",
            ""
        );
    }

    @Test
    public void testTraceFGS() throws Exception {
        runALDB(
            "set conf traces/fgs_conf.yml",
            "set diff on",
            "trace traces/fgs_counterexample.xml",
            "step",
            "step"
        );
        assertOutput(
            "Setting parsing options from traces/fgs_conf.yml...done.",
            "Reading trace from traces/fgs_counterexample.xml...done.",
            "",
            "S1 -> S2",
            "------------",
            "FlightModes_NAV_Selected: { boolean/True }",
            "stable: { boolean/False }",
            "",
            "",
            "S2 -> S3",
            "------------",
            "FlightModes_ALTSEL_Selected: { boolean/True }",
            "FlightModes_FD_On: { boolean/True }",
            "FlightModes_HDG_Switch_Pressed: { boolean/False }",
            "FlightModes_Modes_On: { boolean/True }",
            "FlightModes_NAV_Capture_Condition_Met: { boolean/True }",
            "FlightModes_NAV_Selected: { boolean/False }",
            "FlightModes_NAV_Switch_Pressed: { boolean/False }",
            "FlightModes_Pilot_Flying_Transfer: { boolean/True }",
            "FlightModes_Selected_NAV_Frequency_Changed: { boolean/False }",
            "stable: { boolean/True }",
            ""
        );
    }

    @Test
    public void testTraceFGS_afterLoad() throws Exception {
        runALDB(
            "load models/switch.als",
            "set conf traces/fgs_conf.yml",
            "set diff on",
            "trace traces/fgs_counterexample.xml",
            "step",
            "step"
        );
        assertOutput(
            "Reading model from models/switch.als...done.",
            "Setting parsing options from traces/fgs_conf.yml...done.",
            "Reading trace from traces/fgs_counterexample.xml...done.",
            "",
            "S1 -> S2",
            "------------",
            "FlightModes_NAV_Selected: { boolean/True }",
            "stable: { boolean/False }",
            "",
            "",
            "S2 -> S3",
            "------------",
            "FlightModes_ALTSEL_Selected: { boolean/True }",
            "FlightModes_FD_On: { boolean/True }",
            "FlightModes_HDG_Switch_Pressed: { boolean/False }",
            "FlightModes_Modes_On: { boolean/True }",
            "FlightModes_NAV_Capture_Condition_Met: { boolean/True }",
            "FlightModes_NAV_Selected: { boolean/False }",
            "FlightModes_NAV_Switch_Pressed: { boolean/False }",
            "FlightModes_Pilot_Flying_Transfer: { boolean/True }",
            "FlightModes_Selected_NAV_Frequency_Changed: { boolean/False }",
            "stable: { boolean/True }",
            ""
        );
    }

    @Test
    public void testTraceFGS_afterLoadWithEmbeddedConf() throws Exception {
        runALDB(
            "load models/musical_chairs.als",
            "set conf traces/fgs_conf.yml",
            "set diff on",
            "trace traces/fgs_counterexample.xml",
            "step",
            "step"
        );
        assertOutput(
            "Reading model from models/musical_chairs.als...done.",
            "Setting parsing options from traces/fgs_conf.yml...done.",
            "Reading trace from traces/fgs_counterexample.xml...done.",
            "",
            "S1 -> S2",
            "------------",
            "FlightModes_NAV_Selected: { boolean/True }",
            "stable: { boolean/False }",
            "",
            "",
            "S2 -> S3",
            "------------",
            "FlightModes_ALTSEL_Selected: { boolean/True }",
            "FlightModes_FD_On: { boolean/True }",
            "FlightModes_HDG_Switch_Pressed: { boolean/False }",
            "FlightModes_Modes_On: { boolean/True }",
            "FlightModes_NAV_Capture_Condition_Met: { boolean/True }",
            "FlightModes_NAV_Selected: { boolean/False }",
            "FlightModes_NAV_Switch_Pressed: { boolean/False }",
            "FlightModes_Pilot_Flying_Transfer: { boolean/True }",
            "FlightModes_Selected_NAV_Frequency_Changed: { boolean/False }",
            "stable: { boolean/True }",
            ""
        );
    }

    @Test
    public void testLoadIdempotent_noEmbeddedConf() throws Exception {
        runALDB(
            "load models/river_crossing.als",
            "load models/river_crossing.als",
            "load models/river_crossing.als"
        );
        assertOutput(
            "Reading model from models/river_crossing.als...done.",
            "Reading model from models/river_crossing.als...done.",
            "Reading model from models/river_crossing.als...done."
        );
    }

    @Test
    public void testLoadIdempotent_embeddedConf() throws Exception {
        runALDB(
            "load models/musical_chairs.als",
            "load models/musical_chairs.als",
            "load models/musical_chairs.als"
        );
        assertOutput(
            "Reading model from models/musical_chairs.als...done.",
            "Reading model from models/musical_chairs.als...done.",
            "Reading model from models/musical_chairs.als...done."
        );
    }

    @Test
    public void testLoadEmbeddedConf() throws Exception {
        // A model's (musical_chairs.als) embedded ParsingConf should be removed after loading a new model.
        runALDB(
            "load models/river_crossing.als",
            "load models/musical_chairs.als",
            "load models/river_crossing.als"
        );
        assertOutput(
            "Reading model from models/river_crossing.als...done.",
            "Reading model from models/musical_chairs.als...done.",
            "Reading model from models/river_crossing.als...done."
        );
    }

    private void runALDB(String... inputLines) throws Exception {
        Process aldb = Runtime.getRuntime().exec("java -jar dist/aldb.jar");
        OutputStream aldbIn = aldb.getOutputStream();
        String input = String.join("\n", inputLines) + "\n";
        aldbIn.write(input.getBytes());
        aldbIn.close();

        BufferedReader aldbOut = new BufferedReader(new InputStreamReader(aldb.getInputStream()));
        String line;
        while ((line = aldbOut.readLine()) != null) {
            aldbOutput.append(line + "\n");
        }
        aldbOutput.deleteCharAt(aldbOutput.length() - 1);

        aldb.waitFor();
    }

    private void assertOutput(String... outputLines) {
        assertEquals(String.join("\n", outputLines) + "\n", aldbOutput.toString().replace(PROMPT, ""));
    }
}
