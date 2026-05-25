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
import com.perplexinggames.ironsoul.tiles.TileMap;
import com.perplexinggames.ironsoul.tiles.TileSet;
import com.perplexinggames.ironsoul.entities.Player;
import com.perplexinggames.ironsoul.projectile.ProjectileSystem;
import com.perplexinggames.ironsoul.projectile.Projectile;
import com.perplexinggames.ironsoul.core.Main;

public class GameScreen implements Screen {
    private Main game;
    private SpriteBatch batch;
    private OrthographicCamera camera;
    private TileMap tileMap;
    private Player player;
    private Texture tileTexture;
    private ProjectileSystem projectileSystem;

    public GameScreen(Main game) { this.game = game; }

    @Override
    public void show() {
        batch = new SpriteBatch();
        camera = new OrthographicCamera(800, 600);
        tileMap = new TileMap(100, 100, 32, 32);

        tileTexture = new Texture(Gdx.files.internal("libgdx128.png"));
        TextureRegion[][] splitTiles = TextureRegion.split(tileTexture, 32, 32);
        TileSet tileSet = TileSet.loadFromAtlas("default", 32, 32, splitTiles);
        tileMap.setTileSet(tileSet);
        tileMap.generateTestMap();

        player = new Player(100, 100);
        projectileSystem = new ProjectileSystem(); // Инициализируем систему пуль
    }

    @Override
    public void render(float delta) {
        if (Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)) {
            game.showMainMenu();
            return;
        }

        // --- СТРЕЛЬБА ---
        if (Gdx.input.isButtonJustPressed(Input.Buttons.LEFT)) {
            Vector3 mousePos = new Vector3(Gdx.input.getX(), Gdx.input.getY(), 0);
            camera.unproject(mousePos);

            float startX = player.getX() + player.getWidth() / 2f;
            float startY = player.getY() + player.getHeight() * 0.7f;
            float angle = MathUtils.atan2(mousePos.y - startY, mousePos.x - startX);
            float dirX = MathUtils.cos(angle);
            float dirY = MathUtils.sin(angle);

            float shootSpeed = 900f;
            projectileSystem.spawnPhysicsShot(startX, startY, dirX, dirY, shootSpeed);
            player.getVelocity().x -= dirX * (shootSpeed * 0.3f); // Отдача
        }

        Gdx.gl.glClearColor(0, 0, 0, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        player.update(delta);
        tileMap.update(delta);
        projectileSystem.update(delta); // Обновляем пули

        // --- ПРОВЕРКА ПОПАДАНИЙ ПУЛИ В СТЕНЫ ---
        for (Projectile p : projectileSystem.getProjectiles()) {
            if (tileMap.getCollisionManager().checkCollision(p.getBounds(), "collision")) {
                p.destroy();
                projectileSystem.spawnExplosion(p.getX(), p.getY()); // Взрыв!
            }
        }

        if (tileMap.getCollisionManager().checkCollision(player.getBoundingBox(), "collision")) {
            player.handleCollision();
        }
        tileMap.getTileLogic().updateEntityInteraction(player);

        camera.position.set(player.getX(), player.getY(), 0);
        camera.update();

        batch.setProjectionMatrix(camera.combined);
        batch.begin();
        tileMap.render(batch, camera);
        player.render(batch);
        projectileSystem.render(batch); // Отрисовка пуль и взрывов
        batch.end();
    }

    @Override public void resize(int width, int height) { camera.viewportWidth = width; camera.viewportHeight = height; camera.update(); }
    @Override public void pause() {}
    @Override public void resume() {}
    @Override public void hide() {}
    @Override public void dispose() { batch.dispose(); if (tileTexture != null) tileTexture.dispose(); projectileSystem.dispose(); }
}
