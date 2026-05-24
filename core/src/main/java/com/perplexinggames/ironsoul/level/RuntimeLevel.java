package com.perplexinggames.ironsoul.level;

import com.badlogic.gdx.math.MathUtils;
import com.perplexinggames.ironsoul.terrain.TerrainPath;
import com.perplexinggames.ironsoul.terrain.TerrainPoint;
import com.perplexinggames.ironsoul.world.WorldBlockData;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Collections;
import java.util.List;

import com.badlogic.gdx.utils.LongMap;

public class RuntimeLevel {
    private String id;
    private String name;
    private int width;
    private int height;
    private int tileSize;
    private final LongMap<BlockData> blocks;
    private final List<TerrainPath> terrainPaths;

    public RuntimeLevel(LevelData levelData) {
        this.blocks = new LongMap<>();
        this.terrainPaths = new ArrayList<>();
        apply(levelData);
    }

    public void apply(LevelData levelData) {
        id = levelData.id == null ? "test-level" : levelData.id;
        name = levelData.name == null ? "Test Level" : levelData.name;
        width = levelData.width;
        height = levelData.height;
        tileSize = levelData.tileSize;
        blocks.clear();
        terrainPaths.clear();

        if (levelData.blocks != null) {
            for (BlockData block : levelData.blocks) {
                if (block == null || block.type == null || !isInside(block.x, block.y)) {
                    continue;
                }
                blocks.put(pack(block.x, block.y), block.copy());
            }
        }

        if (levelData.terrainPaths != null) {
            for (TerrainPath terrainPath : levelData.terrainPaths) {
                TerrainPath sanitized = sanitizeTerrainPath(terrainPath);
                if (sanitized != null) {
                    terrainPaths.add(sanitized);
                }
            }
        }
    }

    public LevelData toLevelData() {
        LevelData levelData = new LevelData(id, name, width, height, tileSize);
        levelData.blocks.addAll(copyBlocks());
        levelData.terrainPaths.addAll(copyTerrainPaths());
        return levelData;
    }

    public void apply(WorldBlockData worldBlockData, int tileSize) {
        if (worldBlockData == null) {
            return;
        }

        LevelData levelData = new LevelData(
            worldBlockData.id,
            worldBlockData.name,
            worldBlockData.width,
            worldBlockData.height,
            tileSize
        );
        levelData.blocks.addAll(worldBlockData.tiles);
        levelData.terrainPaths.addAll(worldBlockData.terrain);
        apply(levelData);
    }

    public List<BlockData> copyBlocks() {
        List<BlockData> blockCopies = new ArrayList<>(blocks.size);
        for (BlockData block : blocks.values()) {
            blockCopies.add(block.copy());
        }
        blockCopies.sort(Comparator.comparingInt((BlockData block) -> block.y).thenComparingInt(block -> block.x));
        return blockCopies;
    }

    public List<TerrainPath> copyTerrainPaths() {
        List<TerrainPath> copies = new ArrayList<>(terrainPaths.size());
        for (TerrainPath terrainPath : terrainPaths) {
            copies.add(terrainPath.copy());
        }
        return copies;
    }

    public ResizeResult resize(int newWidth, int newHeight) {
        ResizeResult result = new ResizeResult();
        if (newWidth <= 0 || newHeight <= 0) {
            result.addWarning("Block size must be greater than zero.");
            return result;
        }

        width = newWidth;
        height = newHeight;
        clampBlocks(result);
        clampTerrain(result);
        return result;
    }

    public boolean setBlock(int x, int y, BlockType type) {
        if (!isInside(x, y)) {
            return false;
        }

        long key = pack(x, y);
        BlockData existing = blocks.get(key);
        if (existing != null && existing.type == type) {
            return false;
        }

        blocks.put(key, new BlockData(x, y, type));
        return true;
    }

    public void restoreBlock(BlockData blockData) {
        if (blockData == null || blockData.type == null || !isInside(blockData.x, blockData.y)) {
            return;
        }
        blocks.put(pack(blockData.x, blockData.y), blockData.copy());
    }

    public BlockData removeBlock(int x, int y) {
        if (!isInside(x, y)) {
            return null;
        }
        return blocks.remove(pack(x, y));
    }

    public boolean hasBlock(int x, int y) {
        return getBlock(x, y) != null;
    }

    public BlockData getBlock(int x, int y) {
        return blocks.get(pack(x, y));
    }

    public BlockData getBlockCopy(int x, int y) {
        BlockData block = getBlock(x, y);
        return block == null ? null : block.copy();
    }

    public Iterable<BlockData> getBlocks() {
        return blocks.values();
    }

    public List<TerrainPath> getTerrainPaths() {
        return Collections.unmodifiableList(terrainPaths);
    }

    public TerrainPath getTerrainPath(String pathId) {
        if (pathId == null) {
            return null;
        }
        for (TerrainPath terrainPath : terrainPaths) {
            if (pathId.equals(terrainPath.getId())) {
                return terrainPath.copy();
            }
        }
        return null;
    }

    public boolean setTerrainPath(TerrainPath terrainPath) {
        TerrainPath sanitized = sanitizeTerrainPath(terrainPath);
        if (sanitized == null) {
            return false;
        }

        for (int i = 0; i < terrainPaths.size(); i++) {
            TerrainPath existing = terrainPaths.get(i);
            if (sanitized.getId().equals(existing.getId())) {
                if (terrainPathsEqual(existing, sanitized)) {
                    return false;
                }
                terrainPaths.set(i, sanitized);
                return true;
            }
        }

        terrainPaths.add(sanitized);
        return true;
    }

