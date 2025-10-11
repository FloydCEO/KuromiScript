// src/interpreter/Environment.java
package interpreter;

import runtime.Value;
import java.util.HashMap;
import java.util.Map;

public class Environment {
    private final Map<String, Value> values = new HashMap<>();
    private final Environment enclosing;

    public Environment() {
        this.enclosing = null;
    }

    public Environment(Environment enclosing) {
        this.enclosing = enclosing;
    }

    public void define(String name, Value value) {
        values.put(name, value);
    }

    public void set(String name, Value value) {
        if (values.containsKey(name)) {
            values.put(name, value);
            return;
        }
        if (enclosing != null) {
            enclosing.set(name, value);
            return;
        }
        throw new RuntimeException("Undefined variable '" + name + "'.");
    }

    public Value get(String name) {
        if (values.containsKey(name)) {
            return values.get(name);
        }
        if (enclosing != null) {
            return enclosing.get(name);
        }
        throw new RuntimeException("Undefined variable '" + name + "'.");
    }
}