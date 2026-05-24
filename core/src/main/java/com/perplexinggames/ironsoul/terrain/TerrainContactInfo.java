package com.perplexinggames.ironsoul.terrain;

import com.badlogic.gdx.math.Vector2;

public class TerrainContactInfo {
    private final Vector2 contactPoint;
    private final Vector2 surfaceNormal;
    private final Vector2 surfaceTangent;
    private final float surfaceAngle;
    private final TerrainSegment segment;
    private final boolean grounded;
    private final String material;
    private final float friction;

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
        this.contactPoint = new Vector2(contactPoint);
        this.surfaceNormal = new Vector2(surfaceNormal);
        this.surfaceTangent = new Vector2(surfaceTangent);
        this.surfaceAngle = surfaceAngle;
        this.segment = segment;
        this.grounded = grounded;
        this.material = material;
        this.friction = friction;
    }

    public static TerrainContactInfo noGround(Vector2 probePosition) {
        return new TerrainContactInfo(
            probePosition,
            new Vector2(0f, 1f),
            new Vector2(1f, 0f),
            0f,
            null,
            false,
            "default",
            1f
        );
    }

    public Vector2 getContactPoint() {
        return new Vector2(contactPoint);
    }

    public Vector2 getSurfaceNormal() {
        return new Vector2(surfaceNormal);
    }

    public Vector2 getSurfaceTangent() {
        return new Vector2(surfaceTangent);
    }

    public float getSurfaceAngle() {
        return surfaceAngle;
    }

    public TerrainSegment getSegment() {
        return segment;
    }

    public boolean isGrounded() {
        return grounded;
    }

    public String getMaterial() {
        return material;
    }

    public float getFriction() {
        return friction;
    }
}
