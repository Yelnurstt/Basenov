package com.perplexinggames.ironsoul.terrain.spline;

import com.badlogic.gdx.math.Vector2;

public class SplineSample {
    private final float x;
    private final float y;
    private final float tangentX;
    private final float tangentY;
    private final float normalX;
    private final float normalY;
    private final float distanceAlongPath;

    public SplineSample(Vector2 position, Vector2 tangent, float distanceFromStart) {
        this(position, tangent, tangent == null ? null : new Vector2(-tangent.y, tangent.x), distanceFromStart);
    }

    public SplineSample(Vector2 position, Vector2 tangent, Vector2 normal, float distanceAlongPath) {
        Vector2 safePosition = position == null ? new Vector2() : new Vector2(position);
        Vector2 safeTangent = tangent == null || tangent.len2() == 0f ? new Vector2(1f, 0f) : new Vector2(tangent).nor();
        Vector2 safeNormal = normal == null || normal.len2() == 0f
            ? new Vector2(-safeTangent.y, safeTangent.x)
            : new Vector2(normal).nor();
        this.x = safePosition.x;
        this.y = safePosition.y;
        this.tangentX = safeTangent.x;
        this.tangentY = safeTangent.y;
        this.normalX = safeNormal.x;
        this.normalY = safeNormal.y;
        this.distanceAlongPath = distanceAlongPath;
    }

    public Vector2 getPosition() {
        return new Vector2(x, y);
    }

    public Vector2 getTangent() {
        return new Vector2(tangentX, tangentY);
    }

    public Vector2 getNormal() {
        return new Vector2(normalX, normalY);
    }

    public float getDistanceFromStart() {
        return distanceAlongPath;
    }

    public float getDistanceAlongPath() {
        return distanceAlongPath;
    }

    public float getX() {
        return x;
    }

    public float getY() {
        return y;
    }

    public float getTangentX() {
        return tangentX;
    }

    public float getTangentY() {
        return tangentY;
    }

    public float getNormalX() {
        return normalX;
    }

    public float getNormalY() {
        return normalY;
    }
}
