package com.perplexinggames.ironsoul.tank.state;

import com.perplexinggames.ironsoul.tank.controller.TankController;

public interface TankControlState {
    TankControlStateId getId();

    String getName();

    void enter(TankController controller);

    void exit(TankController controller);

    void update(TankController controller, float delta);

    void onJumpRequested(TankController controller);

    void onDashRequested(TankController controller);

    void onCrouchChanged(TankController controller, boolean crouching);

    void onMeleeRamRequested(TankController controller);

    boolean allowsCombat();

    boolean allowsShield();
}
