package com.perplexinggames.ironsoul.level.serialization;

import java.util.ArrayList;

import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.Json;
import com.perplexinggames.ironsoul.level.LevelData;

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

        LevelData levelData = json.fromJson(LevelData.class, source.readString("UTF-8"));
        if (levelData.blocks == null) {
            levelData.blocks = new ArrayList<>();
        }
        return levelData;
    }
}
