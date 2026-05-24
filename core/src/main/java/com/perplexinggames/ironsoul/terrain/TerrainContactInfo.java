package com.perplexinggames.ironsoul.terrain;

import com.badlogic.gdx.math.Vector2;

public class TerrainContactInfo {
    public boolean hasContact;
    public final Vector2 point = new Vector2();
    public final Vector2 normal = new Vector2(0f, 1f);
    public float angle;

    private final Vector2 surfaceTangent = new Vector2(1f, 0f);
    private TerrainSegment segment;
    private String material = "default";
    private float friction = 1f;

    public TerrainContactInfo() {
        reset();
    }

    public TerrainContactInfo(
        Vector2 contactPoint,
        Vector2 surfaceNormal,
        Vector2 surfaceTangent,
        float surfaceAngle,
        TerrainSegment segment,
        boolean grounded,
        String material,
        float friction
    ) {
        if (grounded) {
            setContact(contactPoint, surfaceNormal, surfaceTangent, surfaceAngle, segment, material, friction);
        } else {
            reset();
            point.set(contactPoint);
        }
    }

    public static TerrainContactInfo noGround(Vector2 probePosition) {
        TerrainContactInfo info = new TerrainContactInfo();
        info.point.set(probePosition);
        return info;
    }

    public void reset() {
        hasContact = false;
        point.setZero();
        normal.set(0f, 1f);
        surfaceTangent.set(1f, 0f);
        angle = 0f;
        segment = null;
        material = "default";
        friction = 1f;
    }

    public void setContact(
        Vector2 contactPoint,
        Vector2 surfaceNormal,
        Vector2 tangent,
        float surfaceAngle,
        TerrainSegment terrainSegment,
        String surfaceMaterial,
        float surfaceFriction
    ) {
        hasContact = true;
        point.set(contactPoint);
        normal.set(surfaceNormal);
        surfaceTangent.set(tangent);
        angle = surfaceAngle;
        segment = terrainSegment;
        material = surfaceMaterial == null ? "default" : surfaceMaterial;
        friction = surfaceFriction;
    }

    public Vector2 getContactPoint() {
        return new Vector2(point);
    }

    public Vector2 getSurfaceNormal() {
        return new Vector2(normal);
    }

    public Vector2 getSurfaceTangent() {
        return new Vector2(surfaceTangent);
    }

    public float getSurfaceAngle() {
        return angle;
    }

    public TerrainSegment getSegment() {
        return segment;
    }

    public boolean isGrounded() {
        return hasContact;
    }

    public String getMaterial() {
        return material;
    }

    public float getFriction() {
        return friction;
    }
}
