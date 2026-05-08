package com.perplexinggames.ironsoul.core.gamestate.states;

import com.perplexinggames.ironsoul.core.gamestate.AbstractGameState;
import com.perplexinggames.ironsoul.core.gamestate.GameStateType;
import com.perplexinggames.ironsoul.input.state.InputMode;

public class PauseState extends AbstractGameState {
    public PauseState(InputMode inputMode) {
        super(GameStateType.PAUSE, "Pause", inputMode, false);
    }
}
