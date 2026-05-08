package com.perplexinggames.ironsoul.input.command.commands;

import com.perplexinggames.ironsoul.input.command.Command;
import com.perplexinggames.ironsoul.tank.controller.TankController;

public class DashCommand implements Command {
    private final TankController tankController;

    public DashCommand(TankController tankController) {
        this.tankController = tankController;
    }

    @Override
    public void execute() {
        tankController.requestDash();
    }
}
