package com.perplexinggames.ironsoul.input.command.commands;

import com.perplexinggames.ironsoul.input.command.Command;
import com.perplexinggames.ironsoul.tank.controller.TankController;

public class JumpCommand implements Command {
    private final TankController tankController;

    public JumpCommand(TankController tankController) {
        this.tankController = tankController;
    }

    @Override
    public void execute() {
        tankController.requestJump();
    }
}
