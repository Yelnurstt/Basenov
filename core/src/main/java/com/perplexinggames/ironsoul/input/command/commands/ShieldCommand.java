package com.perplexinggames.ironsoul.input.command.commands;

import com.perplexinggames.ironsoul.input.command.Command;
import com.perplexinggames.ironsoul.tank.combat.ShieldSystem;

public class ShieldCommand implements Command {
    private final ShieldSystem shieldSystem;
    private final boolean raised;

    public ShieldCommand(ShieldSystem shieldSystem, boolean raised) {
        this.shieldSystem = shieldSystem;
        this.raised = raised;
    }

    @Override
    public void execute() {
        shieldSystem.setRaised(raised);
    }
}
