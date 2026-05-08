package com.perplexinggames.ironsoul.input.command.commands;

import com.perplexinggames.ironsoul.input.command.Command;
import com.perplexinggames.ironsoul.tank.controller.HorizontalDirection;
import com.perplexinggames.ironsoul.tank.controller.TankController;

public class StopMoveCommand implements Command {
    private final TankController tankController;
    private final HorizontalDirection direction;

    public StopMoveCommand(TankController tankController, HorizontalDirection direction) {
        this.tankController = tankController;
        this.direction = direction;
    }

    @Override
    public void execute() {
        tankController.endMove(direction);
    }
}
