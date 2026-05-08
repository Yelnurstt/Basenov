package com.perplexinggames.ironsoul.input.state;

import com.perplexinggames.ironsoul.input.binding.InputActionEvent;
import com.perplexinggames.ironsoul.input.command.Command;
import com.perplexinggames.ironsoul.input.command.CommandContext;
import com.perplexinggames.ironsoul.input.command.InputCommandFactory;

import java.util.Optional;

public interface InputMode {
    Optional<Command> map(InputActionEvent event, InputCommandFactory factory, CommandContext context);
}
