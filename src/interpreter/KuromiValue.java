// src/interpreter/KuromiValue.java
package interpreter;

import java.awt.image.BufferedImage;

public class KuromiValue {
    public enum Type {
        NUMBER, STRING, IMAGE, BOOLEAN, NULL
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

    public double asNumber() {
        if (type == Type.NUMBER) return (Double) value;
        throw new RuntimeException("Value is not a number.");
    }

    public String asString() {
        if (type == Type.STRING) return (String) value;
        throw new RuntimeException("Value is not a string.");
    }

    public BufferedImage asImage() {
        if (type == Type.IMAGE) return (BufferedImage) value;
        throw new RuntimeException("Value is not an image.");
    }

    public boolean asBoolean() {
        if (type == Type.BOOLEAN) return (Boolean) value;
        throw new RuntimeException("Value is not a boolean.");
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
}