package com.perplexinggames.ironsoul.level;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import com.badlogic.gdx.utils.LongMap;

public class RuntimeLevel {
    private String id;
    private String name;
    private int width;
    private int height;
    private int tileSize;
    private final LongMap<BlockData> blocks;

    public RuntimeLevel(LevelData levelData) {
        this.blocks = new LongMap<>();
        apply(levelData);
    }

    public void apply(LevelData levelData) {
        id = levelData.id == null ? "test-level" : levelData.id;
        name = levelData.name == null ? "Test Level" : levelData.name;
        width = levelData.width;
        height = levelData.height;
        tileSize = levelData.tileSize;
        blocks.clear();

        if (levelData.blocks == null) {
            return;
        }

        for (BlockData block : levelData.blocks) {
            if (block == null || block.type == null || !isInside(block.x, block.y)) {
                continue;
            }
            blocks.put(pack(block.x, block.y), block.copy());
        }
    }

    public LevelData toLevelData() {
        LevelData levelData = new LevelData(id, name, width, height, tileSize);
        List<BlockData> blockCopies = new ArrayList<>(blocks.size);
        for (BlockData block : blocks.values()) {
            blockCopies.add(block.copy());
        }
        blockCopies.sort(Comparator.comparingInt((BlockData block) -> block.y).thenComparingInt(block -> block.x));
        levelData.blocks.addAll(blockCopies);
        return levelData;
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

    private long pack(int x, int y) {
        return ((long) x << 32) | (y & 0xffffffffL);
    }
}
