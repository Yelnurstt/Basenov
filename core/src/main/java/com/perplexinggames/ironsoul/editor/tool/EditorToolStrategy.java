package com.perplexinggames.ironsoul.editor.tool;

import com.perplexinggames.ironsoul.editor.LevelEditor;

public interface EditorToolStrategy {
    String getName();

    void onMouseDown(LevelEditor levelEditor, int gridX, int gridY, float worldX, float worldY, int button);

    void onMouseDrag(LevelEditor levelEditor, int gridX, int gridY, float worldX, float worldY);

    void onMouseUp(LevelEditor levelEditor, int gridX, int gridY, float worldX, float worldY, int button);

    void onMouseMove(LevelEditor levelEditor, int gridX, int gridY, float worldX, float worldY);

    default boolean usesContinuousWorldDrag() {
        return false;
    }
}
