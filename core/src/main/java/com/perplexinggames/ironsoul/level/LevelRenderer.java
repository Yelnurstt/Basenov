package com.perplexinggames.ironsoul.level;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.GridPoint2;
import com.badlogic.gdx.math.MathUtils;
import com.perplexinggames.ironsoul.terrain.TerrainCollisionBuilder;
import com.perplexinggames.ironsoul.terrain.TerrainCollisionData;
import com.perplexinggames.ironsoul.terrain.TerrainPath;
import com.perplexinggames.ironsoul.terrain.TerrainPoint;
import com.perplexinggames.ironsoul.terrain.TerrainSegment;
import com.perplexinggames.ironsoul.terrain.spline.SplineTerrainRenderer;
import com.perplexinggames.ironsoul.world.GateData;
import com.perplexinggames.ironsoul.world.SpawnPointData;
import com.perplexinggames.ironsoul.world.WorldBlockData;
import com.perplexinggames.ironsoul.world.WorldElementData;

public class LevelRenderer {
    private final SpriteBatch spriteBatch;
    private final ShapeRenderer shapeRenderer;
    private final TerrainCollisionBuilder terrainCollisionBuilder;
    private final SplineTerrainRenderer splineTerrainRenderer;
    private final Color blockColor;
    private final Color gridColor;
    private final Color hoveredColor;
    private final Color selectedColor;
    private final Color terrainSegmentColor;
    private final Color terrainPointColor;
    private final Color selectedTerrainPointColor;
    private final Color gateOutlineColor;
    private final Color spawnPointColor;
    private final Color objectColor;
    private final Color enemyColor;
    private final Color rewardColor;
    private final Color triggerColor;
    private final Color blockBoundsColor;

    public LevelRenderer() {
        this.spriteBatch = new SpriteBatch();
        this.shapeRenderer = new ShapeRenderer();
        this.terrainCollisionBuilder = new TerrainCollisionBuilder();
        this.splineTerrainRenderer = new SplineTerrainRenderer(24f);
        this.blockColor = new Color(0.24f, 0.27f, 0.30f, 1f);
        this.gridColor = new Color(0.38f, 0.44f, 0.48f, 0.65f);
        this.hoveredColor = new Color(1f, 0.84f, 0.2f, 1f);
        this.selectedColor = new Color(0.2f, 0.92f, 1f, 1f);
        this.terrainSegmentColor = new Color(0.38f, 0.82f, 0.52f, 1f);
        this.terrainPointColor = new Color(0.96f, 0.76f, 0.2f, 1f);
        this.selectedTerrainPointColor = new Color(1f, 0.4f, 0.25f, 1f);
        this.gateOutlineColor = new Color(1f, 0.65f, 0.2f, 1f);
        this.spawnPointColor = new Color(0.2f, 0.85f, 1f, 1f);
        this.objectColor = new Color(0.72f, 0.84f, 1f, 1f);
        this.enemyColor = new Color(1f, 0.4f, 0.4f, 1f);
        this.rewardColor = new Color(1f, 0.9f, 0.2f, 1f);
        this.triggerColor = new Color(0.45f, 1f, 0.55f, 1f);
        this.blockBoundsColor = new Color(0.75f, 0.82f, 0.96f, 0.9f);
    }

    public void renderGameplay(RuntimeLevel runtimeLevel, WorldBlockData activeBlock, OrthographicCamera camera) {
        spriteBatch.setProjectionMatrix(camera.combined);
        spriteBatch.begin();
        splineTerrainRenderer.renderBackgroundAndMain(spriteBatch, camera, runtimeLevel);
        spriteBatch.end();

        shapeRenderer.setProjectionMatrix(camera.combined);
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        drawBlocks(runtimeLevel);
        drawTerrainSegments(runtimeLevel);
        drawSpawnPoints(activeBlock);
        drawMarkers(activeBlock);
        shapeRenderer.end();

        shapeRenderer.setProjectionMatrix(camera.combined);
        shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
        drawBlockBounds(runtimeLevel);
        drawGates(activeBlock);
        shapeRenderer.end();

        spriteBatch.setProjectionMatrix(camera.combined);
        spriteBatch.begin();
        splineTerrainRenderer.renderMain(spriteBatch, camera, runtimeLevel);
        spriteBatch.end();
    }

    public void renderGameplayForeground(RuntimeLevel runtimeLevel, OrthographicCamera camera) {
        spriteBatch.setProjectionMatrix(camera.combined);
        spriteBatch.begin();
        splineTerrainRenderer.renderForeground(spriteBatch, camera, runtimeLevel);
        spriteBatch.end();
    }

    public void renderEditor(RuntimeLevel runtimeLevel, WorldBlockData activeBlock, OrthographicCamera camera, GridPoint2 hoveredCell,
                             GridPoint2 selectedCell, TerrainPoint selectedTerrainPoint,
                             String selectedSplinePathId, String selectedSplinePointId) {
        renderGameplay(runtimeLevel, activeBlock, camera);

        shapeRenderer.setProjectionMatrix(camera.combined);
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        if (runtimeLevel.getSplinePaths().isEmpty()) {
            drawTerrainPoints(runtimeLevel, selectedTerrainPoint);
        }
        shapeRenderer.end();

        if (!runtimeLevel.getSplinePaths().isEmpty()) {
            splineTerrainRenderer.renderDebug(shapeRenderer, camera, runtimeLevel, selectedSplinePathId, selectedSplinePointId);
        }

        shapeRenderer.setProjectionMatrix(camera.combined);
        shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
        drawGrid(runtimeLevel, camera);
        drawCellOutline(runtimeLevel, hoveredCell, hoveredColor);
        drawCellOutline(runtimeLevel, selectedCell, selectedColor);
        shapeRenderer.end();
    }

