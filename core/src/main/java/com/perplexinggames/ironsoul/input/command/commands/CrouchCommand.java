package com.perplexinggames.ironsoul.input.command.commands;

import com.perplexinggames.ironsoul.input.command.Command;
import com.perplexinggames.ironsoul.tank.controller.TankController;

public class CrouchCommand implements Command {
    private final TankController tankController;
    private final boolean crouching;

    public CrouchCommand(TankController tankController, boolean crouching) {
        this.tankController = tankController;
        this.crouching = crouching;
    }

    @Override
    public void execute() {
        tankController.setCrouching(crouching);
    }
}
