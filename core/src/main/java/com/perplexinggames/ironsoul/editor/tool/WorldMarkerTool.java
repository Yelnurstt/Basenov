package com.perplexinggames.ironsoul.editor.tool;

import com.badlogic.gdx.Input;
import com.perplexinggames.ironsoul.editor.LevelEditor;

public class WorldMarkerTool implements EditorToolStrategy {
    private final String name;
    private final LevelEditor.MarkerLayer layer;

    public WorldMarkerTool(String name, LevelEditor.MarkerLayer layer) {
        this.name = name;
        this.layer = layer;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public void onMouseDown(LevelEditor levelEditor, int gridX, int gridY, float worldX, float worldY, int button) {
        if (button == Input.Buttons.LEFT) {
            levelEditor.placeMarker(layer, worldX, worldY);
        }
    }

    @Override
    public void onMouseDrag(LevelEditor levelEditor, int gridX, int gridY, float worldX, float worldY) {
    }

    @Override
    public void onMouseUp(LevelEditor levelEditor, int gridX, int gridY, float worldX, float worldY, int button) {
    }

    @Override
    public void onMouseMove(LevelEditor levelEditor, int gridX, int gridY, float worldX, float worldY) {
    }
}
