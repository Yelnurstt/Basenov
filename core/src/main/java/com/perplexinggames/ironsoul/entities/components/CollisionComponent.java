package com.perplexinggames.ironsoul.entities.components;

import com.badlogic.gdx.math.Rectangle;

public class CollisionComponent {

    private final Rectangle bounds;

    public CollisionComponent(float x, float y, float width, float height) {
        bounds = new Rectangle(x, y, width, height);
    }

    public Rectangle getBounds() {
        return bounds;
    }

    public void updatePosition(float x, float y) {
        bounds.setPosition(x, y);
    }

    public boolean overlaps(Rectangle other) {
        return bounds.overlaps(other);
    }
}
