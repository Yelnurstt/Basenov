package com.perplexinggames.ironsoul.core.gamestate;

import com.perplexinggames.ironsoul.input.state.InputMode;

public interface GameState {
    GameStateType getType();

    String getName();

    InputMode getInputMode();

    boolean updatesSimulation();
}
