package com.perplexinggames.ironsoul.editor.tool;

import com.badlogic.gdx.Input;
import com.perplexinggames.ironsoul.editor.LevelEditor;
import com.perplexinggames.ironsoul.editor.command.EraseBlockCommand;

public class EraseBlockTool implements EditorToolStrategy {
    @Override
    public String getName() {
        return "ERASE_BLOCK";
    }

    @Override
    public void onMouseDown(LevelEditor levelEditor, int gridX, int gridY, int button) {
        if (button == Input.Buttons.LEFT) {
            levelEditor.executeCommand(new EraseBlockCommand(levelEditor, gridX, gridY));
        }
    }

    @Override
    public void onMouseDrag(LevelEditor levelEditor, int gridX, int gridY) {
        levelEditor.executeCommand(new EraseBlockCommand(levelEditor, gridX, gridY));
    }

    @Override
    public void onMouseUp(LevelEditor levelEditor, int gridX, int gridY, int button) {
    }

    @Override
    public void onMouseMove(LevelEditor levelEditor, int gridX, int gridY) {
    }
}
