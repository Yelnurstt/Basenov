package com.perplexinggames.ironsoul.editor.ui;

import com.perplexinggames.ironsoul.editor.EditorMode;
import com.perplexinggames.ironsoul.editor.tool.EditorToolStrategy;

public interface EditorToolController {
    void setMode(EditorMode mode);
    EditorMode getMode();
    void setTool(EditorToolStrategy toolStrategy);
    String getCurrentToolName();
}
