package com.perplexinggames.ironsoul.tank.state;

import com.perplexinggames.ironsoul.tank.controller.TankController;

public class AirborneState extends AbstractTankControlState {
    public AirborneState() {
        super(TankControlStateId.AIRBORNE, "Airborne");
    }

    @Override
    public void update(TankController controller, float delta) {
        controller.applyHorizontalMovement(
            delta,
            controller.getConfig().getAirAcceleration(),
            controller.getConfig().getAirDeceleration()
        );
        controller.applyGravity(delta);
        controller.integrate(delta);

        if (controller.isGrounded()) {
            controller.changeState(TankControlStateId.GROUNDED);
            return;
        }

        if (controller.isEligibleForWallSlide()) {
            controller.changeState(TankControlStateId.WALL_SLIDE);
        }
    }

    @Override
    public void onDashRequested(TankController controller) {
        controller.startDash();
    }

    @Override
    public void onMeleeRamRequested(TankController controller) {
        controller.performRamImpulse();
    }
}
