package com.perplexinggames.ironsoul.entities.components;

public class AIComponent {

    public enum AIState {
        IDLE,
        PATROL,
        ATTACK,
        DEAD
    }

    private AIState state = AIState.IDLE;

    public AIState getState() {
        return state;
    }

    public void setState(AIState state) {
        this.state = state;
    }

    public boolean isDead() {
        return state == AIState.DEAD;
    }

    public boolean canAttack() {
        return state != AIState.DEAD;
    }
}
