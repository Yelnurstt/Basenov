package com.perplexinggames.ironsoul.input.adapter;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;

import java.util.EnumMap;
import java.util.EnumSet;
import java.util.Map;

public class LibGdxInputAdapter implements DeviceInputAdapter<KeyboardControl> {
    private final Map<KeyboardControl, Integer> keyCodes = new EnumMap<>(KeyboardControl.class);
    private EnumSet<KeyboardControl> previousHeld = EnumSet.noneOf(KeyboardControl.class);

    public LibGdxInputAdapter() {
        keyCodes.put(KeyboardControl.A, Input.Keys.A);
        keyCodes.put(KeyboardControl.D, Input.Keys.D);
        keyCodes.put(KeyboardControl.W, Input.Keys.W);
        keyCodes.put(KeyboardControl.S, Input.Keys.S);
        keyCodes.put(KeyboardControl.SPACE, Input.Keys.SPACE);
        keyCodes.put(KeyboardControl.SHIFT_LEFT, Input.Keys.SHIFT_LEFT);
        keyCodes.put(KeyboardControl.CONTROL_LEFT, Input.Keys.CONTROL_LEFT);
        keyCodes.put(KeyboardControl.Q, Input.Keys.Q);
        keyCodes.put(KeyboardControl.E, Input.Keys.E);
        keyCodes.put(KeyboardControl.F, Input.Keys.F);
        keyCodes.put(KeyboardControl.I, Input.Keys.I);
        keyCodes.put(KeyboardControl.J, Input.Keys.J);
        keyCodes.put(KeyboardControl.K, Input.Keys.K);
        keyCodes.put(KeyboardControl.L, Input.Keys.L);
        keyCodes.put(KeyboardControl.M, Input.Keys.M);
        keyCodes.put(KeyboardControl.ESCAPE, Input.Keys.ESCAPE);
        keyCodes.put(KeyboardControl.ENTER, Input.Keys.ENTER);
    }

    @Override
    public ControlInputFrame<KeyboardControl> poll() {
        EnumSet<KeyboardControl> currentHeld = EnumSet.noneOf(KeyboardControl.class);
        for (Map.Entry<KeyboardControl, Integer> entry : keyCodes.entrySet()) {
            if (Gdx.input.isKeyPressed(entry.getValue())) {
                currentHeld.add(entry.getKey());
            }
        }

        EnumSet<KeyboardControl> pressed = currentHeld.clone();
        pressed.removeAll(previousHeld);

        EnumSet<KeyboardControl> released = previousHeld.clone();
        released.removeAll(currentHeld);

        ControlInputFrame<KeyboardControl> frame =
            new ControlInputFrame<>(pressed, currentHeld.clone(), released);
        previousHeld = currentHeld;
        return frame;
    }
}
