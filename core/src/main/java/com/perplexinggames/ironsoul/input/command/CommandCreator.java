package com.perplexinggames.ironsoul.input.command;

@FunctionalInterface
public interface CommandCreator {
    Command create(CommandContext context);
}
