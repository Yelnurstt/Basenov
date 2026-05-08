package com.perplexinggames.ironsoul.input.command.commands;

import com.perplexinggames.ironsoul.core.gameplay.OverlayController;
import com.perplexinggames.ironsoul.core.gamestate.GameStateManager;
import com.perplexinggames.ironsoul.input.command.Command;

public class ToggleInventoryCommand implements Command {
    private final OverlayController overlayController;
    private final GameStateManager gameStateManager;

    public ToggleInventoryCommand(OverlayController overlayController, GameStateManager gameStateManager) {
        this.overlayController = overlayController;
        this.gameStateManager = gameStateManager;
    }

    @Override
    public void execute() {
        overlayController.toggleInventory(gameStateManager);
    }
}
