package com.perplexinggames.ironsoul.world;

import com.perplexinggames.ironsoul.level.BlockData;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public final class WorldValidator {
    private WorldValidator() {
    }

    public static WorldValidationResult validate(WorldData worldData) {
        WorldValidationResult result = new WorldValidationResult();
        if (worldData == null) {
            result.addError("World data is missing.");
            return result;
        }

        Set<String> blockIds = new HashSet<>();
        for (WorldBlockData block : worldData.worldBlocks) {
            if (block == null) {
                result.addError("World contains a null block entry.");
                continue;
            }

            if (block.id == null || block.id.isBlank()) {
                result.addError("Block id must not be empty.");
            } else if (!blockIds.add(block.id)) {
                result.addError("Block id must be unique: " + block.id);
            }

            if (block.width <= 0 || block.height <= 0) {
                result.addError("Block " + block.id + " must have width/height > 0.");
            }

            validateTiles(result, block, worldData.tileSize);
            validateSpawnPoints(result, block, worldData.tileSize);
            validateElements(result, block.id, "object", block.objects, block, worldData.tileSize);
            validateElements(result, block.id, "enemy", block.enemies, block, worldData.tileSize);
            validateElements(result, block.id, "reward", block.rewards, block, worldData.tileSize);
            validateElements(result, block.id, "trigger", block.triggers, block, worldData.tileSize);
        }

        for (WorldBlockData block : worldData.worldBlocks) {
            if (block == null) {
                continue;
            }
            for (GateData gate : block.gates) {
                validateGate(result, worldData, block, gate);
            }
        }

        if (worldData.activeBlockId != null && worldData.findBlock(worldData.activeBlockId) == null) {
            result.addError("Active block does not exist: " + worldData.activeBlockId);
        }

        return result;
    }

    private static void validateTiles(WorldValidationResult result, WorldBlockData block, int tileSize) {
        int ignored = tileSize;
        for (BlockData tile : block.tiles) {
            if (tile == null) {
                continue;
            }
            if (tile.x < 0 || tile.x >= block.width || tile.y < 0 || tile.y >= block.height) {
                result.addError("Tile out of bounds in block " + block.id + " at (" + tile.x + ", " + tile.y + ").");
            }
        }
    }

    private static void validateSpawnPoints(WorldValidationResult result, WorldBlockData block, int tileSize) {
        Set<String> spawnIds = new HashSet<>();
        for (SpawnPointData spawnPoint : block.spawnPoints) {
            if (spawnPoint == null) {
                continue;
            }
            if (spawnPoint.id == null || spawnPoint.id.isBlank()) {
                result.addError("Spawn point id must not be empty in block " + block.id + ".");
            } else if (!spawnIds.add(spawnPoint.id)) {
                result.addError("Spawn point id must be unique inside block " + block.id + ": " + spawnPoint.id);
            }
            if (!isInsideBlock(spawnPoint.x, spawnPoint.y, block, tileSize)) {
                result.addError("Spawn point " + spawnPoint.id + " is outside block " + block.id + ".");
            }
        }
    }

    private static void validateElements(WorldValidationResult result, String blockId, String label,
                                         List<WorldElementData> elements, WorldBlockData block, int tileSize) {
        for (WorldElementData element : elements) {
            if (element == null) {
                continue;
            }
            if (!isInsideBlock(element.x, element.y, block, tileSize)) {
                result.addError(label + " " + element.id + " is outside block " + blockId + ".");
            }
        }
    }

    private static void validateGate(WorldValidationResult result, WorldData worldData, WorldBlockData block, GateData gate) {
        if (gate == null) {
            return;
        }
        if (gate.sourceBlockId == null || worldData.findBlock(gate.sourceBlockId) == null) {
            result.addError("Gate " + gate.id + " references missing source block " + gate.sourceBlockId + ".");
        }
        if (gate.targetBlockId == null || worldData.findBlock(gate.targetBlockId) == null) {
            result.addError("Gate " + gate.id + " references missing target block " + gate.targetBlockId + ".");
        }
        if (gate.bounds == null || gate.bounds.isEmpty()) {
            result.addError("Gate " + gate.id + " must have non-empty bounds.");
        }
        WorldBlockData targetBlock = worldData.findBlock(gate.targetBlockId);
        if (targetBlock != null && gate.targetSpawnPointId != null && !hasSpawnPoint(targetBlock, gate.targetSpawnPointId)) {
            result.addError("Gate " + gate.id + " references missing target spawn " + gate.targetSpawnPointId + ".");
        }
        if (gate.bounds != null) {
            float maxWidth = block.getPixelWidth(worldData.tileSize);
            float maxHeight = block.getPixelHeight(worldData.tileSize);
            boolean inside = gate.bounds.x >= 0f
                && gate.bounds.y >= 0f
                && gate.bounds.x + gate.bounds.width <= maxWidth
                && gate.bounds.y + gate.bounds.height <= maxHeight;
            if (!inside) {
                result.addError("Gate " + gate.id + " bounds are outside block " + block.id + ".");
            }
        }
    }

    private static boolean hasSpawnPoint(WorldBlockData block, String spawnPointId) {
        for (SpawnPointData spawnPoint : block.spawnPoints) {
            if (spawnPoint != null && spawnPointId.equals(spawnPoint.id)) {
                return true;
            }
        }
        return false;
    }

    private static boolean isInsideBlock(float x, float y, WorldBlockData block, int tileSize) {
        return x >= 0f && x <= block.getPixelWidth(tileSize) && y >= 0f && y <= block.getPixelHeight(tileSize);
    }
}
