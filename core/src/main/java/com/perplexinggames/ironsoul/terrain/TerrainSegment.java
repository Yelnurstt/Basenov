package com.perplexinggames.ironsoul.terrain;

import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;

public class TerrainSegment {
    private static final float EPSILON = 0.0001f;

    // Сделали приватными для строгой инкапсуляции
    private final TerrainPoint p1;
    private final TerrainPoint p2;
    private final Vector2 direction;
    private final Vector2 normal;
    private final float angle;

    public TerrainSegment(TerrainPoint startPoint, TerrainPoint endPoint) {
        this.p1 = startPoint;
        this.p2 = endPoint;
        Vector2 tangent = new Vector2(p2.x - p1.x, p2.y - p1.y);

        if (tangent.isZero(EPSILON)) {
            tangent.set(1f, 0f);
        } else {
            tangent.nor();
        }

        this.direction = new Vector2(tangent);
        Vector2 resolvedNormal = new Vector2(-tangent.y, tangent.x);

        if (resolvedNormal.y < 0f) {
            resolvedNormal.scl(-1f);
        }

        this.normal = resolvedNormal.nor();
        this.angle = MathUtils.atan2(tangent.y, tangent.x) * MathUtils.radiansToDegrees;
    }

    // ==========================================
    // ГЕТТЕРЫ ДЛЯ ФИЗИКИ КОЛЛИЗИЙ (добавлены)
    // ==========================================
    public TerrainPoint getP1() {
        return p1;
    }

    public TerrainPoint getP2() {
        return p2;
    }

    // ==========================================
    // СТАРЫЕ ГЕТТЕРЫ (оставлены для совместимости с RuntimeLevel)
    // ==========================================
    public TerrainPoint getStartPoint() {
        return p1;
    }

    public TerrainPoint getEndPoint() {
        return p2;
    }

    public Vector2 getDirection() {
        return new Vector2(direction);
    }

    public Vector2 getTangent() {
        return new Vector2(direction);
    }

    public Vector2 getNormal() {
        return new Vector2(normal);
    }

    public float getAngle() {
        return angle;
    }

    public float getLength() {
        return new Vector2(p2.x - p1.x, p2.y - p1.y).len();
    }

    public boolean containsX(float x) {
        float minX = Math.min(p1.x, p2.x) - EPSILON;
        float maxX = Math.max(p1.x, p2.x) + EPSILON;
        return x >= minX && x <= maxX;
    }

    public float getY(float x) {
        return getYAtX(x);
    }

    public float getYAtX(float x) {
        float deltaX = p2.x - p1.x;
        if (Math.abs(deltaX) <= EPSILON) {
            return Math.max(p1.y, p2.y);
        }
        float alpha = (x - p1.x) / deltaX;
        return MathUtils.lerp(p1.y, p2.y, alpha);
    }
}
