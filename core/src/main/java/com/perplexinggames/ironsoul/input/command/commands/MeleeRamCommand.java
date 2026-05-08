package com.perplexinggames.ironsoul.input.command.commands;

import com.perplexinggames.ironsoul.input.command.Command;
import com.perplexinggames.ironsoul.tank.controller.TankController;

public class MeleeRamCommand implements Command {
    private final TankController tankController;

    public MeleeRamCommand(TankController tankController) {
        this.tankController = tankController;
    }

    @Override
    public void execute() {
        tankController.requestMeleeRam();
    }
}
