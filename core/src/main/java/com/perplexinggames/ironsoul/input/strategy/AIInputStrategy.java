package com.perplexinggames.ironsoul.input.strategy;

import com.perplexinggames.ironsoul.input.binding.InputActionEvent;
import com.perplexinggames.ironsoul.input.command.CommandContext;
import com.perplexinggames.ironsoul.input.command.CommandQueue;
import com.perplexinggames.ironsoul.input.command.InputCommandFactory;

public class AIInputStrategy implements InputStrategy {
    private final AiCommandSource commandSource;
    private final InputCommandFactory commandFactory;

    public AIInputStrategy(AiCommandSource commandSource, InputCommandFactory commandFactory) {
        this.commandSource = commandSource;
        this.commandFactory = commandFactory;
    }

    @Override
    public void collectCommands(CommandQueue queue, float delta, CommandContext context) {
        for (InputActionEvent event : commandSource.nextActions(delta, context)) {
            context.getGameStateManager()
                .getCurrentState()
                .getInputMode()
                .map(event, commandFactory, context)
                .ifPresent(queue::enqueue);
        }
    }

    @Override
    public String getName() {
        return "AI";
    }
}
