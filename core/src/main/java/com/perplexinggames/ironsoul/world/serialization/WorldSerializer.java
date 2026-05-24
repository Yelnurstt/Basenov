package com.perplexinggames.ironsoul.world.serialization;

import com.badlogic.gdx.files.FileHandle;
import com.perplexinggames.ironsoul.world.WorldData;

public interface WorldSerializer {
    void save(WorldData worldData, FileHandle targetFile);

    WorldData load(FileHandle localFile, FileHandle internalFile);
}
