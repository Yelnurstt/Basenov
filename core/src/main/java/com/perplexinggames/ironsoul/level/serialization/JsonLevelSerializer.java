package com.perplexinggames.ironsoul.level.serialization;

import java.util.ArrayList;

import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.SerializationException;
import com.perplexinggames.ironsoul.level.LevelData;
import com.perplexinggames.ironsoul.terrain.TerrainPath;

public class JsonLevelSerializer implements LevelSerializer {
    private final Json json;

    public JsonLevelSerializer() {
        this.json = new Json();
        this.json.setUsePrototypes(false);
    }

    @Override
    public void save(LevelData levelData, FileHandle targetFile) {
        if (targetFile.parent() != null) {
            targetFile.parent().mkdirs();
        }
        targetFile.writeString(json.prettyPrint(levelData), false, "UTF-8");
    }

    @Override
    public LevelData load(FileHandle localFile, FileHandle internalFile) {
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
        LevelData levelData;
        try {
            levelData = json.fromJson(LevelData.class, raw);
        } catch (SerializationException exception) {
            String sanitized = sanitizeLegacyCollectionWrappers(raw);
            if (sanitized.equals(raw)) {
                throw exception;
            }
            levelData = json.fromJson(LevelData.class, sanitized);
        }
        if (levelData.blocks == null) {
            levelData.blocks = new ArrayList<>();
        }
        if (levelData.terrainPaths == null) {
            levelData.terrainPaths = new ArrayList<>();
        }
        for (int i = levelData.terrainPaths.size() - 1; i >= 0; i--) {
            TerrainPath terrainPath = levelData.terrainPaths.get(i);
            if (terrainPath == null || terrainPath.getId() == null || terrainPath.getId().isEmpty()) {
                levelData.terrainPaths.remove(i);
            }
        }
        return levelData;
    }

    private String sanitizeLegacyCollectionWrappers(String raw) {
        String sanitized = unwrapListField(raw, "points");
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
