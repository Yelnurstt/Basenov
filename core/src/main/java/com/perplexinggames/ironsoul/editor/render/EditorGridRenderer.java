package com.perplexinggames.ironsoul.editor.render;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.perplexinggames.ironsoul.editor.LevelEditor;
import com.perplexinggames.ironsoul.world.WorldBlockData;

public class EditorGridRenderer {
    private final ShapeRenderer shapeRenderer;

    public EditorGridRenderer() {
        this.shapeRenderer = new ShapeRenderer();
    }

    public void render(LevelEditor levelEditor, OrthographicCamera camera) {
        if (!levelEditor.getSnapService().isEnabled()) {
            return;
        }

        WorldBlockData activeBlock = levelEditor.getActiveBlock();
        if (activeBlock == null) {
            return;
        }

        float snapSize = levelEditor.getSnapService().getSnapSize();
        if (snapSize <= 0) return;

        float blockWidth = activeBlock.width * levelEditor.getWorldData().tileSize;
        float blockHeight = activeBlock.height * levelEditor.getWorldData().tileSize;

        shapeRenderer.setProjectionMatrix(camera.combined);
        shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
        shapeRenderer.setColor(new Color(1f, 1f, 1f, 0.15f)); // Light translucent white

        // Draw vertical lines
        for (float x = 0; x <= blockWidth; x += snapSize) {
            shapeRenderer.line(x, 0, x, blockHeight);
        }

        // Draw horizontal lines
        for (float y = 0; y <= blockHeight; y += snapSize) {
            shapeRenderer.line(0, y, blockWidth, y);
        }

        shapeRenderer.end();
    }

    public void dispose() {
        shapeRenderer.dispose();
    }
}
