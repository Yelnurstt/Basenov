package com.perplexinggames.ironsoul.tank.combat;

import com.badlogic.gdx.math.MathUtils;
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

    // Возвращаем обычный выстрел для PrimaryFireCommand (по клику мыши)
    public void firePrimary() {
        if (!tankController.canUseCombatActions() || primaryCooldownRemaining > 0f) return;
        primaryCooldownRemaining = 0.5f;

        // Для обычного клика задаем фиксированную среднюю скорость
        float speed = 800f;
        spawnProjectileAndApplyRecoil(speed);

        lastCombatAction = "Primary cannon fired";
    }

    // Возвращаем метод для SecondaryFireCommand, чтобы не было ошибки компиляции
    public void fireSecondary() {
        if (!tankController.canUseCombatActions() || secondaryCooldownRemaining > 0f) return;
        secondaryCooldownRemaining = 0.8f;
        lastCombatAction = "Secondary salvo launched";
    }

    // Выстрел с зажатием кнопки
    public void beginCharge() {
        if (!tankController.canUseCombatActions() || charging || primaryCooldownRemaining > 0f) return;
        charging = true;
        chargeTime = 0f;
        lastCombatAction = "Charging main gun...";
    }

    public void releaseCharge() {
        if (!charging) return;
        charging = false;
        primaryCooldownRemaining = 0.5f;

        float chargeFactor = MathUtils.clamp(chargeTime, 0.2f, 1.5f) / 1.5f;
        float speed = 400f + (chargeFactor * 800f);

        spawnProjectileAndApplyRecoil(speed);

        lastCombatAction = "Fired with power: " + (int)(chargeFactor * 100) + "%";
        chargeTime = 0f;
    }

    // Вспомогательный метод, чтобы не дублировать код спавна пули и отдачи
    private void spawnProjectileAndApplyRecoil(float speed) {
        float bodyRotation = tankController.getModel().getRotationDegrees();
        float facingAngle = tankController.getFacingDirection().sign() > 0f ? 0f : 180f;

        float turretAngle = bodyRotation + facingAngle;
        if (aimDirection == AimDirection.UP) turretAngle += (facingAngle == 0 ? 45f : -45f);
        if (aimDirection == AimDirection.DOWN) turretAngle += (facingAngle == 0 ? -25f : 25f);

        float dirX = MathUtils.cosDeg(turretAngle);
        float dirY = MathUtils.sinDeg(turretAngle);

        float startX = tankController.getModel().getX() + tankController.getModel().getWidth() * 0.5f + (dirX * 30f);
        float startY = tankController.getModel().getY() + tankController.getModel().getHeight() * 0.7f + (dirY * 30f);

        if (projectileSystem != null) {
            projectileSystem.spawnPhysicsShot(startX, startY, dirX, dirY, speed);
        }

        float recoilForce = speed * 0.3f;
        tankController.getModel().setVelocityX(tankController.getModel().getVelocityX() - (dirX * recoilForce));
    }

    public void setAimDirection(AimDirection aimDirection) {
        this.aimDirection = aimDirection;
    }

    public void resetAimDirection() {
        this.aimDirection = AimDirection.FORWARD;
    }

    public AimDirection getAimDirection() { return aimDirection; }
    public boolean isCharging() { return charging; }
    public float getChargeTime() { return chargeTime; }
    public String getLastCombatAction() { return lastCombatAction; }
}
