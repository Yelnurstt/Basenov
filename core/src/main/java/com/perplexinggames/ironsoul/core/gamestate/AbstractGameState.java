package com.perplexinggames.ironsoul.core.gamestate;

import com.perplexinggames.ironsoul.input.state.InputMode;

public abstract class AbstractGameState implements GameState {
    private final GameStateType type;
    private final String name;
    private final InputMode inputMode;
    private final boolean updatesSimulation;

    protected AbstractGameState(GameStateType type, String name, InputMode inputMode, boolean updatesSimulation) {
        this.type = type;
        this.name = name;
        this.inputMode = inputMode;
        this.updatesSimulation = updatesSimulation;
    }

    @Override
    public GameStateType getType() {
        return type;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public InputMode getInputMode() {
        return inputMode;
    }

    @Override
    public boolean updatesSimulation() {
        return updatesSimulation;
    }
}
