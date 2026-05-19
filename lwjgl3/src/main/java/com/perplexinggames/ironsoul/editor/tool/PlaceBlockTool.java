package com.perplexinggames.ironsoul.editor.tool;

import com.badlogic.gdx.Input;
import com.perplexinggames.ironsoul.editor.LevelEditor;
import com.perplexinggames.ironsoul.editor.command.PlaceBlockCommand;
import com.perplexinggames.ironsoul.level.BlockType;

public class PlaceBlockTool implements EditorToolStrategy {
    @Override
    public String getName() {
        return "PLACE_BLOCK";
    }

    @Override
    public void onMouseDown(LevelEditor levelEditor, int gridX, int gridY, int button) {
        if (button == Input.Buttons.LEFT) {
            levelEditor.executeCommand(new PlaceBlockCommand(levelEditor, gridX, gridY, BlockType.SOLID));
        }
    }

    @Override
    public void onMouseDrag(LevelEditor levelEditor, int gridX, int gridY) {
        levelEditor.executeCommand(new PlaceBlockCommand(levelEditor, gridX, gridY, BlockType.SOLID));
    }

    @Override
    public void onMouseUp(LevelEditor levelEditor, int gridX, int gridY, int button) {
    }

    @Override
    public void onMouseMove(LevelEditor levelEditor, int gridX, int gridY) {
    }
}
