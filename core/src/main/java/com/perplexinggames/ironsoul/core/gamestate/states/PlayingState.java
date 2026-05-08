package com.perplexinggames.ironsoul.core.gamestate.states;

import com.perplexinggames.ironsoul.core.gamestate.AbstractGameState;
import com.perplexinggames.ironsoul.core.gamestate.GameStateType;
import com.perplexinggames.ironsoul.input.state.InputMode;

public class PlayingState extends AbstractGameState {
    public PlayingState(InputMode inputMode) {
        super(GameStateType.PLAYING, "Playing", inputMode, true);
    }
}
