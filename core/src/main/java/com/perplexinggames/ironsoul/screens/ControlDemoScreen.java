package com.perplexinggames.ironsoul.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Vector3;
import com.perplexinggames.ironsoul.entities.PhysicsTank;
import com.perplexinggames.ironsoul.terrain.SegmentTerrainCollisionProvider;
import com.perplexinggames.ironsoul.terrain.TerrainCollisionBuilder;
import com.perplexinggames.ironsoul.terrain.TerrainCollisionData;
import com.perplexinggames.ironsoul.terrain.TerrainPath;
import com.perplexinggames.ironsoul.terrain.TerrainPoint;
import com.perplexinggames.ironsoul.terrain.TerrainSegment;

import java.util.Collections;

public class ControlDemoScreen implements Screen {
    private SpriteBatch batch;
    private ShapeRenderer shapeRenderer;
    private BitmapFont font;
    private OrthographicCamera camera;

    private TerrainPath terrainPath;
    private TerrainCollisionData terrainCollisionData;
    private PhysicsTank tank;
    private boolean debugMode = true;

    @Override
    public void show() {
        batch = new SpriteBatch();
        shapeRenderer = new ShapeRenderer();
        font = new BitmapFont();
        camera = new OrthographicCamera(1280, 720);

        terrainPath = createLinearTerrainPath();
        terrainCollisionData = new TerrainCollisionBuilder().build(terrainPath);
        tank = new PhysicsTank(0f, 200f, new SegmentTerrainCollisionProvider(Collections.singletonList(terrainCollisionData)));
    }

    @Override
    public void render(float delta) {
        Vector3 mousePos = new Vector3(Gdx.input.getX(), Gdx.input.getY(), 0f);
        camera.unproject(mousePos);
        tank.setAimTarget(mousePos.x, mousePos.y);

        tank.update(delta);

        camera.position.x += (tank.physics.x - camera.position.x) * 5f * delta;
        camera.position.y += ((tank.physics.y + 150f) - camera.position.y) * 5f * delta;
        camera.update();

        Gdx.gl.glClearColor(0.15f, 0.15f, 0.2f, 1f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        shapeRenderer.setProjectionMatrix(camera.combined);
        shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
        shapeRenderer.setColor(Color.LIME);
        for (TerrainSegment segment : terrainPath.getSegments()) {
            shapeRenderer.line(segment.getP1().x, segment.getP1().y, segment.getP2().x, segment.getP2().y);
        }

        if (debugMode) {
            shapeRenderer.setColor(Color.RED);
            float probeY = tank.physics.y + tank.physics.probeHeightOffset;
            float leftX = tank.physics.x - tank.physics.trackWidth / 2f;
            float rightX = tank.physics.x + tank.physics.trackWidth / 2f;
            shapeRenderer.line(leftX, probeY, leftX, probeY - tank.physics.probeLength);
            shapeRenderer.line(rightX, probeY, rightX, probeY - tank.physics.probeLength);

            shapeRenderer.setColor(Color.CYAN);
            if (tank.physics.leftContact.hasContact) {
                shapeRenderer.circle(tank.physics.leftContact.point.x, tank.physics.leftContact.point.y, 4f);
                shapeRenderer.line(
                    tank.physics.leftContact.point,
                    tank.physics.leftContact.point.cpy().add(tank.physics.leftContact.normal.cpy().scl(25f))
                );
            }
            if (tank.physics.rightContact.hasContact) {
                shapeRenderer.circle(tank.physics.rightContact.point.x, tank.physics.rightContact.point.y, 4f);
                shapeRenderer.line(
                    tank.physics.rightContact.point,
                    tank.physics.rightContact.point.cpy().add(tank.physics.rightContact.normal.cpy().scl(25f))
                );
            }
        }
        shapeRenderer.end();

        batch.setProjectionMatrix(camera.combined);
        batch.begin();
        tank.render(batch);
        batch.end();

        batch.getProjectionMatrix().setToOrtho2D(0f, 0f, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        batch.begin();
        font.draw(batch, "State: " + tank.physics.state, 10f, Gdx.graphics.getHeight() - 10f);
        font.draw(batch, "Rotation: " + String.format("%.1f", tank.physics.rotation), 10f, Gdx.graphics.getHeight() - 30f);
        font.draw(batch, "Speed: " + String.format("%.1f", tank.physics.velocity.len()), 10f, Gdx.graphics.getHeight() - 50f);
        font.draw(batch, "Controls: A/D - Move, Mouse - Aim", 10f, Gdx.graphics.getHeight() - 70f);
        batch.end();
    }

    private TerrainPath createLinearTerrainPath() {
        return new TerrainPath(
            "control-demo-terrain",
            java.util.Arrays.asList(
                new TerrainPoint("p0", -500f, 100f),
                new TerrainPoint("p1", 300f, 100f),
                new TerrainPoint("p2", 600f, 300f),
                new TerrainPoint("p3", 900f, 300f),
                new TerrainPoint("p4", 1100f, 100f),
                new TerrainPoint("p5", 1250f, 100f),
                new TerrainPoint("p6", 1500f, 400f),
                new TerrainPoint("p7", 2000f, 400f)
            ),
            TerrainPath.CurveType.LINEAR,
            "demo-dirt",
            4f,
            1f
        );
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
