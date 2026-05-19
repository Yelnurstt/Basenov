package com.perplexinggames.ironsoul.tiles;

import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;

public class CollisionManager {
    private TileMap tileMap;
    private Array<Rectangle> collisionRects;

    public CollisionManager(TileMap tileMap) {
        this.tileMap = tileMap;
        this.collisionRects = new Array<>();
    }

    // Проверка коллизии с прямоугольником сущности
    public boolean checkCollision(Rectangle entity, String... collisionLayers) {
        int leftTile = (int)(entity.x / tileMap.getTileWidth());
        int rightTile = (int)((entity.x + entity.width - 1) / tileMap.getTileWidth());
        int bottomTile = (int)(entity.y / tileMap.getTileHeight());
        int topTile = (int)((entity.y + entity.height - 1) / tileMap.getTileHeight());

        for (int y = bottomTile; y <= topTile; y++) {
            for (int x = leftTile; x <= rightTile; x++) {
                for (String layerName : collisionLayers) {
                    TileLayer layer = tileMap.getLayer(layerName);
                    if (layer != null) {
                        int tileId = layer.getTile(x, y);
                        if (tileId != -1) {
                            Tile tile = tileMap.getTileSet().getTile(tileId);
                            if (tile != null && tile.isSolid()) {
                                return true;
                            }
                        }
                    }
                }
            }
        }
        return false;
    }

    // Получение тайла в мировой позиции
    public Tile getTileAt(float worldX, float worldY, String layerName) {
        int tileX = (int)(worldX / tileMap.getTileWidth());
        int tileY = (int)(worldY / tileMap.getTileHeight());

        TileLayer layer = tileMap.getLayer(layerName);
        if (layer != null) {
            int tileId = layer.getTile(tileX, tileY);
            if (tileId != -1) {
                return tileMap.getTileSet().getTile(tileId);
            }
        }
        return null;
    }

    // Проверка конкретного тайла на позиции
    public boolean isSolidAt(int tileX, int tileY, String layerName) {
        TileLayer layer = tileMap.getLayer(layerName);
        if (layer != null) {
            int tileId = layer.getTile(tileX, tileY);
            if (tileId != -1) {
                Tile tile = tileMap.getTileSet().getTile(tileId);
                return tile != null && tile.isSolid();
            }
        }
        return false;
    }

    // A* поиск пути для навигации
    public Array<Vector2> findPath(int startX, int startY, int targetX, int targetY, String collisionLayer) {
        Array<Vector2> path = new Array<>();

        // Простая реализация A* (можно расширить)
        if (startX == targetX && startY == targetY) {
            path.add(new Vector2(targetX, targetY));
            return path;
        }

        // Здесь должна быть полноценная A* реализация
        // Для краткости - упрощенная версия
        Array<Vector2> openList = new Array<>();
        Array<Vector2> closedList = new Array<>();

        openList.add(new Vector2(startX, startY));

        // TODO: Реализовать полный A* алгоритм

        return path;
    }

    // Получение соседних тайлов
    public Array<Vector2> getNeighbors(int x, int y, String collisionLayer) {
        Array<Vector2> neighbors = new Array<>();
        int[][] directions = {{0,1},{1,0},{0,-1},{-1,0}};

        for (int[] dir : directions) {
            int newX = x + dir[0];
            int newY = y + dir[1];

            if (!isSolidAt(newX, newY, collisionLayer)) {
                neighbors.add(new Vector2(newX, newY));
            }
        }

        return neighbors;
    }

    // Проверка линии видимости
    public boolean lineOfSight(int x1, int y1, int x2, int y2, String collisionLayer) {
        // Алгоритм Брезенхема для линии
        int dx = Math.abs(x2 - x1);
        int dy = Math.abs(y2 - y1);
        int sx = x1 < x2 ? 1 : -1;
        int sy = y1 < y2 ? 1 : -1;
        int err = dx - dy;

        int currentX = x1;
        int currentY = y1;

        while (currentX != x2 || currentY != y2) {
            if (isSolidAt(currentX, currentY, collisionLayer)) {
                return false;
            }

            int e2 = 2 * err;
            if (e2 > -dy) {
                err -= dy;
                currentX += sx;
            }
            if (e2 < dx) {
                err += dx;
                currentY += sy;
            }
        }

        return true;
    }
}
