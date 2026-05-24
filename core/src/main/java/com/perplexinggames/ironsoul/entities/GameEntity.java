package com.perplexinggames.ironsoul.entities;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;

public abstract class GameEntity {

    protected float x;
    protected float y;

    protected float health;
    protected float maxHealth;

    public GameEntity(float x, float y, float maxHealth) {
        this.x = x;
        this.y = y;
        this.maxHealth = maxHealth;
        this.health = maxHealth;
    }

    public abstract void update(float delta);

    public abstract void render(SpriteBatch batch);

    public void takeDamage(float amount) {
        health -= amount;

        if (health < 0f) {
            health = 0f;
        }
    }

    public boolean isDead() {
        return health <= 0f;
    }

    public float getHealth() {
        return health;
    }

    public float getMaxHealth() {
        return maxHealth;
    }

    public float getX() {
        return x;
    }

    public float getY() {
        return y;
    }
}
