package com.perplexinggames.ironsoul.input.command.commands;

import com.perplexinggames.ironsoul.input.command.Command;
import com.perplexinggames.ironsoul.tank.controller.HorizontalDirection;
import com.perplexinggames.ironsoul.tank.controller.TankController;

public class MoveRightCommand implements Command {
    private final TankController tankController;

    public MoveRightCommand(TankController tankController) {
        this.tankController = tankController;
    }

    @Override
    public void execute() {
        tankController.beginMove(HorizontalDirection.RIGHT);
    }
}
