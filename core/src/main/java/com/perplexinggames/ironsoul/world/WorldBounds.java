package com.perplexinggames.ironsoul.world;

import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Rectangle;

public class WorldBounds {
    public float x;
    public float y;
    public float width;
    public float height;

    public WorldBounds() {
    }

    public WorldBounds(float x, float y, float width, float height) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
    }

    public WorldBounds copy() {
        return new WorldBounds(x, y, width, height);
    }

    public boolean isEmpty() {
        return width <= 0f || height <= 0f;
    }

    public boolean contains(float worldX, float worldY) {
        return worldX >= x && worldX <= x + width && worldY >= y && worldY <= y + height;
    }

    public boolean overlaps(Rectangle rectangle) {
        return rectangle != null && rectangle.overlaps(toRectangle());
    }

    public Rectangle toRectangle() {
        return new Rectangle(x, y, width, height);
    }

    public boolean clampTo(float maxWidth, float maxHeight) {
        float clampedX = MathUtils.clamp(x, 0f, Math.max(0f, maxWidth - Math.max(width, 0f)));
        float clampedY = MathUtils.clamp(y, 0f, Math.max(0f, maxHeight - Math.max(height, 0f)));
        float clampedWidth = MathUtils.clamp(width, 0f, maxWidth);
        float clampedHeight = MathUtils.clamp(height, 0f, maxHeight);
        boolean changed = Float.compare(x, clampedX) != 0
            || Float.compare(y, clampedY) != 0
            || Float.compare(width, clampedWidth) != 0
            || Float.compare(height, clampedHeight) != 0;
        x = clampedX;
        y = clampedY;
        width = clampedWidth;
        height = clampedHeight;
        return changed;
    }
}
