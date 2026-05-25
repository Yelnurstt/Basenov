package com.perplexinggames.ironsoul.terrain.spline;

import com.badlogic.gdx.math.Vector2;

public class SplineSample {
    private final Vector2 position;
    private final Vector2 tangent;
    private final float distanceFromStart;

    public SplineSample(Vector2 position, Vector2 tangent, float distanceFromStart) {
        this.position = new Vector2(position);
        this.tangent = tangent == null ? new Vector2(1f, 0f) : new Vector2(tangent).nor();
        this.distanceFromStart = distanceFromStart;
    }

    public Vector2 getPosition() {
        return new Vector2(position);
    }

    public Vector2 getTangent() {
        return new Vector2(tangent);
    }

    public float getDistanceFromStart() {
        return distanceFromStart;
    }
}
