package com.perplexinggames.ironsoul.terrain;

import com.badlogic.gdx.math.Vector2;

public interface TerrainCollisionProvider {
    TerrainContactInfo findGroundBelow(Vector2 position, float probeDistance);
}
