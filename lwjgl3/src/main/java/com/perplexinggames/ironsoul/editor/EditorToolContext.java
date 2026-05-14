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

    public void onMouseDown(LevelEditor levelEditor, int gridX, int gridY, int button) {
        activeTool.onMouseDown(levelEditor, gridX, gridY, button);
    }

    public void onMouseDrag(LevelEditor levelEditor, int gridX, int gridY) {
        activeTool.onMouseDrag(levelEditor, gridX, gridY);
    }

    public void onMouseUp(LevelEditor levelEditor, int gridX, int gridY, int button) {
        activeTool.onMouseUp(levelEditor, gridX, gridY, button);
    }

    public void onMouseMove(LevelEditor levelEditor, int gridX, int gridY) {
        activeTool.onMouseMove(levelEditor, gridX, gridY);
    }
}
