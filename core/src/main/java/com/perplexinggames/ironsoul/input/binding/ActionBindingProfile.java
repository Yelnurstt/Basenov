package com.perplexinggames.ironsoul.input.binding;

import com.perplexinggames.ironsoul.input.adapter.ControlInputFrame;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class ActionBindingProfile<C extends Enum<C>> {
    private final Class<C> controlType;
    private final EnumMap<InputAction, EnumSet<C>> bindings = new EnumMap<>(InputAction.class);

    public ActionBindingProfile(Class<C> controlType) {
        this.controlType = controlType;
    }

    @SafeVarargs
    public final ActionBindingProfile<C> bind(InputAction action, C... controls) {
        EnumSet<C> boundControls = EnumSet.noneOf(controlType);
        for (C control : controls) {
            boundControls.add(control);
        }
        bindings.put(action, boundControls);
        return this;
    }

    public ActionBindingProfile<C> rebind(InputAction action, Set<C> controls) {
        EnumSet<C> reboundControls = EnumSet.noneOf(controlType);
        reboundControls.addAll(controls);
        bindings.put(action, reboundControls);
        return this;
    }

    public Set<C> getBindings(InputAction action) {
        return bindings.getOrDefault(action, EnumSet.noneOf(controlType));
    }

    public List<InputActionEvent> resolve(ControlInputFrame<C> frame) {
        List<InputActionEvent> events = new ArrayList<>();
        for (Map.Entry<InputAction, EnumSet<C>> entry : bindings.entrySet()) {
            EnumSet<C> controls = entry.getValue();
            addIfTriggered(events, entry.getKey(), InputPhase.START, controls, frame.getPressed());
            addIfTriggered(events, entry.getKey(), InputPhase.ACTIVE, controls, frame.getHeld());
            addIfTriggered(events, entry.getKey(), InputPhase.END, controls, frame.getReleased());
        }
        return events;
    }

    private void addIfTriggered(
        List<InputActionEvent> events,
        InputAction action,
        InputPhase phase,
        Set<C> expectedControls,
        Set<C> currentControls
    ) {
        boolean triggered = expectedControls.stream().anyMatch(currentControls::contains);
        if (triggered) {
            events.add(new InputActionEvent(action, phase));
        }
    }
}
