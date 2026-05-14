package com.perplexinggames.ironsoul.level;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.GridPoint2;
import com.badlogic.gdx.math.MathUtils;

public class LevelRenderer {
    private final ShapeRenderer shapeRenderer;
    private final Color blockColor;
    private final Color gridColor;
    private final Color hoveredColor;
    private final Color selectedColor;

    public LevelRenderer() {
        this.shapeRenderer = new ShapeRenderer();
        this.blockColor = new Color(0.24f, 0.27f, 0.30f, 1f);
        this.gridColor = new Color(0.38f, 0.44f, 0.48f, 0.65f);
        this.hoveredColor = new Color(1f, 0.84f, 0.2f, 1f);
        this.selectedColor = new Color(0.2f, 0.92f, 1f, 1f);
    }

    public void renderGameplay(RuntimeLevel runtimeLevel, OrthographicCamera camera) {
        shapeRenderer.setProjectionMatrix(camera.combined);
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        drawBlocks(runtimeLevel);
        shapeRenderer.end();
    }

    public void renderEditor(RuntimeLevel runtimeLevel, OrthographicCamera camera, GridPoint2 hoveredCell,
                             GridPoint2 selectedCell) {
        renderGameplay(runtimeLevel, camera);

        shapeRenderer.setProjectionMatrix(camera.combined);
        shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
        drawGrid(runtimeLevel, camera);
        drawCellOutline(runtimeLevel, hoveredCell, hoveredColor);
        drawCellOutline(runtimeLevel, selectedCell, selectedColor);
        shapeRenderer.end();
    }

    public void dispose() {
        shapeRenderer.dispose();
    }

    private void drawBlocks(RuntimeLevel runtimeLevel) {
        int tileSize = runtimeLevel.getTileSize();
        shapeRenderer.setColor(blockColor);
        for (BlockData block : runtimeLevel.getBlocks()) {
            shapeRenderer.rect(block.x * tileSize, block.y * tileSize, tileSize, tileSize);
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
}
