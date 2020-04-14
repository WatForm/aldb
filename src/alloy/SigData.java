package alloy;

import edu.mit.csail.sdg.ast.Sig;
import edu.mit.csail.sdg.ast.Type;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class SigData {
    private String label;
    // Map of field name to Type.
    private Map<String, Type> data;

    public SigData(Sig sig) {
        label = sig.label;
        data = new HashMap<>();
        for (Sig.Field field : sig.getFields()) {
            data.put(field.label, field.type());
        }
    }

    public String getLabel() {
        return label;
    }

    public Set<String> getFields() {
        return data.keySet();
    }

    public String getTypeForField(String field) {
        Type type = data.get(field);

        if (type == null) {
            return null;
        }

        return type.toString();
    }

    public int getArityForField(String field) {
        Type type = data.get(field);

        if (type == null) {
            return 0;
        }

        // Subtract one from arity to ignore the default relation from the State sig.
        return type.arity() - 1;
    }
}
