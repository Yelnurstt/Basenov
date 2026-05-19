package com.perplexinggames.ironsoul.editor.command;

import com.perplexinggames.ironsoul.editor.LevelEditor;

public class SaveLevelCommand implements EditorCommand {
    private final LevelEditor levelEditor;

    public SaveLevelCommand(LevelEditor levelEditor) {
        this.levelEditor = levelEditor;
    }

    @Override
    public String getName() {
        return "SAVE_LEVEL";
    }

    @Override
    public void execute() {
        levelEditor.saveLevelToDefaultLocation();
    }

    @Override
    public boolean didChangeState() {
        return false;
    }
}
