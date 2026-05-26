// core/src/main/java/com/perplexinggames/ironsoul/screens/DemoGameScreen.java
package com.perplexinggames.ironsoul.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.ScreenUtils;
import com.perplexinggames.ironsoul.entities.Player;
import com.perplexinggames.ironsoul.projectile.ProjectileSystem;
import com.perplexinggames.ironsoul.tiles.*;
import com.perplexinggames.ironsoul.utils.TextureGenerator;

public class DemoGameScreen implements Screen {
    private SpriteBatch batch;
    private OrthographicCamera camera;
    private TileMap tileMap;
    private Player player;
    private TileSet tileSet;

    // Новая система снарядов
    private ProjectileSystem projectileSystem;

    private static final int TILE_SIZE = 32;
    private static final int MAP_WIDTH = 40;
    private static final int MAP_HEIGHT = 30;
    private static final int WORLD_WIDTH = MAP_WIDTH * TILE_SIZE;
    private static final int WORLD_HEIGHT = MAP_HEIGHT * TILE_SIZE;

    private Texture groundTexture;
    private Texture wallTexture;
    private Texture waterTexture;
    private Texture lavaTexture;
    private Texture spikeTexture;

    @Override
    public void show() {
        batch = new SpriteBatch();
        camera = new OrthographicCamera(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());

        createDemoTextures();
        createTileSet();
        createTileMap();

        player = new Player(TILE_SIZE * 2, TILE_SIZE * 2);

        // Инициализируем систему снарядов (передаем CollisionManager карты)
        projectileSystem = new ProjectileSystem();
        camera.position.set(player.getX(), player.getY(), 0);
        camera.update();
    }

    private void createDemoTextures() {
        groundTexture = TextureGenerator.createTileTexture(TILE_SIZE, 0x88AA88FF, 0x668866FF);
        wallTexture = TextureGenerator.createTileTexture(TILE_SIZE, 0x666666FF, 0x444444FF);
        waterTexture = TextureGenerator.createTileTexture(TILE_SIZE, 0x4488CCFF, 0x2266AAFF);
        lavaTexture = TextureGenerator.createTileTexture(TILE_SIZE, 0xFF6600FF, 0xCC4400FF);
        spikeTexture = TextureGenerator.createTileTexture(TILE_SIZE, 0xCCCCCCFF, 0xFF0000FF);
    }

    private void createTileSet() {
        tileSet = new TileSet("demo", TILE_SIZE, TILE_SIZE);
        tileSet.addTile(0, new Tile(0, new TextureRegion(groundTexture), Tile.TileType.GROUND));
        tileSet.addTile(1, new Tile(1, new TextureRegion(wallTexture), Tile.TileType.WALL));

        Tile waterTile = new Tile(2, new TextureRegion(waterTexture), Tile.TileType.WATER);
        waterTile.setDamage(5);
        tileSet.addTile(2, waterTile);

        Tile lavaTile = new Tile(3, new TextureRegion(lavaTexture), Tile.TileType.LAVA);
        lavaTile.setDamage(15);
        tileSet.addTile(3, lavaTile);

        Tile spikeTile = new Tile(4, new TextureRegion(spikeTexture), Tile.TileType.SPIKE);
        spikeTile.setDamage(20);
        tileSet.addTile(4, spikeTile);
    }

    private void createTileMap() {
        tileMap = new TileMap(MAP_WIDTH, MAP_HEIGHT, TILE_SIZE, TILE_SIZE);
        tileMap.setTileSet(tileSet);

        TileLayer groundLayer = new TileLayer("ground", MAP_WIDTH, MAP_HEIGHT, TILE_SIZE, TILE_SIZE);
        TileLayer collisionLayer = new TileLayer("collision", MAP_WIDTH, MAP_HEIGHT, TILE_SIZE, TILE_SIZE);
        TileLayer logicLayer = new TileLayer("logic", MAP_WIDTH, MAP_HEIGHT, TILE_SIZE, TILE_SIZE);

        generateTestMap(groundLayer, collisionLayer, logicLayer);

        tileMap.addLayer(groundLayer);
        tileMap.addLayer(collisionLayer);
        tileMap.addLayer(logicLayer);

        groundLayer.setParallaxFactor(1.0f);
        logicLayer.setVisible(true);
    }

