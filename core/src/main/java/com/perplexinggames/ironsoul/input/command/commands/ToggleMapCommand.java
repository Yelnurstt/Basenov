package com.perplexinggames.ironsoul.input.command.commands;

import com.perplexinggames.ironsoul.core.gameplay.OverlayController;
import com.perplexinggames.ironsoul.input.command.Command;

public class ToggleMapCommand implements Command {
    private final OverlayController overlayController;

    public ToggleMapCommand(OverlayController overlayController) {
        this.overlayController = overlayController;
    }

    @Override
    public void execute() {
        overlayController.toggleMap();
    }
}