    public void dispose() {
        spriteBatch.dispose();
        shapeRenderer.dispose();
        splineTerrainRenderer.dispose();
    }

    private void drawBlocks(RuntimeLevel runtimeLevel) {
        int tileSize = runtimeLevel.getTileSize();
        shapeRenderer.setColor(blockColor);
        for (BlockData block : runtimeLevel.getBlocks()) {
            shapeRenderer.rect(block.x * tileSize, block.y * tileSize, tileSize, tileSize);
        }
    }

    private void drawTerrainSegments(RuntimeLevel runtimeLevel) {
        shapeRenderer.setColor(terrainSegmentColor);
        for (TerrainPath terrainPath : runtimeLevel.getTerrainPaths()) {
            TerrainCollisionData collisionData = terrainCollisionBuilder.build(terrainPath);
            for (TerrainSegment segment : collisionData.getSegments()) {
                shapeRenderer.rectLine(
                    segment.getStartPoint().getX(),
                    segment.getStartPoint().getY(),
                    segment.getEndPoint().getX(),
                    segment.getEndPoint().getY(),
                    Math.max(2f, terrainPath.getDebugWidth())
                );
            }
        }
    }

    private void drawTerrainPoints(RuntimeLevel runtimeLevel, TerrainPoint selectedTerrainPoint) {
        for (TerrainPath terrainPath : runtimeLevel.getTerrainPaths()) {
            for (TerrainPoint point : terrainPath.getPoints()) {
                boolean selected = selectedTerrainPoint != null && point.getId().equals(selectedTerrainPoint.getId());
                shapeRenderer.setColor(selected ? selectedTerrainPointColor : terrainPointColor);
                shapeRenderer.circle(point.getX(), point.getY(), selected ? 7f : 5f, 16);
            }
        }
    }

    private void drawGrid(RuntimeLevel runtimeLevel, OrthographicCamera camera) {
        int tileSize = runtimeLevel.getTileSize();
        float halfWidth = camera.viewportWidth * camera.zoom * 0.5f;
        float halfHeight = camera.viewportHeight * camera.zoom * 0.5f;

        int startX = Math.max(0, MathUtils.floor((camera.position.x - halfWidth) / tileSize) - 1);
        int endX = Math.min(runtimeLevel.getWidth(), MathUtils.ceil((camera.position.x + halfWidth) / tileSize) + 1);
        int startY = Math.max(0, MathUtils.floor((camera.position.y - halfHeight) / tileSize) - 1);
        int endY = Math.min(runtimeLevel.getHeight(), MathUtils.ceil((camera.position.y + halfHeight) / tileSize) + 1);

        float worldWidth = runtimeLevel.getPixelWidth();
        float worldHeight = runtimeLevel.getPixelHeight();

        shapeRenderer.setColor(gridColor);
        for (int x = startX; x <= endX; x++) {
            float drawX = x * tileSize;
            shapeRenderer.line(drawX, 0, drawX, worldHeight);
        }
        for (int y = startY; y <= endY; y++) {
            float drawY = y * tileSize;
            shapeRenderer.line(0, drawY, worldWidth, drawY);
        }
    }

    private void drawCellOutline(RuntimeLevel runtimeLevel, GridPoint2 cell, Color color) {
        if (cell == null || !runtimeLevel.isInside(cell.x, cell.y)) {
            return;
        }

        int tileSize = runtimeLevel.getTileSize();
        shapeRenderer.setColor(color);
        shapeRenderer.rect(cell.x * tileSize, cell.y * tileSize, tileSize, tileSize);
    }

    private void drawBlockBounds(RuntimeLevel runtimeLevel) {
        shapeRenderer.setColor(blockBoundsColor);
        shapeRenderer.rect(0f, 0f, runtimeLevel.getPixelWidth(), runtimeLevel.getPixelHeight());
    }

    private void drawGates(WorldBlockData activeBlock) {
        if (activeBlock == null) {
            return;
        }
        shapeRenderer.setColor(gateOutlineColor);
        for (GateData gate : activeBlock.gates) {
            if (gate == null || gate.bounds == null) {
                continue;
            }
            shapeRenderer.rect(gate.bounds.x, gate.bounds.y, gate.bounds.width, gate.bounds.height);
        }
    }

    private void drawSpawnPoints(WorldBlockData activeBlock) {
        if (activeBlock == null) {
            return;
        }
        shapeRenderer.setColor(spawnPointColor);
        for (SpawnPointData spawnPoint : activeBlock.spawnPoints) {
            if (spawnPoint == null) {
                continue;
            }
            shapeRenderer.circle(spawnPoint.x, spawnPoint.y, 8f, 16);
        }
    }

    private void drawMarkers(WorldBlockData activeBlock) {
        if (activeBlock == null) {
            return;
        }
        drawMarkerList(activeBlock.objects, objectColor);
        drawMarkerList(activeBlock.enemies, enemyColor);
        drawMarkerList(activeBlock.rewards, rewardColor);
        drawMarkerList(activeBlock.triggers, triggerColor);
    }

    private void drawMarkerList(java.util.List<WorldElementData> markers, Color color) {
        shapeRenderer.setColor(color);
        for (WorldElementData marker : markers) {
            if (marker == null) {
                continue;
            }
            shapeRenderer.rect(marker.x - 5f, marker.y - 5f, 10f, 10f);
        }
    }
}