    private void generateTestMap(TileLayer ground, TileLayer collision, TileLayer logic) {
        for (int x = 0; x < MAP_WIDTH; x++) {
            for (int y = 0; y < MAP_HEIGHT; y++) {
                ground.setTile(x, y, 0);
            }
        }
        for (int x = 0; x < MAP_WIDTH; x++) {
            ground.setTile(x, 0, 1); collision.setTile(x, 0, 1);
            ground.setTile(x, MAP_HEIGHT - 1, 1); collision.setTile(x, MAP_HEIGHT - 1, 1);
        }
        for (int y = 0; y < MAP_HEIGHT; y++) {
            ground.setTile(0, y, 1); collision.setTile(0, y, 1);
            ground.setTile(MAP_WIDTH - 1, y, 1); collision.setTile(MAP_WIDTH - 1, y, 1);
        }
        for (int y = 5; y < 15; y++) {
            ground.setTile(10, y, 1); collision.setTile(10, y, 1);
        }
        for (int x = 15; x < 25; x++) {
            ground.setTile(x, 20, 1); collision.setTile(x, 20, 1);
        }
        for (int x = 30; x < 38; x++) {
            for (int y = 2; y < 8; y++) {
                ground.setTile(x, y, 3); logic.setTile(x, y, 3);
            }
        }
        for (int x = 5; x < 12; x++) {
            for (int y = 22; y < 28; y++) {
                ground.setTile(x, y, 2); logic.setTile(x, y, 2);
            }
        }
        for (int x = 18; x < 22; x++) {
            for (int y = 10; y < 14; y++) {
                ground.setTile(x, y, 4); logic.setTile(x, y, 4);
            }
        }
        for (int x = 25; x < 30; x++) {
            for (int y = 15; y < 20; y++) {
                ground.setTile(x, y, 0);
            }
        }
        for (int x = 24; x <= 30; x++) {
            ground.setTile(x, 14, 1); ground.setTile(x, 20, 1);
            collision.setTile(x, 14, 1); collision.setTile(x, 20, 1);
        }
        for (int y = 14; y <= 20; y++) {
            ground.setTile(24, y, 1); ground.setTile(30, y, 1);
            collision.setTile(24, y, 1); collision.setTile(30, y, 1);
        }
    }

    @Override
    public void render(float delta) {
        ScreenUtils.clear(0.1f, 0.1f, 0.2f, 1);

        // 1. Прицеливание с помощью мыши
        Vector3 mousePos = new Vector3(Gdx.input.getX(), Gdx.input.getY(), 0);
        camera.unproject(mousePos);
        player.setAimTarget(mousePos.x, mousePos.y);

        // 2. Стрельба по клику ЛКМ
        if (Gdx.input.isButtonJustPressed(Input.Buttons.LEFT)) {
            // Вычисляем позицию дула (примерно по центру)
            float startX = player.getX() + player.getWidth() / 2f;
            float startY = player.getY() + player.getHeight() * 0.7f;

            // Направление от дула к мышке
            float angle = MathUtils.atan2(mousePos.y - startY, mousePos.x - startX);
            float dirX = MathUtils.cos(angle);
            float dirY = MathUtils.sin(angle);

            float shootSpeed = 900f; // Начальная скорость полета пули
            projectileSystem.spawnPhysicsShot(startX, startY, dirX, dirY, shootSpeed);

            // Физическая отдача танка в противоположную сторону
            player.getVelocity().x -= dirX * (shootSpeed * 0.3f);
        }

        // 3. Обновление логики
        player.update(delta);
        projectileSystem.update(delta); // Обновляем полет ракет и взрывы
        clampPlayerPosition();

        if (tileMap.getCollisionManager().checkCollision(player.getBoundingBox(), "collision")) {
            player.handleCollision();
        }
        tileMap.getTileLogic().updateEntityInteraction(player);
        updateCamera();

        // 4. Отрисовка всего
        batch.setProjectionMatrix(camera.combined);
        batch.begin();
        tileMap.render(batch, camera);
        player.render(batch);
        projectileSystem.render(batch); // Рендер пуль и эффектов
        batch.end();
    }

    private void clampPlayerPosition() {
        float playerX = player.getX();
        float playerY = player.getY();
        if (playerX < 0) player.setX(0);
        if (playerY < 0) player.setY(0);
        if (playerX > WORLD_WIDTH - TILE_SIZE) player.setX(WORLD_WIDTH - TILE_SIZE);
        if (playerY > WORLD_HEIGHT - TILE_SIZE) player.setY(WORLD_HEIGHT - TILE_SIZE);
    }

    private void updateCamera() {
        camera.position.set(player.getX() + TILE_SIZE / 2, player.getY() + TILE_SIZE / 2, 0);
        float camX = Math.min(Math.max(camera.position.x, Gdx.graphics.getWidth() / 2f), WORLD_WIDTH - Gdx.graphics.getWidth() / 2f);
        float camY = Math.min(Math.max(camera.position.y, Gdx.graphics.getHeight() / 2f), WORLD_HEIGHT - Gdx.graphics.getHeight() / 2f);
        camera.position.set(camX, camY, 0);
        camera.update();
    }

    private void drawDebugInfo() {}
    private void printDebugInfo() {}

    @Override
    public void resize(int width, int height) {
        camera.viewportWidth = width;
        camera.viewportHeight = height;
        camera.update();
    }

    @Override public void pause() {}
    @Override public void resume() {}
    @Override public void hide() {}

    @Override
    public void dispose() {
        batch.dispose();
        groundTexture.dispose();
        wallTexture.dispose();
        waterTexture.dispose();
        lavaTexture.dispose();
        spikeTexture.dispose();
        player.dispose();
        if (projectileSystem != null) projectileSystem.dispose();
    }
}
