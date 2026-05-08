package com.perplexinggames.ironsoul.input.command;

import com.perplexinggames.ironsoul.input.binding.InputPhase;

import java.util.EnumMap;
import java.util.Optional;

public class ActionCommandBinding {
    private final EnumMap<InputPhase, CommandCreator> phaseBindings = new EnumMap<>(InputPhase.class);

    public ActionCommandBinding on(InputPhase phase, CommandCreator creator) {
        phaseBindings.put(phase, creator);
        return this;
    }

    public Optional<Command> create(InputPhase phase, CommandContext context) {
        CommandCreator creator = phaseBindings.get(phase);
        if (creator == null) {
            return Optional.empty();
        }
        return Optional.of(creator.create(context));
    }
}
