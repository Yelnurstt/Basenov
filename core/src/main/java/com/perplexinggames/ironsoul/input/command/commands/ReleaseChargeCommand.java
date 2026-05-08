package com.perplexinggames.ironsoul.input.command.commands;

import com.perplexinggames.ironsoul.input.command.Command;
import com.perplexinggames.ironsoul.tank.combat.WeaponSystem;

public class ReleaseChargeCommand implements Command {
    private final WeaponSystem weaponSystem;

    public ReleaseChargeCommand(WeaponSystem weaponSystem) {
        this.weaponSystem = weaponSystem;
    }

    @Override
    public void execute() {
        weaponSystem.releaseCharge();
    }
}
