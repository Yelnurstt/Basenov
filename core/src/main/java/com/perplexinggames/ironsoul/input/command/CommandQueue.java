package com.perplexinggames.ironsoul.input.command;

import java.util.ArrayDeque;
import java.util.Queue;

public class CommandQueue {
    private final Queue<Command> queuedCommands = new ArrayDeque<>();

    public void enqueue(Command command) {
        queuedCommands.add(command);
    }

    public Command poll() {
        return queuedCommands.poll();
    }

    public boolean isEmpty() {
        return queuedCommands.isEmpty();
    }

    public void clear() {
        queuedCommands.clear();
    }
}
