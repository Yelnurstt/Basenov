package com.perplexinggames.ironsoul.world.runtime;

import com.badlogic.gdx.Gdx;
import com.perplexinggames.ironsoul.world.WorldData;
import com.perplexinggames.ironsoul.world.WorldGraph;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

public class WorldStreamingService {
    private final WorldData worldData;
    private final WorldGraph worldGraph;
    private final Set<String> loadedBlockIds = new LinkedHashSet<>();
    private String currentBlockId;
    private String previousBlockId;

    public WorldStreamingService(WorldData worldData) {
        this.worldData = worldData;
        this.worldGraph = new WorldGraph(worldData);
        this.currentBlockId = worldData.activeBlockId;
    }

    public void setCurrentBlock(String blockId) {
        if (blockId == null) {
            return;
        }
        previousBlockId = currentBlockId;
        currentBlockId = blockId;
        worldData.activeBlockId = blockId;
        loadBlock(blockId);
        loadConnectedBlocks(blockId);
        unloadFarBlocks();
        log("Current block: " + currentBlockId);
        log("Loaded blocks: " + String.join(", ", loadedBlockIds));
    }

    public void loadBlock(String blockId) {
        if (blockId != null && loadedBlockIds.add(blockId)) {
            log("Loading block: " + blockId);
        }
    }

    public void unloadBlock(String blockId) {
        if (blockId != null && loadedBlockIds.remove(blockId)) {
            log("Unloading far block: " + blockId);
        }
    }

    public void loadConnectedBlocks(String blockId) {
        for (String connectedBlockId : worldGraph.findConnectedBlocks(blockId)) {
            loadBlock(connectedBlockId);
        }
    }

    public void unloadFarBlocks() {
        Set<String> keepLoaded = new LinkedHashSet<>();
        if (currentBlockId != null) {
            keepLoaded.add(currentBlockId);
            keepLoaded.addAll(worldGraph.findConnectedBlocks(currentBlockId));
        }
        if (previousBlockId != null) {
            keepLoaded.add(previousBlockId);
        }

        List<String> loadedSnapshot = new ArrayList<>(loadedBlockIds);
        for (String loadedBlockId : loadedSnapshot) {
            if (!keepLoaded.contains(loadedBlockId)) {
                unloadBlock(loadedBlockId);
            }
        }
    }

    public boolean isBlockLoaded(String blockId) {
        return loadedBlockIds.contains(blockId);
    }

    public List<String> getLoadedBlocks() {
        return new ArrayList<>(loadedBlockIds);
    }

    public String getCurrentBlock() {
        return currentBlockId;
    }

    public List<String> getConnectedBlocks() {
        return currentBlockId == null ? List.of() : worldGraph.findConnectedBlocks(currentBlockId);
    }

    private void log(String message) {
        if (Gdx.app != null) {
            Gdx.app.log("WorldStreamingService", message);
        }
    }
}
