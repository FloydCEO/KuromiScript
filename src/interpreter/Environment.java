// src/interpreter/Environment.java
package interpreter;

import java.util.HashMap;
import java.util.Map;

public class Environment {
    private final Map<String, KuromiValue> values = new HashMap<>();
    private final Environment enclosing;

    public Environment() {
        this.enclosing = null;
    }

    public Environment(Environment enclosing) {
        this.enclosing = enclosing;
    }

    public void define(String name, KuromiValue value) {
        values.put(name, value);
    }

    public void assign(String name, KuromiValue value) {
        if (values.containsKey(name)) {
            values.put(name, value);
            return;
        }
        if (enclosing != null) {
            enclosing.assign(name, value);
            return;
        }
        throw new RuntimeException("Undefined variable '" + name + "'.");
    }

    public KuromiValue get(String name) {
        if (values.containsKey(name)) {
            return values.get(name);
        }
        if (enclosing != null) {
            return enclosing.get(name);
        }
        throw new RuntimeException("Undefined variable '" + name + "'.");
    }
}