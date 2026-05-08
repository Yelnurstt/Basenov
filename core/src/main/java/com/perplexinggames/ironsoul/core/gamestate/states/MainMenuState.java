package com.perplexinggames.ironsoul.core.gamestate.states;

import com.perplexinggames.ironsoul.core.gamestate.AbstractGameState;
import com.perplexinggames.ironsoul.core.gamestate.GameStateType;
import com.perplexinggames.ironsoul.input.state.InputMode;

public class MainMenuState extends AbstractGameState {
    public MainMenuState(InputMode inputMode) {
        super(GameStateType.MAIN_MENU, "Main Menu", inputMode, false);
    }
}
