package com.perplexinggames.ironsoul.input.adapter;

import java.util.Collections;
import java.util.Set;

public class ControlInputFrame<C extends Enum<C>> {
    private final Set<C> pressed;
    private final Set<C> held;
    private final Set<C> released;

    public ControlInputFrame(Set<C> pressed, Set<C> held, Set<C> released) {
        this.pressed = Collections.unmodifiableSet(pressed);
        this.held = Collections.unmodifiableSet(held);
        this.released = Collections.unmodifiableSet(released);
    }

    public Set<C> getPressed() {
        return pressed;
    }

    public Set<C> getHeld() {
        return held;
    }

    public Set<C> getReleased() {
        return released;
    }
}
