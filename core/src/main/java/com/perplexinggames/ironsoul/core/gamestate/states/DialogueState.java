package com.perplexinggames.ironsoul.core.gamestate.states;

import com.perplexinggames.ironsoul.core.gamestate.AbstractGameState;
import com.perplexinggames.ironsoul.core.gamestate.GameStateType;
import com.perplexinggames.ironsoul.input.state.InputMode;

public class DialogueState extends AbstractGameState {
    public DialogueState(InputMode inputMode) {
        super(GameStateType.DIALOGUE, "Dialogue", inputMode, false);
    }
}
