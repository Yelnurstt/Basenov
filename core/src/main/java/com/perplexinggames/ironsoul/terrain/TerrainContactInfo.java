package com.perplexinggames.ironsoul.terrain;

import com.badlogic.gdx.math.Vector2;

public class TerrainContactInfo {
    public boolean hasContact = false;
    public final Vector2 point = new Vector2();
    public final Vector2 normal = new Vector2();
    public float angle = 0f;

    public void reset() {
        hasContact = false;
        point.setZero();
        normal.setZero();
        angle = 0f;
    }
}
