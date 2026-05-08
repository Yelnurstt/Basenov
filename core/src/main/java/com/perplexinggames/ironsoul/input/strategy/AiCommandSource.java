package com.perplexinggames.ironsoul.input.strategy;

import com.perplexinggames.ironsoul.input.binding.InputActionEvent;
import com.perplexinggames.ironsoul.input.command.CommandContext;

import java.util.List;

public interface AiCommandSource {
    List<InputActionEvent> nextActions(float delta, CommandContext context);
}