    public boolean removeTerrainPath(String pathId) {
        if (pathId == null) {
            return false;
        }
        for (int i = 0; i < terrainPaths.size(); i++) {
            if (pathId.equals(terrainPaths.get(i).getId())) {
                terrainPaths.remove(i);
                return true;
            }
        }
        return false;
    }

    public int getTerrainPathCount() {
        return terrainPaths.size();
    }

    public int getTerrainPointCount() {
        int count = 0;
        for (TerrainPath terrainPath : terrainPaths) {
            count += terrainPath.getPoints().size();
        }
        return count;
    }

    public boolean isInside(int x, int y) {
        return x >= 0 && x < width && y >= 0 && y < height;
    }

    public int getBlockCount() {
        return blocks.size;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    public int getTileSize() {
        return tileSize;
    }

    public int getPixelWidth() {
        return width * tileSize;
    }

    public int getPixelHeight() {
        return height * tileSize;
    }

    private TerrainPath sanitizeTerrainPath(TerrainPath terrainPath) {
        if (terrainPath == null || terrainPath.getId() == null || terrainPath.getId().isEmpty()) {
            return null;
        }

        List<TerrainPoint> pointCopies = new ArrayList<>();
        for (TerrainPoint point : terrainPath.getPoints()) {
            if (point == null || point.getId() == null || point.getId().isEmpty()) {
                continue;
            }
            pointCopies.add(point.copy());
        }

        return new TerrainPath(
            terrainPath.getId(),
            pointCopies,
            terrainPath.getCurveType() == null ? TerrainPath.CurveType.LINEAR : terrainPath.getCurveType(),
            terrainPath.getMaterial() == null ? "default" : terrainPath.getMaterial(),
            terrainPath.getDebugWidth(),
            terrainPath.getFriction()
        );
    }

    private boolean terrainPathsEqual(TerrainPath first, TerrainPath second) {
        if (!first.getId().equals(second.getId())) {
            return false;
        }
        List<TerrainPoint> firstPoints = first.getPoints();
        List<TerrainPoint> secondPoints = second.getPoints();
        if (firstPoints.size() != secondPoints.size()) {
            return false;
        }
        for (int i = 0; i < firstPoints.size(); i++) {
            TerrainPoint firstPoint = firstPoints.get(i);
            TerrainPoint secondPoint = secondPoints.get(i);
            if (!firstPoint.getId().equals(secondPoint.getId())) {
                return false;
            }
            if (Float.compare(firstPoint.getX(), secondPoint.getX()) != 0) {
                return false;
            }
            if (Float.compare(firstPoint.getY(), secondPoint.getY()) != 0) {
                return false;
            }
        }
        return true;
    }

    private long pack(int x, int y) {
        return ((long) x << 32) | (y & 0xffffffffL);
    }

    private void clampBlocks(ResizeResult result) {
        LongMap<BlockData> clampedBlocks = new LongMap<>();
        int clampedCount = 0;
        for (BlockData block : blocks.values()) {
            int clampedX = MathUtils.clamp(block.x, 0, width - 1);
            int clampedY = MathUtils.clamp(block.y, 0, height - 1);
            if (clampedX != block.x || clampedY != block.y) {
                clampedCount++;
            }
            clampedBlocks.put(pack(clampedX, clampedY), new BlockData(clampedX, clampedY, block.type));
        }
        blocks.clear();
        for (BlockData block : clampedBlocks.values()) {
            blocks.put(pack(block.x, block.y), block);
        }
        if (clampedCount > 0) {
            result.addWarning("Clamped " + clampedCount + " tile blocks to the resized block bounds.");
        }
    }

    private void clampTerrain(ResizeResult result) {
        float maxX = getPixelWidth();
        float maxY = getPixelHeight();
        int clampedPoints = 0;

        for (int i = 0; i < terrainPaths.size(); i++) {
            TerrainPath terrainPath = terrainPaths.get(i);
            List<TerrainPoint> clamped = new ArrayList<>(terrainPath.getPoints().size());
            for (TerrainPoint point : terrainPath.getPoints()) {
                float clampedX = MathUtils.clamp(point.getX(), 0f, maxX);
                float clampedY = MathUtils.clamp(point.getY(), 0f, maxY);
                if (Float.compare(clampedX, point.getX()) != 0 || Float.compare(clampedY, point.getY()) != 0) {
                    clampedPoints++;
                }
                clamped.add(new TerrainPoint(point.getId(), clampedX, clampedY));
            }
            terrainPaths.set(i, new TerrainPath(
                terrainPath.getId(),
                clamped,
                terrainPath.getCurveType(),
                terrainPath.getMaterial(),
                terrainPath.getDebugWidth(),
                terrainPath.getFriction()
            ));
        }

        if (clampedPoints > 0) {
            result.addWarning("Clamped " + clampedPoints + " terrain points to the resized block bounds.");
        }
    }

    public static class ResizeResult {
        private final List<String> warnings = new ArrayList<>();

        public void addWarning(String warning) {
            warnings.add(warning);
        }

        public boolean hasWarnings() {
            return !warnings.isEmpty();
        }

        public List<String> getWarnings() {
            return Collections.unmodifiableList(warnings);
        }
    }
}
