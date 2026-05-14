package com.perplexinggames.ironsoul.level;

import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.utils.Array;

public class LevelCollision {
    private final RuntimeLevel runtimeLevel;

    public LevelCollision(RuntimeLevel runtimeLevel) {
        this.runtimeLevel = runtimeLevel;
    }

    public boolean collides(Rectangle bounds) {
        int tileSize = runtimeLevel.getTileSize();
        int left = MathUtils.floor(bounds.x / tileSize);
        int right = MathUtils.floor((bounds.x + bounds.width - 0.001f) / tileSize);
        int bottom = MathUtils.floor(bounds.y / tileSize);
        int top = MathUtils.floor((bounds.y + bounds.height - 0.001f) / tileSize);

        for (int y = bottom; y <= top; y++) {
            for (int x = left; x <= right; x++) {
                if (runtimeLevel.hasBlock(x, y)) {
                    return true;
                }
            }
        }
        return false;
    }

    public Array<Rectangle> getSolidRectangles() {
        Array<Rectangle> rectangles = new Array<>();
        int tileSize = runtimeLevel.getTileSize();
        for (BlockData block : runtimeLevel.getBlocks()) {
            rectangles.add(new Rectangle(block.x * tileSize, block.y * tileSize, tileSize, tileSize));
        }
        return rectangles;
    }
}
