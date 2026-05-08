package com.perplexinggames.ironsoul.input.command.commands;

import com.perplexinggames.ironsoul.core.gameplay.OverlayController;
import com.perplexinggames.ironsoul.input.command.Command;

public class ToggleInventoryCommand implements Command {
    private final OverlayController overlayController;

    public ToggleInventoryCommand(OverlayController overlayController) {
        this.overlayController = overlayController;
    }

    @Override
    public void execute() {
        overlayController.toggleInventory();
    }
}
