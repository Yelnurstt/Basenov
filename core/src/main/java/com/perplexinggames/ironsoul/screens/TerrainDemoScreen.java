package com.perplexinggames.ironsoul.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector3;
import com.perplexinggames.ironsoul.entities.PhysicsTank;
import com.perplexinggames.ironsoul.terrain.SegmentTerrainCollisionProvider;
import com.perplexinggames.ironsoul.terrain.TerrainPath;
import com.perplexinggames.ironsoul.terrain.TerrainSegment;

public class TerrainDemoScreen implements Screen {
    private SpriteBatch batch;
    private ShapeRenderer shapeRenderer;
    private BitmapFont font;
    private OrthographicCamera camera;

    private TerrainPath terrainPath;
    private PhysicsTank tank;

    // Включает/выключает отображение лучей (probes) и нормалей
    private boolean debugMode = true;

    @Override
    public void show() {
        batch = new SpriteBatch();
        shapeRenderer = new ShapeRenderer();
        font = new BitmapFont();
        camera = new OrthographicCamera(1280, 720);

        // 1. Создаем наш ландшафт (MVP требования)
        terrainPath = new TerrainPath();
        // flat -> slope up -> flat -> slope down -> gap -> steep slope
        terrainPath.addSegment(-500, 100, 300, 100);       // Flat
        terrainPath.addSegment(300, 100, 600, 300);        // Slope Up
        terrainPath.addSegment(600, 300, 900, 300);        // Flat High
        terrainPath.addSegment(900, 300, 1100, 100);       // Slope Down
        // GAP (пропасть) между 1100 и 1250!
        terrainPath.addSegment(1250, 100, 1500, 400);      // Steep Slope
        terrainPath.addSegment(1500, 400, 2000, 400);      // Final Flat

        // 2. Создаем провайдер коллизий
        SegmentTerrainCollisionProvider collisionProvider = new SegmentTerrainCollisionProvider(terrainPath);

        // 3. Создаем танк (спавним в воздухе над стартовой платформой)
        tank = new PhysicsTank(0, 200, collisionProvider);
    }

    @Override
    public void render(float delta) {
        // --- Прицеливание башней (переводим координаты мыши в игровой мир) ---
        Vector3 mousePos = new Vector3(Gdx.input.getX(), Gdx.input.getY(), 0);
        camera.unproject(mousePos);
        tank.setAimTarget(mousePos.x, mousePos.y);

        // --- Обновляем физику и логику танка ---
        tank.update(delta);

        // --- Камера плавно следит за танком ---
        camera.position.x += (tank.physics.x - camera.position.x) * 5f * delta;
        camera.position.y += ((tank.physics.y + 150f) - camera.position.y) * 5f * delta; // Чуть выше танка
        camera.update();

        // Очистка экрана
        Gdx.gl.glClearColor(0.15f, 0.15f, 0.2f, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        // === 1. ОТРИСОВКА ЛАНДШАФТА И ДЕБАГА (Линии) ===
        shapeRenderer.setProjectionMatrix(camera.combined);
        shapeRenderer.begin(ShapeRenderer.ShapeType.Line);

        // Рисуем отрезки земли (зеленые)
        // СТАЛО:
        shapeRenderer.setColor(Color.LIME);
        for (TerrainSegment seg : terrainPath.getSegments()) {
            shapeRenderer.line(seg.getP1().x, seg.getP1().y, seg.getP2().x, seg.getP2().y);
        }

        if (debugMode) {
            // Рисуем лучи (Probes - красные)
            shapeRenderer.setColor(Color.RED);

            float leftExpectedY = tank.physics.y - (tank.physics.trackWidth / 2f) * MathUtils.sinDeg(tank.physics.rotation);
            float rightExpectedY = tank.physics.y + (tank.physics.trackWidth / 2f) * MathUtils.sinDeg(tank.physics.rotation);
            float pY = tank.physics.probeHeightOffset;

            float lX = tank.physics.x - tank.physics.trackWidth / 2f;
            float cX = tank.physics.x;
            float rX = tank.physics.x + tank.physics.trackWidth / 2f;

            // Левый, Центральный и Правый лучи
            shapeRenderer.line(lX, leftExpectedY + pY, lX, leftExpectedY + pY - tank.physics.probeLength);
            shapeRenderer.line(cX, tank.physics.y + pY, cX, tank.physics.y + pY - tank.physics.probeLength);
            shapeRenderer.line(rX, rightExpectedY + pY, rX, rightExpectedY + pY - tank.physics.probeLength);
        }
        shapeRenderer.end();

        // === 2. ОТРИСОВКА ТАНКА (Спрайты с матрицами) ===
        batch.setProjectionMatrix(camera.combined);
        batch.begin();
        tank.render(batch);
        batch.end();

        // === 3. ОТРИСОВКА ТЕКСТА (UI) ===
        batch.getProjectionMatrix().setToOrtho2D(0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        batch.begin();
        font.draw(batch, "State: " + tank.physics.state, 10, Gdx.graphics.getHeight() - 10);
        font.draw(batch, "Rotation: " + String.format("%.1f", tank.physics.rotation), 10, Gdx.graphics.getHeight() - 30);
        font.draw(batch, "Speed: " + String.format("%.1f", tank.physics.velocity.len()), 10, Gdx.graphics.getHeight() - 50);
        font.draw(batch, "Controls: A/D - Move, Mouse - Aim", 10, Gdx.graphics.getHeight() - 70);
        batch.end();
    }

    @Override
    public void resize(int width, int height) {
        camera.viewportWidth = width;
        camera.viewportHeight = height;
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
        shapeRenderer.dispose();
        font.dispose();
        tank.dispose();
    }
}
