package alloy;

import alloy.SigData;

import edu.mit.csail.sdg.ast.Sig;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.Before;
import org.junit.Test;

import java.util.HashSet;
import java.util.Set;

public class TestSigData {
    private SigData sigData;

    @Before
    public void init() {
        Sig sigA = new Sig.PrimSig("A");
        Sig sigB = new Sig.PrimSig("B");
        Sig.Field f1 = sigA.addField("f", sigB.lone_arrow_lone(sigB));
        Sig.Field f2 = sigA.addField("g", sigB);
        sigData = new SigData(sigA);
    }

    @Test
    public void testGetLabel() {
        assertEquals("A", sigData.getLabel());
    }

    @Test
    public void testGetFields() {
        Set<String> expected = new HashSet<>();
        expected.add("f");
        expected.add("g");
        assertEquals(expected, sigData.getFields());
    }

    @Test
    public void testGetTypeForField() {
        assertEquals("{A->B->B}", sigData.getTypeForField("f"));
        assertEquals("{A->B}", sigData.getTypeForField("g"));
    }

    @Test
    public void testGetArityForField() {
        assertEquals(2, sigData.getArityForField("f"));
        assertEquals(1, sigData.getArityForField("g"));
    }
}
