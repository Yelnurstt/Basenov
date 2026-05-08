package com.perplexinggames.ironsoul.input.command;

public class CommandExecutor {
    public void flush(CommandQueue queue) {
        while (!queue.isEmpty()) {
            Command command = queue.poll();
            if (command != null) {
                command.execute();
            }
        }
    }
}
