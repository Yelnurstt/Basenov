package com.perplexinggames.ironsoul.world.runtime;

import com.badlogic.gdx.math.MathUtils;
import com.perplexinggames.ironsoul.tank.controller.FacingDirection;
import com.perplexinggames.ironsoul.terrain.TerrainPath;
import com.perplexinggames.ironsoul.terrain.TerrainPoint;
import com.perplexinggames.ironsoul.world.SpawnPointData;
import com.perplexinggames.ironsoul.world.WorldBlockData;

public final class WorldSpawnResolver {
    private WorldSpawnResolver() {
    }

    public static SpawnPointData createDefaultSpawnPoint(String id, String blockId, WorldBlockData block, int tileSize, String label) {
        float preferredX = tileSize * 2f;
        float preferredY = tileSize * 4f;
        return new SpawnPointData(
            id,
            blockId,
            clampX(block, preferredX, tileSize),
            resolveSafeSpawnY(block, preferredX, preferredY, tileSize),
            FacingDirection.RIGHT,
            label
        );
    }

    public static SpawnPointData resolveSafeSpawnPoint(WorldBlockData block, SpawnPointData spawnPoint, int tileSize) {
        if (block == null || spawnPoint == null) {
            return spawnPoint;
        }
        return new SpawnPointData(
            spawnPoint.id,
            spawnPoint.blockId,
            clampX(block, spawnPoint.x, tileSize),
            resolveSafeSpawnY(block, spawnPoint.x, spawnPoint.y, tileSize),
            spawnPoint.facingDirection,
            spawnPoint.debugLabel
        );
    }

    public static float resolveSafeSpawnY(WorldBlockData block, float preferredX, float preferredY, int tileSize) {
        if (block == null) {
            return preferredY;
        }

        float localHighestY = Float.NEGATIVE_INFINITY;
        float globalHighestY = 0f;
        float probeRadius = tileSize * 4f;

        for (TerrainPath terrainPath : block.terrain) {
            for (TerrainPoint point : terrainPath.getPoints()) {
                globalHighestY = Math.max(globalHighestY, point.getY());
                if (Math.abs(point.getX() - preferredX) <= probeRadius) {
                    localHighestY = Math.max(localHighestY, point.getY());
                }
            }
        }

        float terrainReferenceY = localHighestY == Float.NEGATIVE_INFINITY ? globalHighestY : localHighestY;
        float spawnMargin = tileSize * 4f;
        float minSpawnY = tileSize * 3f;
        float maxSpawnY = Math.max(minSpawnY, block.getPixelHeight(tileSize) - tileSize * 2f);
        return MathUtils.clamp(Math.max(preferredY, terrainReferenceY + spawnMargin), minSpawnY, maxSpawnY);
    }

    private static float clampX(WorldBlockData block, float preferredX, int tileSize) {
        return MathUtils.clamp(preferredX, tileSize, Math.max(tileSize, block.getPixelWidth(tileSize) - tileSize));
    }
}
