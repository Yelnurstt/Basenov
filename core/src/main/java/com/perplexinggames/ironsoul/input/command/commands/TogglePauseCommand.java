package com.perplexinggames.ironsoul.input.command.commands;

import com.perplexinggames.ironsoul.core.gamestate.GameStateManager;
import com.perplexinggames.ironsoul.input.command.Command;

public class TogglePauseCommand implements Command {
    private final GameStateManager gameStateManager;

    public TogglePauseCommand(GameStateManager gameStateManager) {
        this.gameStateManager = gameStateManager;
    }

    @Override
    public void execute() {
        gameStateManager.togglePause();
    }
}
