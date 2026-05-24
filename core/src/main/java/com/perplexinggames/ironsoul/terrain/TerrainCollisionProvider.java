package com.perplexinggames.ironsoul.terrain;

import com.badlogic.gdx.math.Vector2;

public interface TerrainCollisionProvider {
    TerrainContactInfo findGroundBelow(Vector2 position, float probeDistance);

    // НОВЫЙ МЕТОД: Радар для стен
    boolean hasBlockingWall(float startX, float endX, float currentY, float maxStepHeight);

    default void getContactInfo(float x, float y, float probeLength, TerrainContactInfo outInfo) {
        if (outInfo == null) {
            return;
        }

        TerrainContactInfo contactInfo = findGroundBelow(new Vector2(x, y), probeLength);
        if (!contactInfo.isGrounded()) {
            outInfo.reset();
            return;
        }

        outInfo.setContact(
            contactInfo.getContactPoint(),
            contactInfo.getSurfaceNormal(),
            contactInfo.getSurfaceTangent(),
            contactInfo.getSurfaceAngle(),
            contactInfo.getSegment(),
            contactInfo.getMaterial(),
            contactInfo.getFriction()
        );
    }
}
