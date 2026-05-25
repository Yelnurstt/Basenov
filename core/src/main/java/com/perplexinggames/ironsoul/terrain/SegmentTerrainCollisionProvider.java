package com.perplexinggames.ironsoul.terrain;

import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

public class SegmentTerrainCollisionProvider implements TerrainCollisionProvider {
    private final List<TerrainCollisionData> collisionData;

    public SegmentTerrainCollisionProvider(TerrainPath terrainPath) {
        this(Collections.singletonList(new TerrainCollisionBuilder().build(terrainPath)));
    }

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
                if (Math.abs(segment.getP1().x - segment.getP2().x) < 0.1f) {
                    continue;
                }

                if (!segment.containsX(position.x)) {
                    continue;
                }

                float surfaceY = segment.getYAtX(position.x);
                float distanceToSurface = position.y - surfaceY;

                if (distanceToSurface < -35f || distanceToSurface > probeDistance + 15f) {
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

    @Override
    public boolean hasBlockingWall(float startX, float endX, float currentY, float maxStepHeight) {
        float minX = Math.min(startX, endX);
        float maxX = Math.max(startX, endX);

        for (TerrainCollisionData data : collisionData) {
            for (TerrainSegment segment : data.getSegments()) {
                float p1x = segment.getP1().x;
                float p2x = segment.getP2().x;
                float p1y = segment.getP1().y;
                float p2y = segment.getP2().y;

                float segMinX = Math.min(p1x, p2x);
                float segMaxX = Math.max(p1x, p2x);

                if (maxX >= segMinX && minX <= segMaxX) {
                    float dx = p2x - p1x;
                    float dy = p2y - p1y;

                    float angle = Math.abs(MathUtils.atan2(dy, dx) * MathUtils.radiansToDegrees);
                    if (angle > 90f) angle = 180f - angle;

                    if (angle > 55f || Math.abs(dx) < 0.1f) {
                        float minY = Math.min(p1y, p2y);
                        float maxY = Math.max(p1y, p2y);

                        if (maxY > currentY + maxStepHeight && currentY >= minY - 20f) {
                            return true;
                        }
                    }
                }
            }
        }
        return false;
    }
}
