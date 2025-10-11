// src/interpreter/KuromiValue.java
package interpreter;

import java.awt.image.BufferedImage;
import java.util.List;

public class KuromiValue {
    public enum Type {
        NUMBER, STRING, IMAGE, BOOLEAN, ARRAY, NULL
    }

    private final Type type;
    private final Object value;

    public KuromiValue(Type type, Object value) {
        this.type = type;
        this.value = value;
    }

    public Type getType() {
        return type;
    }

    public Object getValue() {
        return value;
    }

    public boolean isNull() {
        return type == Type.NULL;
    }

    public double asNumber() {
        if (type == Type.NUMBER) return (Double) value;
        throw new RuntimeException("Value is not a number.");
    }

    public String asString() {
        if (type == Type.STRING) return (String) value;
        if (type == Type.NUMBER) return String.valueOf(value);
        if (type == Type.BOOLEAN) return String.valueOf(value);
        throw new RuntimeException("Value is not a string.");
    }

    public BufferedImage asImage() {
        if (type == Type.IMAGE) return (BufferedImage) value;
        throw new RuntimeException("Value is not an image.");
    }

    public boolean asBoolean() {
        if (type == Type.BOOLEAN) return (Boolean) value;
        if (type == Type.NULL) return false;
        if (type == Type.NUMBER) return (Double) value != 0;
        if (type == Type.STRING) return !((String) value).isEmpty();
        return true;
    }

    @SuppressWarnings("unchecked")
    public List<KuromiValue> asArray() {
        if (type == Type.ARRAY) return (List<KuromiValue>) value;
        throw new RuntimeException("Value is not an array.");
    }

    public static KuromiValue nullValue() {
        return new KuromiValue(Type.NULL, null);
    }

    public static KuromiValue number(double num) {
        return new KuromiValue(Type.NUMBER, num);
    }

    public static KuromiValue string(String str) {
        return new KuromiValue(Type.STRING, str);
    }

    public static KuromiValue image(BufferedImage img) {
        return new KuromiValue(Type.IMAGE, img);
    }

    public static KuromiValue bool(boolean b) {
        return new KuromiValue(Type.BOOLEAN, b);
    }

    public static KuromiValue array(List<KuromiValue> arr) {
        return new KuromiValue(Type.ARRAY, arr);
    }

    @Override
    public String toString() {
        if (type == Type.NULL) return "null";
        if (type == Type.ARRAY) {
            StringBuilder sb = new StringBuilder("[");
            List<KuromiValue> arr = asArray();
            for (int i = 0; i < arr.size(); i++) {
                sb.append(arr.get(i).toString());
                if (i < arr.size() - 1) sb.append(", ");
            }
            sb.append("]");
            return sb.toString();
        }
        return value != null ? value.toString() : "null";
    }
}