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

        terrainPath = new TerrainPath();
        terrainPath.addSegment(-500, 100, 300, 100);
        terrainPath.addSegment(300, 100, 600, 300);
        terrainPath.addSegment(600, 300, 900, 300);
        terrainPath.addSegment(900, 300, 1100, 100);
        terrainPath.addSegment(1250, 100, 1500, 400);
        terrainPath.addSegment(1500, 400, 2000, 400);

        SegmentTerrainCollisionProvider collisionProvider = new SegmentTerrainCollisionProvider(terrainPath);
        tank = new PhysicsTank(0, 200, collisionProvider);
    }

    @Override
    public void render(float delta) {
        Vector3 mousePos = new Vector3(Gdx.input.getX(), Gdx.input.getY(), 0);
        camera.unproject(mousePos);
        tank.setAimTarget(mousePos.x, mousePos.y);

        tank.update(delta);

        camera.position.x += (tank.physics.x - camera.position.x) * 5f * delta;
        camera.position.y += ((tank.physics.y + 150f) - camera.position.y) * 5f * delta;
        camera.update();

        Gdx.gl.glClearColor(0.15f, 0.15f, 0.2f, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        shapeRenderer.setProjectionMatrix(camera.combined);
        shapeRenderer.begin(ShapeRenderer.ShapeType.Line);

        shapeRenderer.setColor(Color.LIME);
        for (TerrainSegment seg : terrainPath.getSegments()) {
            shapeRenderer.line(seg.getP1().x, seg.getP1().y, seg.getP2().x, seg.getP2().y);
        }

        if (debugMode) {
            shapeRenderer.setColor(Color.RED);

            float leftExpectedY = tank.physics.y - (tank.physics.trackWidth / 2f) * MathUtils.sinDeg(tank.physics.rotation);
            float rightExpectedY = tank.physics.y + (tank.physics.trackWidth / 2f) * MathUtils.sinDeg(tank.physics.rotation);
            float pY = tank.physics.probeHeightOffset;

            float lX = tank.physics.x - tank.physics.trackWidth / 2f;
            float cX = tank.physics.x;
            float rX = tank.physics.x + tank.physics.trackWidth / 2f;

            // Отрисовка всех трех лучей
            shapeRenderer.line(lX, leftExpectedY + pY, lX, leftExpectedY + pY - tank.physics.probeLength);
            shapeRenderer.line(cX, tank.physics.y + pY, cX, tank.physics.y + pY - tank.physics.probeLength);
            shapeRenderer.line(rX, rightExpectedY + pY, rX, rightExpectedY + pY - tank.physics.probeLength);

            // Точки контакта и нормали
            if (tank.physics.leftContact.hasContact) {
                shapeRenderer.setColor(Color.CYAN);
                shapeRenderer.circle(tank.physics.leftContact.point.x, tank.physics.leftContact.point.y, 4f);
                shapeRenderer.line(tank.physics.leftContact.point, tank.physics.leftContact.point.cpy().add(tank.physics.leftContact.normal.cpy().scl(25f)));
            }
            if (tank.physics.centerContact.hasContact) {
                shapeRenderer.setColor(Color.YELLOW);
                shapeRenderer.circle(tank.physics.centerContact.point.x, tank.physics.centerContact.point.y, 4f);
                shapeRenderer.line(tank.physics.centerContact.point, tank.physics.centerContact.point.cpy().add(tank.physics.centerContact.normal.cpy().scl(25f)));
            }
            if (tank.physics.rightContact.hasContact) {
                shapeRenderer.setColor(Color.CYAN);
                shapeRenderer.circle(tank.physics.rightContact.point.x, tank.physics.rightContact.point.y, 4f);
                shapeRenderer.line(tank.physics.rightContact.point, tank.physics.rightContact.point.cpy().add(tank.physics.rightContact.normal.cpy().scl(25f)));
            }
        }
        shapeRenderer.end();

        batch.setProjectionMatrix(camera.combined);
        batch.begin();
        tank.render(batch);
        batch.end();

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

    @Override public void pause() {}
    @Override public void resume() {}
    @Override public void hide() {}

    @Override
    public void dispose() {
        batch.dispose();
        shapeRenderer.dispose();
        font.dispose();
        tank.dispose();
    }
}
