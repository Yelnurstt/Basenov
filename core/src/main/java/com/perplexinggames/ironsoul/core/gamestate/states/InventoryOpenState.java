package com.perplexinggames.ironsoul.core.gamestate.states;

import com.perplexinggames.ironsoul.core.gamestate.AbstractGameState;
import com.perplexinggames.ironsoul.core.gamestate.GameStateType;
import com.perplexinggames.ironsoul.input.state.InputMode;

public class InventoryOpenState extends AbstractGameState {
    public InventoryOpenState(InputMode inputMode) {
        super(GameStateType.INVENTORY_OPEN, "Inventory Open", inputMode, false);
    }
}
