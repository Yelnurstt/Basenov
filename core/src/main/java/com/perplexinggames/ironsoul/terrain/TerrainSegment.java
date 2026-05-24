package com.perplexinggames.ironsoul.terrain;

import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;

public class TerrainSegment {
    private static final float EPSILON = 0.0001f;

    private final TerrainPoint startPoint;
    private final TerrainPoint endPoint;

    public TerrainSegment(TerrainPoint startPoint, TerrainPoint endPoint) {
        this.startPoint = startPoint;
        this.endPoint = endPoint;
    }

    public TerrainPoint getStartPoint() {
        return startPoint;
    }

    public TerrainPoint getEndPoint() {
        return endPoint;
    }

    public Vector2 getDirection() {
        return new Vector2(endPoint.getX() - startPoint.getX(), endPoint.getY() - startPoint.getY());
    }

    public Vector2 getTangent() {
        Vector2 direction = getDirection();
        if (direction.isZero(EPSILON)) {
            return new Vector2(1f, 0f);
        }
        return direction.nor();
    }

    public Vector2 getNormal() {
        Vector2 tangent = getTangent();
        Vector2 normal = new Vector2(-tangent.y, tangent.x);
        if (normal.y < 0f) {
            normal.scl(-1f);
        }
        return normal.nor();
    }

    public float getAngle() {
        Vector2 tangent = getTangent();
        return MathUtils.atan2(tangent.y, tangent.x) * MathUtils.radiansToDegrees;
    }

    public float getLength() {
        return getDirection().len();
    }

    public boolean containsX(float x) {
        float minX = Math.min(startPoint.getX(), endPoint.getX()) - EPSILON;
        float maxX = Math.max(startPoint.getX(), endPoint.getX()) + EPSILON;
        return x >= minX && x <= maxX;
    }

    public float getYAtX(float x) {
        float deltaX = endPoint.getX() - startPoint.getX();
        if (Math.abs(deltaX) <= EPSILON) {
            return Math.max(startPoint.getY(), endPoint.getY());
        }

        float alpha = (x - startPoint.getX()) / deltaX;
        return MathUtils.lerp(startPoint.getY(), endPoint.getY(), alpha);
    }
}
