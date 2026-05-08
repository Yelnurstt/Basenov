package com.perplexinggames.ironsoul.tank.state;

import com.perplexinggames.ironsoul.tank.controller.TankController;

public class GroundedState extends AbstractTankControlState {
    public GroundedState() {
        super(TankControlStateId.GROUNDED, "Grounded");
    }

    @Override
    public void enter(TankController controller) {
        controller.snapToGround();
    }

    @Override
    public void update(TankController controller, float delta) {
        controller.applyHorizontalMovement(
            delta,
            controller.getConfig().getMoveAcceleration(),
            controller.getConfig().getGroundDeceleration()
        );
        controller.snapToGround();
        controller.integrateHorizontal(delta);

        if (!controller.isGrounded()) {
            controller.changeState(TankControlStateId.AIRBORNE);
        }
    }

    @Override
    public void onJumpRequested(TankController controller) {
        controller.launchJump();
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
