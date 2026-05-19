package com.perplexinggames.ironsoul.level;

import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.utils.Array;

/**
 * Minimal gameplay collision boundary over the current block-based level model.
 * The richer {@code tiles} package remains separate for now and can be unified later.
 */
public interface LevelCollisionProvider {
    boolean collides(Rectangle bounds);

    Array<Rectangle> getNearbySolidBlocks(Rectangle bounds);

    Array<Rectangle> getSolidRectangles();
}
