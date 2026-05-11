package com.perplexinggames.ironsoul.tiles;

import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.utils.IntMap;
import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonValue;

public class TileSet {
    private String name;
    private int tileWidth, tileHeight;
    private IntMap<Tile> tiles;
    private TextureRegion sourceTexture;
    private int firstGid; // для совместимости с Tiled

    public TileSet(String name, int tileWidth, int tileHeight) {
        this.name = name;
        this.tileWidth = tileWidth;
        this.tileHeight = tileHeight;
        this.tiles = new IntMap<>();
        this.firstGid = 1;
    }

    public void addTile(int id, Tile tile) {
        tiles.put(id, tile);
    }

    public Tile getTile(int id) {
        return tiles.get(id);
    }

    public boolean hasTile(int id) {
        return tiles.containsKey(id);
    }

    // Загрузка из текстурного атласа
    public static TileSet loadFromAtlas(String name, int tileWidth, int tileHeight,
                                        TextureRegion[][] atlas) {
        TileSet tileSet = new TileSet(name, tileWidth, tileHeight);
        int id = 0;
        for (int y = 0; y < atlas.length; y++) {
            for (int x = 0; x < atlas[y].length; x++) {
                Tile.TileType type = Tile.TileType.GROUND;
                // Можно определить тип по имени текстуры или позиции
                Tile tile = new Tile(id, atlas[y][x], type);
                tileSet.addTile(id++, tile);
            }
        }
        return tileSet;
    }

    // Геттеры
    public int getTileWidth() { return tileWidth; }
    public int getTileHeight() { return tileHeight; }
    public String getName() { return name; }
    public int getFirstGid() { return firstGid; }
    public void setFirstGid(int firstGid) { this.firstGid = firstGid; }
}
