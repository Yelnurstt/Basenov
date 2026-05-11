package com.perplexinggames.ironsoul.tiles;

import com.badlogic.gdx.math.Rectangle;
import com.perplexinggames.ironsoul.entities.Entity;


import com.badlogic.gdx.math.Vector2;
public class TileLogic {
    private TileMap tileMap;

    public TileLogic(TileMap tileMap) {
        this.tileMap = tileMap;
    }

    // Обновление логики всех тайлов вокруг сущности
    public void updateEntityInteraction(Entity entity) {
        // Сброс модификаторов перед проверкой тайлов
        entity.setMovementMultiplier(1.0f);
        entity.setFriction(0.9f);

        Rectangle bounds = entity.getBoundingBox();
        int leftTile = (int)(bounds.x / tileMap.getTileWidth());
        int rightTile = (int)((bounds.x + bounds.width) / tileMap.getTileWidth());
        int bottomTile = (int)(bounds.y / tileMap.getTileHeight());
        int topTile = (int)((bounds.y + bounds.height) / tileMap.getTileHeight());

        for (int y = bottomTile; y <= topTile; y++) {
            for (int x = leftTile; x <= rightTile; x++) {
                processTileAt(x, y, entity);
            }
        }
    }

    // Обработка конкретного тайла
    private void processTileAt(int x, int y, Entity entity) {
        TileLayer logicLayer = tileMap.getLayer("logic");
        if (logicLayer != null) {
            int tileId = logicLayer.getTile(x, y);
            if (tileId != -1) {
                Tile tile = tileMap.getTileSet().getTile(tileId);
                if (tile != null) {
                    applyTileEffect(tile, entity);
                }
            }
        }
    }

    // Применение эффектов тайла
    private void applyTileEffect(Tile tile, Entity entity) {
        switch (tile.getType()) {
            case LAVA:
                entity.takeDamage(tile.getDamage());
                break;
            case SPIKE:
                entity.takeDamage(tile.getDamage());
                break;
            case WATER:
                // Эффект замедления
                entity.setMovementMultiplier(0.5f);
                break;
            case ICE:
                // увеличение скольжения
                entity.setFriction(0.1f);
                break;
            case CONVEYOR:
                // движение в определенном направлении
                entity.applyForce(getConveyorDirection(tile));
                break;
            case DOOR:
                if (entity.hasKey()) {
                    openDoor(tile);
                }
                break;
            case CHEST:
                if (entity.isInteracting()) {
                    openChest(tile, entity);
                }
                break;
        }
    }

    private Vector2 getConveyorDirection(Tile tile) {
        // Можно хранить направление в метаданных тайла
        return new Vector2(1, 0); // движение вправо
    }

    private void openDoor(Tile tile) {
        // Логика открытия двери
        tile.setSolid(false);
    }

    private void openChest(Tile tile, Entity entity) {
        // Логика открытия сундука
        entity.addItem(getChestItem(tile));
        tile.setInteractive(false);
    }

    private String getChestItem(Tile tile) {
        // Получение предмета из сундука
        return "health_potion";
    }
}
