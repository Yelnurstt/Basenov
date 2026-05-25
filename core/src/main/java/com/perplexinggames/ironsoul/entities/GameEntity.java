package com.perplexinggames.ironsoul.entities;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.perplexinggames.ironsoul.entities.components.HealthComponent;
public abstract class GameEntity {

    protected float x;
    protected float y;

    protected HealthComponent healthComponent;

    public GameEntity(float x, float y, float maxHealth) {
        this.x = x;
        this.y = y;
        this.healthComponent = new HealthComponent(maxHealth);
    }

    public abstract void update(float delta);

    public abstract void render(SpriteBatch batch);

    public void takeDamage(float amount) {
        healthComponent.takeDamage(amount);
    }

    public boolean isDead() {
        return healthComponent.isDead();
    }

    public float getHealth() {
        return healthComponent.getHealth();
    }

    public float getMaxHealth() {
        return healthComponent.getMaxHealth();
    }

    public float getX() {
        return x;
    }

    public float getY() {
        return y;
    }
}
