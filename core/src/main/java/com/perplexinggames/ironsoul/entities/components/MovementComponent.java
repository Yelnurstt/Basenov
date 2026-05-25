package com.perplexinggames.ironsoul.entities.components;

public class MovementComponent {

    private float speed;

    public MovementComponent(float speed) {
        this.speed = speed;
    }

    public float getSpeed() {
        return speed;
    }

    public void setSpeed(float speed) {
        this.speed = speed;
    }
}
