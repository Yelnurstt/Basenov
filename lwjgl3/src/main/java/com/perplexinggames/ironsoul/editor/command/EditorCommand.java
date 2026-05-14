package com.perplexinggames.ironsoul.editor.command;

public interface EditorCommand {
    String getName();

    void execute();

    default void undo() {
    }

    default boolean canUndo() {
        return false;
    }

    boolean didChangeState();
}
