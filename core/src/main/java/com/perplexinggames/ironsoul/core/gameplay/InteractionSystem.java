package com.perplexinggames.ironsoul.core.gameplay;

import com.perplexinggames.ironsoul.core.gamestate.GameStateManager;
import com.perplexinggames.ironsoul.core.gamestate.GameStateType;

import java.util.List;

public class InteractionSystem {
    private final DialogueSystem dialogueSystem;
    private final GameStateManager gameStateManager;
    private int interactionCount;
    private String lastInteraction = "Idle";

    public InteractionSystem(DialogueSystem dialogueSystem, GameStateManager gameStateManager) {
        this.dialogueSystem = dialogueSystem;
        this.gameStateManager = gameStateManager;
    }

    public void interact() {
        interactionCount++;
        lastInteraction = "Maintenance terminal engaged";

        if (!dialogueSystem.isActive()) {
            dialogueSystem.startConversation(List.of(
                "Pilot, hull mass confirmed. Expect delayed braking.",
                "Servo pressure is stable. Dash window recalibrated.",
                "Field note: heavy tanks win by commitment, not twitching."
            ));
            gameStateManager.changeState(GameStateType.DIALOGUE);
        }
    }

    public int getInteractionCount() {
        return interactionCount;
    }

    public String getLastInteraction() {
        return lastInteraction;
    }
}
