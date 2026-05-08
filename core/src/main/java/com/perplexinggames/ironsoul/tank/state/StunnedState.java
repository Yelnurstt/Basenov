package com.perplexinggames.ironsoul.tank.state;

import com.perplexinggames.ironsoul.tank.controller.TankController;

public class StunnedState extends AbstractTankControlState {
    private float remainingTime;

    public StunnedState() {
        super(TankControlStateId.STUNNED, "Stunned");
    }

    @Override
    public void enter(TankController controller) {
        remainingTime = controller.getConfig().getStunDuration();
    }

    @Override
    public void update(TankController controller, float delta) {
        remainingTime -= delta;
        controller.applyHorizontalDecay(delta, controller.getConfig().getGroundDeceleration() * 0.5f);
        controller.applyGravity(delta);
        controller.integrate(delta);

        if (remainingTime <= 0f) {
            controller.changeState(controller.isGrounded() ? TankControlStateId.GROUNDED : TankControlStateId.AIRBORNE);
        }
    }

    @Override
    public boolean allowsCombat() {
        return false;
    }

    @Override
    public boolean allowsShield() {
        return false;
    }
}
