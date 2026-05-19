package com.perplexinggames.ironsoul.level.serialization;

import com.badlogic.gdx.files.FileHandle;
import com.perplexinggames.ironsoul.level.LevelData;

public interface LevelSerializer {
    void save(LevelData levelData, FileHandle targetFile);

    LevelData load(FileHandle localFile, FileHandle internalFile);
}
