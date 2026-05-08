package com.perplexinggames.ironsoul.input.command;

import com.perplexinggames.ironsoul.input.binding.InputActionEvent;

import java.util.Optional;

public interface InputCommandFactory {
    Optional<Command> create(InputActionEvent event, CommandContext context);
}
