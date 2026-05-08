package com.perplexinggames.ironsoul.input.command;

public interface Command {
    void execute();

    default String description() {
        return getClass().getSimpleName();
    }
}
