package com.perplexinggames.ironsoul.core.gamestate.states;

import com.perplexinggames.ironsoul.core.gamestate.AbstractGameState;
import com.perplexinggames.ironsoul.core.gamestate.GameStateType;
import com.perplexinggames.ironsoul.input.state.InputMode;

public class CutsceneState extends AbstractGameState {
    public CutsceneState(InputMode inputMode) {
        super(GameStateType.CUTSCENE, "Cutscene", inputMode, false);
    }
}
