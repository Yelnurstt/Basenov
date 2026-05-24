package com.perplexinggames.ironsoul.terrain;

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
                // ФИКС: Используем getP1() и getP2() вместо прямых переменных!
                // Игнорируем строго вертикальные стены при поиске пола
                if (Math.abs(segment.getP1().x - segment.getP2().x) < 0.1f) {
                    continue;
                }

                if (!segment.containsX(position.x)) {
                    continue;
                }

                float surfaceY = segment.getYAtX(position.x);
                float distanceToSurface = position.y - surfaceY;

                // ДОПУСК: -80f чтобы лучи находили гору, даже если танк врезался в неё
                if (distanceToSurface < -80f || distanceToSurface > probeDistance + 15f) {
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

    // ЛОГИКА РАДАРА СТЕН ДЛЯ СЕГМЕНТОВ
    @Override
    public boolean hasBlockingWall(float startX, float endX, float currentY, float maxStepHeight) {
        float minX = Math.min(startX, endX);
        float maxX = Math.max(startX, endX);

        for (TerrainCollisionData data : collisionData) {
            for (TerrainSegment segment : data.getSegments()) {
                if (Math.abs(segment.getP1().x - segment.getP2().x) < 0.1f) {
                    float wallX = segment.getP1().x;

                    // Простая и надежная проверка, которая работает в обе стороны!
                    if (wallX >= minX && wallX <= maxX) {
                        float minY = Math.min(segment.getP1().y, segment.getP2().y);
                        float maxY = Math.max(segment.getP1().y, segment.getP2().y);

                        if (maxY > currentY + maxStepHeight && currentY >= minY - 5f) {
                            return true;
                        }
                    }
                }
            }
        }
        return false;
    }
}
