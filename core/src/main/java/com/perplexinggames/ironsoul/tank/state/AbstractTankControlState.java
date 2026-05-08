package com.perplexinggames.ironsoul.tank.state;

import com.perplexinggames.ironsoul.tank.controller.TankController;

public abstract class AbstractTankControlState implements TankControlState {
    private final TankControlStateId id;
    private final String name;

    protected AbstractTankControlState(TankControlStateId id, String name) {
        this.id = id;
        this.name = name;
    }

    @Override
    public TankControlStateId getId() {
        return id;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public void enter(TankController controller) {
    }

    @Override
    public void exit(TankController controller) {
    }

    @Override
    public void onJumpRequested(TankController controller) {
    }

    @Override
    public void onDashRequested(TankController controller) {
    }

    @Override
    public void onCrouchChanged(TankController controller, boolean crouching) {
    }

    @Override
    public void onMeleeRamRequested(TankController controller) {
    }

    @Override
    public boolean allowsCombat() {
        return true;
    }

    @Override
    public boolean allowsShield() {
        return true;
    }
}
