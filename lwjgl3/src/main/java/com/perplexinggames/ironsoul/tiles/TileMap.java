package com.perplexinggames.ironsoul.tiles;

import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.utils.Array;

public class TileMap {
    private TileSet tileSet;
    private Array<TileLayer> layers;
    private int width, height;
    private int tileWidth, tileHeight;
    private CollisionManager collisionManager;
    private TileLogic tileLogic;

    public TileMap(int width, int height, int tileWidth, int tileHeight) {
        this.width = width;
        this.height = height;
        this.tileWidth = tileWidth;
        this.tileHeight = tileHeight;
        this.layers = new Array<>();
    }

    public void setTileSet(TileSet tileSet) {
        this.tileSet = tileSet;
    }

    public void addLayer(TileLayer layer) {
        layers.add(layer);
    }

    public TileLayer getLayer(String name) {
        for (TileLayer layer : layers) {
            if (layer.getName().equals(name)) {
                return layer;
            }
        }
        return null;
    }

    public void removeLayer(String name) {
        TileLayer layer = getLayer(name);
        if (layer != null) {
            layers.removeValue(layer, true);
        }
    }

    public void render(SpriteBatch batch, OrthographicCamera camera) {
        // Сортируем слои по глубине (parallaxFactor)
        Array<TileLayer> sortedLayers = new Array<>(layers);
        sortedLayers.sort((l1, l2) -> Float.compare(l1.getParallaxFactor(), l2.getParallaxFactor()));

        for (TileLayer layer : sortedLayers) {
            layer.render(batch, camera, tileSet);
        }
    }

    public void update(float delta) {
        // Обновление анимированных тайлов, погодных эффектов и т.д.
    }

    public CollisionManager getCollisionManager() {
        if (collisionManager == null) {
            collisionManager = new CollisionManager(this);
        }
        return collisionManager;
    }

    public TileLogic getTileLogic() {
        if (tileLogic == null) {
            tileLogic = new TileLogic(this);
        }
        return tileLogic;
    }

    // Создание простой тестовой карты
    public void generateTestMap() {
        TileLayer ground = new TileLayer("ground", width, height, tileWidth, tileHeight);
        TileLayer collision = new TileLayer("collision", width, height, tileWidth, tileHeight);
        TileLayer logic = new TileLayer("logic", width, height, tileWidth, tileHeight);

        // Стены по краям
        for (int x = 0; x < width; x++) {
            ground.setTile(x, 0, 1); // пол
            ground.setTile(x, height-1, 1);
            collision.setTile(x, 0, 1);
            collision.setTile(x, height-1, 1);
        }

        for (int y = 0; y < height; y++) {
            ground.setTile(0, y, 1);
            ground.setTile(width-1, y, 1);
            collision.setTile(0, y, 1);
            collision.setTile(width-1, y, 1);
        }

        // Добавляем немного воды и лавы
        logic.setTile(10, 5, 2); // вода
        logic.setTile(20, 8, 3); // лава

        addLayer(ground);
        addLayer(collision);
        addLayer(logic);
    }

    // Геттеры
    public int getWidth() { return width; }
    public int getHeight() { return height; }
    public int getTileWidth() { return tileWidth; }
    public int getTileHeight() { return tileHeight; }
    public TileSet getTileSet() { return tileSet; }
    public Array<TileLayer> getLayers() { return layers; }
}
