package com.perplexinggames.ironsoul.terrain;

import com.badlogic.gdx.math.Vector2;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class SegmentTerrainCollisionProvider implements TerrainCollisionProvider {
    private final List<TerrainCollisionData> collisionData;

    public SegmentTerrainCollisionProvider(Collection<TerrainCollisionData> collisionData) {
        this.collisionData = new ArrayList<>(collisionData);
    }

    @Override
    public TerrainContactInfo findGroundBelow(Vector2 position, float probeDistance) {
        TerrainSegment bestSegment = null;
        TerrainCollisionData bestData = null;
        float bestSurfaceY = Float.NEGATIVE_INFINITY;

        for (TerrainCollisionData data : collisionData) {
            for (TerrainSegment segment : data.getSegments()) {
                if (!segment.containsX(position.x)) {
                    continue;
                }

                float surfaceY = segment.getYAtX(position.x);
                float distanceToSurface = position.y - surfaceY;
                if (distanceToSurface < 0f || distanceToSurface > probeDistance) {
                    continue;
                }

                if (surfaceY > bestSurfaceY) {
                    bestSurfaceY = surfaceY;
                    bestSegment = segment;
                    bestData = data;
                }
            }
        }

        if (bestSegment == null || bestData == null) {
            return TerrainContactInfo.noGround(position);
        }

        return new TerrainContactInfo(
            new Vector2(position.x, bestSurfaceY),
            bestSegment.getNormal(),
            bestSegment.getTangent(),
            bestSegment.getAngle(),
            bestSegment,
            true,
            bestData.getMaterial(),
            bestData.getFriction()
        );
    }
}
