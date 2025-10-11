// src/runtime/Value.java
package runtime;

import parser.ASTNode;
import java.awt.image.BufferedImage;
import java.util.List;

public class Value {
    public enum Type {
        NUMBER, STRING, BOOL, NULL, FUNCTION, IMAGE, ARRAY
    }

    public final Type type;
    public final Object data;

    private Value(Type type, Object data) {
        this.type = type;
        this.data = data;
    }

    public static Value number(double value) {
        return new Value(Type.NUMBER, value);
    }

    public static Value string(String value) {
        return new Value(Type.STRING, value);
    }

    public static Value bool(boolean value) {
        return new Value(Type.BOOL, value);
    }

    public static Value nil() {
        return new Value(Type.NULL, null);
    }

    public static Value function(ASTNode.Function func) {
        return new Value(Type.FUNCTION, func);
    }

    public static Value image(BufferedImage img) {
        return new Value(Type.IMAGE, img);
    }

    public static Value array(List<Value> elements) {
        return new Value(Type.ARRAY, elements);
    }

    public double asNumber() {
        if (type == Type.NUMBER) return (Double) data;
        if (type == Type.BOOL) return ((Boolean) data) ? 1.0 : 0.0;
        throw new RuntimeException("Cannot convert to number");
    }

    public String asString() {
        if (type == Type.STRING) return (String) data;
        if (type == Type.NUMBER) return String.valueOf(data);
        if (type == Type.BOOL) return String.valueOf(data);
        if (type == Type.NULL) return "null";
        return data != null ? data.toString() : "null";
    }

    public boolean asBoolean() {
        if (type == Type.BOOL) return (Boolean) data;
        if (type == Type.NULL) return false;
        if (type == Type.NUMBER) return ((Double) data) != 0;
        if (type == Type.STRING) return !((String) data).isEmpty();
        return true;
    }

    public BufferedImage asImage() {
        if (type == Type.IMAGE) return (BufferedImage) data;
        throw new RuntimeException("Not an image");
    }

    @SuppressWarnings("unchecked")
    public List<Value> asArray() {
        if (type == Type.ARRAY) return (List<Value>) data;
        throw new RuntimeException("Not an array");
    }

    @Override
    public String toString() {
        return asString();
    }
}