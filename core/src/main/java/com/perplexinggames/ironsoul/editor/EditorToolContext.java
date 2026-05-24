package com.perplexinggames.ironsoul.editor;

import com.perplexinggames.ironsoul.editor.tool.EditorToolStrategy;

public class EditorToolContext {
    private EditorToolStrategy activeTool;

    public EditorToolContext(EditorToolStrategy activeTool) {
        this.activeTool = activeTool;
    }

    public void setActiveTool(EditorToolStrategy activeTool) {
        this.activeTool = activeTool;
    }

    public EditorToolStrategy getActiveTool() {
        return activeTool;
    }

    public void onMouseDown(LevelEditor levelEditor, int gridX, int gridY, float worldX, float worldY, int button) {
        activeTool.onMouseDown(levelEditor, gridX, gridY, worldX, worldY, button);
    }

    public void onMouseDrag(LevelEditor levelEditor, int gridX, int gridY, float worldX, float worldY) {
        activeTool.onMouseDrag(levelEditor, gridX, gridY, worldX, worldY);
    }

    public void onMouseUp(LevelEditor levelEditor, int gridX, int gridY, float worldX, float worldY, int button) {
        activeTool.onMouseUp(levelEditor, gridX, gridY, worldX, worldY, button);
    }

    public void onMouseMove(LevelEditor levelEditor, int gridX, int gridY, float worldX, float worldY) {
        activeTool.onMouseMove(levelEditor, gridX, gridY, worldX, worldY);
    }

    public boolean usesContinuousWorldDrag() {
        return activeTool.usesContinuousWorldDrag();
    }
}
