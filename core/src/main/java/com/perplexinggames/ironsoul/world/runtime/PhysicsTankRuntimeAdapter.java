package com.perplexinggames.ironsoul.world.runtime;

import com.badlogic.gdx.math.Rectangle;
import com.perplexinggames.ironsoul.entities.PhysicsTank;
import com.perplexinggames.ironsoul.tank.controller.FacingDirection;

public class PhysicsTankRuntimeAdapter implements TankRuntimeAdapter {
    private final PhysicsTank tank;

    public PhysicsTankRuntimeAdapter(PhysicsTank tank) {
        this.tank = tank;
    }

    @Override
    public Rectangle getBounds() {
        return tank.getBounds();
    }

    @Override
    public void setPosition(float x, float y) {
        tank.setWorldPosition(x, y);
    }

    @Override
    public void setFacingDirection(FacingDirection facingDirection) {
        tank.setFacingDirection(facingDirection);
    }

    @Override
    public float getWidth() {
        return tank.getBodyWidth();
    }

    @Override
    public float getHeight() {
        return tank.getBodyHeight();
    }
}
