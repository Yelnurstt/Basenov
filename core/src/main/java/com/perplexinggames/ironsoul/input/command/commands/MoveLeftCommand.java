package com.perplexinggames.ironsoul.input.command.commands;

import com.perplexinggames.ironsoul.input.command.Command;
import com.perplexinggames.ironsoul.tank.controller.HorizontalDirection;
import com.perplexinggames.ironsoul.tank.controller.TankController;

public class MoveLeftCommand implements Command {
    private final TankController tankController;

    public MoveLeftCommand(TankController tankController) {
        this.tankController = tankController;
    }

    @Override
    public void execute() {
        tankController.beginMove(HorizontalDirection.LEFT);
    }
}
