package com.perplexinggames.ironsoul.terrain;

import com.badlogic.gdx.math.Vector2;
import com.perplexinggames.ironsoul.level.RuntimeLevel;

public class RuntimeTerrainCollisionProvider implements TerrainCollisionProvider {
    private final RuntimeLevel runtimeLevel;
    private final TerrainCollisionBuilder collisionBuilder;

    public RuntimeTerrainCollisionProvider(RuntimeLevel runtimeLevel) {
        this.runtimeLevel = runtimeLevel;
        this.collisionBuilder = new TerrainCollisionBuilder();
    }

    @Override
    public TerrainContactInfo findGroundBelow(Vector2 position, float probeDistance) {
        TerrainContactInfo bestContact = TerrainContactInfo.noGround(position);
        float bestSurfaceY = Float.NEGATIVE_INFINITY;

        for (TerrainPath terrainPath : runtimeLevel.getTerrainPaths()) {
            TerrainCollisionData collisionData = collisionBuilder.build(terrainPath);
            TerrainContactInfo contact = new SegmentTerrainCollisionProvider(java.util.Collections.singletonList(collisionData))
                .findGroundBelow(position, probeDistance);
            if (contact.isGrounded() && contact.getContactPoint().y > bestSurfaceY) {
                bestSurfaceY = contact.getContactPoint().y;
                bestContact = contact;
            }
        }

        return bestContact;
    }

    // РЕАЛИЗАЦИЯ РАДАРА СТЕН ДЛЯ УРОВНЯ
    @Override
    public boolean hasBlockingWall(float startX, float endX, float currentY, float maxStepHeight) {
        for (TerrainPath terrainPath : runtimeLevel.getTerrainPaths()) {
            TerrainCollisionData collisionData = collisionBuilder.build(terrainPath);
            SegmentTerrainCollisionProvider provider = new SegmentTerrainCollisionProvider(java.util.Collections.singletonList(collisionData));

            if (provider.hasBlockingWall(startX, endX, currentY, maxStepHeight)) {
                return true;
            }
        }
        return false;
    }

}
