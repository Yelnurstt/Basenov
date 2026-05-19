package com.perplexinggames.ironsoul.editor.tool;

import com.perplexinggames.ironsoul.editor.LevelEditor;

public interface EditorToolStrategy {
    String getName();

    void onMouseDown(LevelEditor levelEditor, int gridX, int gridY, int button);

    void onMouseDrag(LevelEditor levelEditor, int gridX, int gridY);

    void onMouseUp(LevelEditor levelEditor, int gridX, int gridY, int button);

    void onMouseMove(LevelEditor levelEditor, int gridX, int gridY);
}
