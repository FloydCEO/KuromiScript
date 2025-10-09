// src/interpreter/Environment.java
package interpreter;

import java.util.HashMap;
import java.util.Map;

public class Environment {
    private final Map<String, KuromiValue> values = new HashMap<>();

    public void define(String name, KuromiValue value) {
        values.put(name, value);
    }

    public KuromiValue get(String name) {
        if (values.containsKey(name)) {
            return values.get(name);
        }
        throw new RuntimeException("Undefined variable '" + name + "'.");
    }
}