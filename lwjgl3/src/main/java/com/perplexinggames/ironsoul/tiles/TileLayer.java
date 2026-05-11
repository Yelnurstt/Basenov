package com.perplexinggames.ironsoul.tiles;

import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Rectangle;

public class TileLayer {
    private String name;
    private int width, height;
    private int tileWidth, tileHeight;
    private int[][] tiles;
    private boolean visible;
    private float opacity;
    private float parallaxFactor; // для эффекта параллакса

    public TileLayer(String name, int width, int height, int tileWidth, int tileHeight) {
        this.name = name;
        this.width = width;
        this.height = height;
        this.tileWidth = tileWidth;
        this.tileHeight = tileHeight;
        this.tiles = new int[height][width];
        this.visible = true;
        this.opacity = 1.0f;
        this.parallaxFactor = 1.0f;

        // Инициализация пустыми тайлами (-1)
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                tiles[y][x] = -1;
            }
        }
    }

    public void setTile(int x, int y, int tileId) {
        if (isValidPosition(x, y)) {
            tiles[y][x] = tileId;
        }
    }

    public int getTile(int x, int y) {
        if (isValidPosition(x, y)) {
            return tiles[y][x];
        }
        return -1;
    }

    private boolean isValidPosition(int x, int y) {
        return x >= 0 && x < width && y >= 0 && y < height;
    }

    public void render(SpriteBatch batch, OrthographicCamera camera, TileSet tileSet) {
        if (!visible) return;

        // Расчет видимой области (фрустум каллинг)
        int startX = Math.max(0, (int)((camera.position.x - camera.viewportWidth / 2) / tileWidth) - 1);
        int startY = Math.max(0, (int)((camera.position.y - camera.viewportHeight / 2) / tileHeight) - 1);
        int endX = Math.min(width, startX + (int)(camera.viewportWidth / tileWidth) + 2);
        int endY = Math.min(height, startY + (int)(camera.viewportHeight / tileHeight) + 2);

        batch.setColor(1, 1, 1, opacity);
        for (int y = startY; y < endY; y++) {
            for (int x = startX; x < endX; x++) {
                int tileId = tiles[y][x];
                if (tileId != -1) {
                    Tile tile = tileSet.getTile(tileId);
                    if (tile != null) {
                        float drawX = x * tileWidth;
                        float drawY = y * tileHeight;

                        // Эффект параллакса для фоновых слоев
                        if (parallaxFactor != 1.0f) {
                            drawX += (camera.position.x - camera.viewportWidth / 2) * (1 - parallaxFactor);
                            drawY += (camera.position.y - camera.viewportHeight / 2) * (1 - parallaxFactor);
                        }

                        batch.draw(tile.getTexture(), drawX, drawY, tileWidth, tileHeight);
                    }
                }
            }
        }
        batch.setColor(1, 1, 1, 1);
    }

    public void clearLayer() {
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                tiles[y][x] = -1;
            }
        }
    }

    // Геттеры и сеттеры
    public String getName() { return name; }
    public boolean isVisible() { return visible; }
    public void setVisible(boolean visible) { this.visible = visible; }
    public float getOpacity() { return opacity; }
    public void setOpacity(float opacity) { this.opacity = Math.max(0, Math.min(1, opacity)); }
    public float getParallaxFactor() { return parallaxFactor; }
    public void setParallaxFactor(float parallaxFactor) { this.parallaxFactor = parallaxFactor; }
    public int getWidth() { return width; }
    public int getHeight() { return height; }
}
