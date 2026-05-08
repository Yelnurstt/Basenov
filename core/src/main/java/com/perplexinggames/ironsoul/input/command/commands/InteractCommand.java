package com.perplexinggames.ironsoul.input.command.commands;

import com.perplexinggames.ironsoul.core.gameplay.InteractionSystem;
import com.perplexinggames.ironsoul.input.command.Command;

public class InteractCommand implements Command {
    private final InteractionSystem interactionSystem;

    public InteractCommand(InteractionSystem interactionSystem) {
        this.interactionSystem = interactionSystem;
    }

    @Override
    public void execute() {
        interactionSystem.interact();
    }
}
