package com.perplexinggames.ironsoul.input.command;

public class CommandExecutor {
    private String lastExecutedCommand = "None";

    public void flush(CommandQueue queue) {
        while (!queue.isEmpty()) {
            Command command = queue.poll();
            if (command != null) {
                command.execute();
                lastExecutedCommand = command.description();
            }
        }
    }

    public String getLastExecutedCommand() {
        return lastExecutedCommand;
    }
}
