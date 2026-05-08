package com.perplexinggames.ironsoul.input.state;

import com.perplexinggames.ironsoul.input.binding.InputAction;
import com.perplexinggames.ironsoul.input.binding.InputActionEvent;
import com.perplexinggames.ironsoul.input.binding.InputPhase;
import com.perplexinggames.ironsoul.input.command.Command;
import com.perplexinggames.ironsoul.input.command.CommandContext;
import com.perplexinggames.ironsoul.input.command.InputCommandFactory;

import java.util.EnumSet;
import java.util.Optional;
import java.util.Set;

public class OverlayInputMode implements InputMode {
    private final Set<InputAction> allowedActions;

    public OverlayInputMode(InputAction... allowedActions) {
        this.allowedActions = EnumSet.noneOf(InputAction.class);
        for (InputAction action : allowedActions) {
            this.allowedActions.add(action);
        }
    }

    @Override
    public Optional<Command> map(InputActionEvent event, InputCommandFactory factory, CommandContext context) {
        if (event.phase() == InputPhase.END || allowedActions.contains(event.action())) {
            return factory.create(event, context);
        }
        return Optional.empty();
    }
}
