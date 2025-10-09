// src/runtime/BuiltInFunctions.java
package runtime;

import interpreter.Interpreter;
import interpreter.KuromiValue;

import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

public class BuiltInFunctions {
    private final Map<String, Function> functions = new HashMap<>();
    private final Interpreter interpreter;
    private final Random random = new Random();

    public interface Function {
        KuromiValue call(Interpreter interpreter, KuromiValue[] arguments);
    }

    public BuiltInFunctions(Interpreter interpreter) {
        this.interpreter = interpreter;
        registerFunctions();
    }

    private void registerFunctions() {
        functions.put("random", args -> {
            if (args.length != 2) throw new RuntimeException("random(min, max) expects 2 arguments.");
            double min = args[0].asNumber();
            double max = args[1].asNumber();
            return KuromiValue.number(min + (max - min) * random.nextDouble());
        });

        functions.put("isKeyPressed", args -> {
            if (args.length != 1) throw new RuntimeException("isKeyPressed(key) expects 1 argument.");
            String key = args[0].asString().toUpperCase();
            return KuromiValue.bool(interpreter.isKeyPressed(key));
        });

        functions.put("moveSprite", args -> {
            if (args.length != 3) throw new RuntimeException("moveSprite(name, dx, dy) expects 3 arguments.");
            String name = args[0].asString();
            double dx = args[1].asNumber();
            double dy = args[2].asNumber();
            interpreter.moveSprite(name, dx, dy);
            return KuromiValue.nullValue();
        });
    }

    public KuromiValue call(String name, KuromiValue[] arguments) {
        Function func = functions.get(name);
        if (func == null) throw new RuntimeException("Undefined function: " + name);
        return func.call(interpreter, arguments);
    }

    public void registerKeyListener(Interpreter.GameWindow window) {
        window.addKeyListener(new KeyListener() {
            @Override
            public void keyTyped(KeyEvent e) {}

            @Override
            public void keyPressed(KeyEvent e) {
                interpreter.setKeyState(KeyEvent.getKeyText(e.getKeyCode()), true);
            }

            @Override
            public void keyReleased(KeyEvent e) {
                interpreter.setKeyState(KeyEvent.getKeyText(e.getKeyCode()), false);
            }
        });
    }
}