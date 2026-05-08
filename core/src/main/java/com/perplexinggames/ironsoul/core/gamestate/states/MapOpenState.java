package com.perplexinggames.ironsoul.core.gamestate.states;

import com.perplexinggames.ironsoul.core.gamestate.AbstractGameState;
import com.perplexinggames.ironsoul.core.gamestate.GameStateType;
import com.perplexinggames.ironsoul.input.state.InputMode;

public class MapOpenState extends AbstractGameState {
    public MapOpenState(InputMode inputMode) {
        super(GameStateType.MAP_OPEN, "Map Open", inputMode, false);
    }
}
