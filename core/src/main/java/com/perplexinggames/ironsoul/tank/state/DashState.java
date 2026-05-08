package com.perplexinggames.ironsoul.tank.state;

import com.perplexinggames.ironsoul.tank.controller.TankController;

public class DashState extends AbstractTankControlState {
    private float remainingTime;

    public DashState() {
        super(TankControlStateId.DASH, "Dash");
    }

    @Override
    public void enter(TankController controller) {
        remainingTime = controller.getConfig().getDashDuration();
        controller.beginDashMotion();
    }

    @Override
    public void update(TankController controller, float delta) {
        remainingTime -= delta;
        controller.integrate(delta);

        if (remainingTime <= 0f) {
            controller.endDashMotion();
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
