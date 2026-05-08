package com.perplexinggames.ironsoul.input.command.commands;

import com.perplexinggames.ironsoul.input.command.Command;
import com.perplexinggames.ironsoul.tank.combat.WeaponSystem;
import com.perplexinggames.ironsoul.tank.controller.AimDirection;

public class AimDownCommand implements Command {
    private final WeaponSystem weaponSystem;
    private final AimDirection aimDirection;

    public AimDownCommand(WeaponSystem weaponSystem, AimDirection aimDirection) {
        this.weaponSystem = weaponSystem;
        this.aimDirection = aimDirection;
    }

    @Override
    public void execute() {
        weaponSystem.setAimDirection(aimDirection);
    }
}
