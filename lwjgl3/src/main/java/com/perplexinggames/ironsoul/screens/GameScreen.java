package com.perplexinggames.ironsoul.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.perplexinggames.ironsoul.tiles.TileMap;
import com.perplexinggames.ironsoul.tiles.TileSet;
import com.perplexinggames.ironsoul.entities.Player;

public class GameScreen implements Screen {
    private SpriteBatch batch;
    private OrthographicCamera camera;
    private TileMap tileMap;
    private Player player;
    private Texture tileTexture;

    @Override
    public void show() {
        batch = new SpriteBatch();
        camera = new OrthographicCamera(800, 600);

        // Создание карты 100x100 тайлов, размер тайла 32x32
        tileMap = new TileMap(100, 100, 32, 32);

        // Загрузка тайлов из текстур и создание TileSet
        tileTexture = new Texture(Gdx.files.internal("libgdx128.png")); // Используем доступный ассет для примера
        TextureRegion[][] splitTiles = TextureRegion.split(tileTexture, 32, 32);
        TileSet tileSet = TileSet.loadFromAtlas("default", 32, 32, splitTiles);
        tileMap.setTileSet(tileSet);

        // Генерация тестовой карты
        tileMap.generateTestMap();

        player = new Player(100, 100, 32, 32);
    }

    @Override
    public void render(float delta) {
        Gdx.gl.glClearColor(0, 0, 0, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        // Обновление камеры
        camera.position.set(player.getX(), player.getY(), 0);
        camera.update();

        // Обновление игрока и карты
        player.update(delta);
        tileMap.update(delta);

        // Проверка коллизий
        if (tileMap.getCollisionManager().checkCollision(player.getBoundingBox(), "collision")) {
            player.handleCollision();
        }

        // Обработка взаимодействия с тайлами
        tileMap.getTileLogic().updateEntityInteraction(player);

        // Рендеринг
        batch.setProjectionMatrix(camera.combined);
        batch.begin();
        tileMap.render(batch, camera);
        player.render(batch);
        batch.end();
    }

    @Override
    public void resize(int width, int height) {
        camera.viewportWidth = width;
        camera.viewportHeight = height;
        camera.update();
    }

    @Override
    public void pause() {
    }

    @Override
    public void resume() {
    }

    @Override
    public void hide() {
    }

    @Override
    public void dispose() {
        batch.dispose();
        if (tileTexture != null) {
            tileTexture.dispose();
        }
    }
}
