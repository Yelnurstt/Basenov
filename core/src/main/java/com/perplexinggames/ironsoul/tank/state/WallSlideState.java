package com.perplexinggames.ironsoul.tank.state;

import com.perplexinggames.ironsoul.tank.controller.TankController;

public class WallSlideState extends AbstractTankControlState {
    public WallSlideState() {
        super(TankControlStateId.WALL_SLIDE, "Wall Slide");
    }

    @Override
    public void update(TankController controller, float delta) {
        controller.applyWallSlide(delta);
        controller.integrate(delta);

        if (controller.isGrounded()) {
            controller.changeState(TankControlStateId.GROUNDED);
            return;
        }

        if (!controller.isEligibleForWallSlide()) {
            controller.changeState(TankControlStateId.AIRBORNE);
        }
    }

    @Override
    public void onJumpRequested(TankController controller) {
        controller.launchWallJump();
        controller.changeState(TankControlStateId.AIRBORNE);
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
