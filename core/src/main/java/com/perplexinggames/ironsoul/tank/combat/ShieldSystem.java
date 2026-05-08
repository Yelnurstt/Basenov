package com.perplexinggames.ironsoul.tank.combat;

import com.perplexinggames.ironsoul.tank.controller.TankController;

public class ShieldSystem {
    private final TankController tankController;
    private boolean raised;
    private String status = "Shield standby";

    public ShieldSystem(TankController tankController) {
        this.tankController = tankController;
    }

    public void setRaised(boolean raised) {
        if (raised && !tankController.canUseShield()) {
            return;
        }
        this.raised = raised;
        status = raised ? "Shield projected" : "Shield retracted";
    }

    public boolean isRaised() {
        return raised;
    }

    public String getStatus() {
        return status;
    }
}
