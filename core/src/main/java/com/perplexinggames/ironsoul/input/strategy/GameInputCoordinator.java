package com.perplexinggames.ironsoul.input.strategy;

import com.perplexinggames.ironsoul.input.command.CommandContext;
import com.perplexinggames.ironsoul.input.command.CommandExecutor;
import com.perplexinggames.ironsoul.input.command.CommandQueue;

public class GameInputCoordinator {
    private final CommandQueue commandQueue = new CommandQueue();
    private final CommandExecutor commandExecutor = new CommandExecutor();
    private final CommandContext commandContext;
    private InputStrategy activeStrategy;

    public GameInputCoordinator(InputStrategy activeStrategy, CommandContext commandContext) {
        this.activeStrategy = activeStrategy;
        this.commandContext = commandContext;
    }

    public void update(float delta) {
        activeStrategy.collectCommands(commandQueue, delta, commandContext);
        commandExecutor.flush(commandQueue);
    }

    public void setActiveStrategy(InputStrategy activeStrategy) {
        this.activeStrategy = activeStrategy;
        commandQueue.clear();
    }

    public String getActiveStrategyName() {
        return activeStrategy.getName();
    }

    public String getLastExecutedCommand() {
        return commandExecutor.getLastExecutedCommand();
    }
}
