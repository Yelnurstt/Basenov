package com.perplexinggames.ironsoul.input.command.commands;

import com.perplexinggames.ironsoul.core.gameplay.DialogueSystem;
import com.perplexinggames.ironsoul.core.gamestate.GameStateManager;
import com.perplexinggames.ironsoul.core.gamestate.GameStateType;
import com.perplexinggames.ironsoul.input.command.Command;

public class AdvanceDialogueCommand implements Command {
    private final DialogueSystem dialogueSystem;
    private final GameStateManager gameStateManager;

    public AdvanceDialogueCommand(DialogueSystem dialogueSystem, GameStateManager gameStateManager) {
        this.dialogueSystem = dialogueSystem;
        this.gameStateManager = gameStateManager;
    }

    @Override
    public void execute() {
        boolean finished = dialogueSystem.advance();
        if (finished) {
            gameStateManager.changeState(GameStateType.PLAYING);
        }
    }
}
