package com.perplexinggames.ironsoul.editor.command;

import com.badlogic.gdx.utils.Array;

public class EditorCommandHistory {
    private final Array<EditorCommand> undoStack;
    private final Array<EditorCommand> redoStack;

    public EditorCommandHistory() {
        this.undoStack = new Array<>();
        this.redoStack = new Array<>();
    }

    public void execute(EditorCommand command) {
        command.execute();
        if (!command.didChangeState()) {
            return;
        }

        redoStack.clear();
        if (command.canUndo()) {
            undoStack.add(command);
        }
    }

    public String undo() {
        if (undoStack.size == 0) {
            return null;
        }

        EditorCommand command = undoStack.pop();
        command.undo();
        if (command.canUndo()) {
            redoStack.add(command);
        }
        return command.getName();
    }

    public String redo() {
        if (redoStack.size == 0) {
            return null;
        }

        EditorCommand command = redoStack.pop();
        command.execute();
        if (command.didChangeState() && command.canUndo()) {
            undoStack.add(command);
        }
        return command.getName();
    }

    public void clear() {
        undoStack.clear();
        redoStack.clear();
    }
}
