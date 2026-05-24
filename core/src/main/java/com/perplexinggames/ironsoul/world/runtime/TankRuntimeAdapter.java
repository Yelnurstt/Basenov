package com.perplexinggames.ironsoul.world.runtime;

import com.badlogic.gdx.math.Rectangle;
import com.perplexinggames.ironsoul.tank.controller.FacingDirection;

public interface TankRuntimeAdapter {
    Rectangle getBounds();

    void setPosition(float x, float y);

    void setFacingDirection(FacingDirection facingDirection);

    float getWidth();

    float getHeight();
}
