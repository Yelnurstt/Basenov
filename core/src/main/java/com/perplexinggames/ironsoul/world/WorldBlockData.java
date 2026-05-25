package com.perplexinggames.ironsoul.world;

import com.perplexinggames.ironsoul.level.BlockData;
import com.perplexinggames.ironsoul.terrain.TerrainPath;
import com.perplexinggames.ironsoul.terrain.spline.SplineLayer;
import com.perplexinggames.ironsoul.terrain.spline.SplinePath;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class WorldBlockData {
    public String id;
    public String name;
    public int width;
    public int height;
    public List<BlockData> tiles;
    public List<TerrainPath> terrain;
    public List<SplinePath> splinePaths;
    public List<SplineLayer> splineLayers;
    public List<WorldElementData> objects;
    public List<WorldElementData> enemies;
    public List<WorldElementData> rewards;
    public List<WorldElementData> triggers;
    public List<GateData> gates;
    public List<SpawnPointData> spawnPoints;
    public Map<String, String> metadata;

    public WorldBlockData() {
        tiles = new ArrayList<>();
        terrain = new ArrayList<>();
        splinePaths = new ArrayList<>();
        splineLayers = new ArrayList<>();
        objects = new ArrayList<>();
        enemies = new ArrayList<>();
        rewards = new ArrayList<>();
        triggers = new ArrayList<>();
        gates = new ArrayList<>();
        spawnPoints = new ArrayList<>();
        metadata = new LinkedHashMap<>();
    }

    public WorldBlockData(String id, String name, int width, int height) {
        this();
        this.id = id;
        this.name = name;
        this.width = width;
        this.height = height;
    }

    public WorldBlockData copy() {
        WorldBlockData copy = new WorldBlockData(id, name, width, height);
        for (BlockData tile : tiles) {
            if (tile != null) {
                copy.tiles.add(tile.copy());
            }
        }
        for (TerrainPath terrainPath : terrain) {
            if (terrainPath != null) {
                copy.terrain.add(terrainPath.copy());
            }
        }
        for (SplinePath splinePath : splinePaths) {
            if (splinePath != null) {
                copy.splinePaths.add(splinePath.copy());
            }
        }
        for (SplineLayer splineLayer : splineLayers) {
            if (splineLayer != null) {
                copy.splineLayers.add(splineLayer.copy());
            }
        }
        copyElements(objects, copy.objects);
        copyElements(enemies, copy.enemies);
        copyElements(rewards, copy.rewards);
        copyElements(triggers, copy.triggers);
        for (GateData gate : gates) {
            if (gate != null) {
                copy.gates.add(gate.copy());
            }
        }
        for (SpawnPointData spawnPoint : spawnPoints) {
            if (spawnPoint != null) {
                copy.spawnPoints.add(spawnPoint.copy());
            }
        }
        copy.metadata.putAll(metadata);
        return copy;
    }

    public int getPixelWidth(int tileSize) {
        return width * tileSize;
    }

    public int getPixelHeight(int tileSize) {
        return height * tileSize;
    }

    private void copyElements(List<WorldElementData> source, List<WorldElementData> target) {
        for (WorldElementData element : source) {
            if (element != null) {
                target.add(element.copy());
            }
        }
    }
}
