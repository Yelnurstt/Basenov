package com.perplexinggames.ironsoul.input.strategy;

import com.perplexinggames.ironsoul.input.command.CommandContext;
import com.perplexinggames.ironsoul.input.command.CommandQueue;

public interface InputStrategy {
    void collectCommands(CommandQueue queue, float delta, CommandContext context);

    String getName();
}
