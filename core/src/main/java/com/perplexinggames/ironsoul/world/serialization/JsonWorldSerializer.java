package com.perplexinggames.ironsoul.world.serialization;

import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.SerializationException;
import com.perplexinggames.ironsoul.level.LevelData;
import com.perplexinggames.ironsoul.level.serialization.JsonLevelSerializer;
import com.perplexinggames.ironsoul.world.SpawnPointData;
import com.perplexinggames.ironsoul.world.WorldBlockData;
import com.perplexinggames.ironsoul.world.WorldData;
import com.perplexinggames.ironsoul.world.runtime.WorldSpawnResolver;

import java.util.ArrayList;

public class JsonWorldSerializer implements WorldSerializer {
    private final Json json;

    public JsonWorldSerializer() {
        json = new Json();
        json.setUsePrototypes(false);
    }

    @Override
    public void save(WorldData worldData, FileHandle targetFile) {
        if (targetFile.parent() != null) {
            targetFile.parent().mkdirs();
        }
        targetFile.writeString(json.prettyPrint(worldData), false, "UTF-8");
    }

    @Override
    public WorldData load(FileHandle localFile, FileHandle internalFile) {
        FileHandle source = null;
        if (localFile != null && localFile.exists()) {
            source = localFile;
        } else if (internalFile != null && internalFile.exists()) {
            source = internalFile;
        }

        if (source == null) {
            return null;
        }

        String raw = source.readString("UTF-8");
        if (raw.contains("worldBlocks")) {
            return normalize(parseWorld(raw));
        }
        return normalize(convertLegacyLevel(source, raw));
    }

    private WorldData parseWorld(String raw) {
        try {
            return json.fromJson(WorldData.class, raw);
        } catch (SerializationException exception) {
            String sanitized = sanitizeLegacyCollectionWrappers(raw);
            if (sanitized.equals(raw)) {
                throw exception;
            }
            return json.fromJson(WorldData.class, sanitized);
        }
    }

    private WorldData convertLegacyLevel(FileHandle source, String raw) {
        JsonLevelSerializer legacySerializer = new JsonLevelSerializer();
        LevelData levelData;
        try {
            levelData = legacySerializer.load(source, null);
        } catch (SerializationException exception) {
            String sanitized = sanitizeLegacyCollectionWrappers(raw);
            levelData = new Json().fromJson(LevelData.class, sanitized);
        }

        if (levelData == null) {
            return null;
        }

        WorldData worldData = new WorldData(levelData.id, levelData.name, levelData.tileSize);
        String blockId = levelData.id == null || levelData.id.isBlank() ? "start-area" : levelData.id;
        WorldBlockData block = new WorldBlockData(blockId, levelData.name == null ? "Start Area" : levelData.name,
            levelData.width, levelData.height);
        block.tiles.addAll(levelData.blocks == null ? new ArrayList<>() : levelData.blocks);
        block.terrain.addAll(levelData.terrainPaths == null ? new ArrayList<>() : levelData.terrainPaths);
        block.spawnPoints.add(WorldSpawnResolver.createDefaultSpawnPoint("START", blockId, block, worldData.tileSize, "START"));
        worldData.worldBlocks.add(block);
        worldData.activeBlockId = blockId;
        return worldData;
    }

    private WorldData normalize(WorldData worldData) {
        if (worldData == null) {
            return null;
        }
        if (worldData.worldBlocks == null) {
            worldData.worldBlocks = new ArrayList<>();
        }
        for (WorldBlockData block : worldData.worldBlocks) {
            if (block == null) {
                continue;
            }
            if (block.tiles == null) {
                block.tiles = new ArrayList<>();
            }
            if (block.terrain == null) {
                block.terrain = new ArrayList<>();
            }
            if (block.objects == null) {
                block.objects = new ArrayList<>();
            }
            if (block.enemies == null) {
                block.enemies = new ArrayList<>();
            }
            if (block.rewards == null) {
                block.rewards = new ArrayList<>();
            }
            if (block.triggers == null) {
                block.triggers = new ArrayList<>();
            }
            if (block.gates == null) {
                block.gates = new ArrayList<>();
            }
            if (block.spawnPoints == null) {
                block.spawnPoints = new ArrayList<>();
            }
            if (block.spawnPoints.isEmpty()) {
                block.spawnPoints.add(WorldSpawnResolver.createDefaultSpawnPoint("START_" + block.id.toUpperCase(),
                    block.id, block, worldData.tileSize, block.name + " start"));
            }
            if (block.metadata == null) {
                block.metadata = new java.util.LinkedHashMap<>();
            }
        }
        if (worldData.metadata == null) {
            worldData.metadata = new java.util.LinkedHashMap<>();
        }
        if (worldData.activeBlockId == null && !worldData.worldBlocks.isEmpty()) {
            worldData.activeBlockId = worldData.worldBlocks.get(0).id;
        }
        return worldData;
    }

    private String sanitizeLegacyCollectionWrappers(String raw) {
        String sanitized = unwrapListField(raw, "points");
        sanitized = unwrapListField(sanitized, "terrain");
        sanitized = unwrapListField(sanitized, "tiles");
        sanitized = unwrapListField(sanitized, "objects");
        sanitized = unwrapListField(sanitized, "enemies");
        sanitized = unwrapListField(sanitized, "rewards");
        sanitized = unwrapListField(sanitized, "triggers");
        sanitized = unwrapListField(sanitized, "gates");
        sanitized = unwrapListField(sanitized, "spawnPoints");
        sanitized = unwrapListField(sanitized, "worldBlocks");
        sanitized = unwrapListField(sanitized, "terrainPaths");
        sanitized = unwrapListField(sanitized, "blocks");
        return sanitized;
    }

    private String unwrapListField(String raw, String fieldName) {
        return raw.replaceAll(
            fieldName + ":\\s*\\{\\s*class:\\s*[^\\r\\n]+\\s*items:\\s*(\\[[\\s\\S]*?\\])\\s*\\}",
            fieldName + ": $1"
        );
    }
}
