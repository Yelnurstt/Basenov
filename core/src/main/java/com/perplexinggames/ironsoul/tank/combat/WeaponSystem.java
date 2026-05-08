package com.perplexinggames.ironsoul.tank.combat;

import com.perplexinggames.ironsoul.projectile.ProjectileSystem;
import com.perplexinggames.ironsoul.tank.controller.AimDirection;
import com.perplexinggames.ironsoul.tank.controller.TankController;

public class WeaponSystem {
    private final TankController tankController;
    private final ProjectileSystem projectileSystem;
    private AimDirection aimDirection = AimDirection.FORWARD;
    private float primaryCooldownRemaining;
    private float secondaryCooldownRemaining;
    private boolean charging;
    private float chargeTime;
    private String lastCombatAction = "Weapons safe";

    public WeaponSystem(TankController tankController) {
        this(tankController, null);
    }

    public WeaponSystem(TankController tankController, ProjectileSystem projectileSystem) {
        this.tankController = tankController;
        this.projectileSystem = projectileSystem;
    }

    public void update(float delta) {
        primaryCooldownRemaining = Math.max(0f, primaryCooldownRemaining - delta);
        secondaryCooldownRemaining = Math.max(0f, secondaryCooldownRemaining - delta);
        if (charging) {
            chargeTime += delta;
        }
    }

    public void setAimDirection(AimDirection aimDirection) {
        this.aimDirection = aimDirection;
        lastCombatAction = "Turret aimed " + aimDirection.name().toLowerCase();
    }

    public void resetAimDirection() {
        aimDirection = AimDirection.FORWARD;
    }

    public void firePrimary() {
        if (!tankController.canUseCombatActions() || primaryCooldownRemaining > 0f) {
            return;
        }
        primaryCooldownRemaining = 0.35f;
        if (projectileSystem != null) {
            projectileSystem.spawnPrimaryShot(
                tankController.getModel(),
                tankController.getFacingDirection(),
                aimDirection
            );
        }
        lastCombatAction = "Primary cannon fired " + aimDirection.name().toLowerCase();
    }

    public void fireSecondary() {
        if (!tankController.canUseCombatActions() || secondaryCooldownRemaining > 0f) {
            return;
        }
        secondaryCooldownRemaining = 0.8f;
        lastCombatAction = "Secondary salvo launched";
    }

    public void beginCharge() {
        if (!tankController.canUseCombatActions() || charging) {
            return;
        }
        charging = true;
        chargeTime = 0f;
        lastCombatAction = "Charge cycle started";
    }

    public void releaseCharge() {
        if (!charging) {
            return;
        }

        charging = false;
        String chargeClass = chargeTime >= 1.2f ? "heavy" : "light";
        lastCombatAction = "Charged " + chargeClass + " blast released";
        chargeTime = 0f;
    }

    public AimDirection getAimDirection() {
        return aimDirection;
    }

    public boolean isCharging() {
        return charging;
    }

    public float getChargeTime() {
        return chargeTime;
    }

    public String getLastCombatAction() {
        return lastCombatAction;
    }
}
