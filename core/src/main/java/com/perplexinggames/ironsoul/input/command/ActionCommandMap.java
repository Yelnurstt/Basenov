package com.perplexinggames.ironsoul.input.command;

import com.perplexinggames.ironsoul.input.binding.InputAction;
import com.perplexinggames.ironsoul.input.binding.InputActionEvent;

import java.util.EnumMap;
import java.util.Map;
import java.util.Optional;

public class ActionCommandMap implements InputCommandFactory {
    private final Map<InputAction, ActionCommandBinding> bindings = new EnumMap<>(InputAction.class);

    public ActionCommandMap register(InputAction action, ActionCommandBinding binding) {
        bindings.put(action, binding);
        return this;
    }

    @Override
    public Optional<Command> create(InputActionEvent event, CommandContext context) {
        ActionCommandBinding binding = bindings.get(event.action());
        if (binding == null) {
            return Optional.empty();
        }
        return binding.create(event.phase(), context);
    }
}
