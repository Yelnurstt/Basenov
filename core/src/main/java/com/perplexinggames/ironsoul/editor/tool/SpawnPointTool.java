package com.perplexinggames.ironsoul.editor.tool;

import com.badlogic.gdx.Input;
import com.perplexinggames.ironsoul.editor.LevelEditor;

public class SpawnPointTool implements EditorToolStrategy {
    @Override
    public String getName() {
        return "SPAWN_POINT";
    }

    @Override
    public void onMouseDown(LevelEditor levelEditor, int gridX, int gridY, float worldX, float worldY, int button) {
        if (button == Input.Buttons.LEFT) {
            levelEditor.placeSpawnPoint(worldX, worldY);
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
