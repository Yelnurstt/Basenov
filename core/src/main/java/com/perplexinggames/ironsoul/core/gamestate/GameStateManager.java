package com.perplexinggames.ironsoul.core.gamestate;

import java.util.EnumMap;
import java.util.Objects;

public class GameStateManager {
    private final EnumMap<GameStateType, GameState> states;
    private GameState currentState;
    private GameState resumeState;

    public GameStateManager(EnumMap<GameStateType, GameState> states, GameStateType initialState) {
        this.states = new EnumMap<>(Objects.requireNonNull(states, "states"));
        this.currentState = requireState(initialState);
        this.resumeState = currentState;
    }

    public GameState getCurrentState() {
        return currentState;
    }

    public void changeState(GameStateType type) {
        currentState = requireState(type);
        if (type != GameStateType.PAUSED) {
            resumeState = currentState;
        }
    }

    public void togglePause() {
        if (currentState.getType() == GameStateType.PAUSED) {
            currentState = resumeState;
            return;
        }

        resumeState = currentState;
        currentState = requireState(GameStateType.PAUSED);
    }

    public boolean isPaused() {
        return currentState.getType() == GameStateType.PAUSED;
    }

    public boolean isState(GameStateType type) {
        return currentState.getType() == type;
    }

    public void toggleState(GameStateType targetState, GameStateType fallbackState) {
        if (currentState.getType() == targetState) {
            changeState(fallbackState);
            return;
        }
        changeState(targetState);
    }

    private GameState requireState(GameStateType type) {
        GameState state = states.get(type);
        if (state == null) {
            throw new IllegalStateException("Missing state registration for " + type);
        }
        return state;
    }
}
