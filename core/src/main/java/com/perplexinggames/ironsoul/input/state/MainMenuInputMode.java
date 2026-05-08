package com.perplexinggames.ironsoul.input.state;

import com.perplexinggames.ironsoul.input.binding.InputActionEvent;
import com.perplexinggames.ironsoul.input.binding.InputPhase;
import com.perplexinggames.ironsoul.input.command.Command;
import com.perplexinggames.ironsoul.input.command.CommandContext;
import com.perplexinggames.ironsoul.input.command.InputCommandFactory;

import java.util.Optional;

public class MainMenuInputMode implements InputMode {
    @Override
    public Optional<Command> map(InputActionEvent event, InputCommandFactory factory, CommandContext context) {
        if (event.phase() == InputPhase.END) {
            return factory.create(event, context);
        }
        return Optional.empty();
    }
}
