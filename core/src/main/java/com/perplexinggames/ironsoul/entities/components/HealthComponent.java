package com.perplexinggames.ironsoul.entities.components;

public class HealthComponent {

    private float health;
    private final float maxHealth;

    public HealthComponent(float maxHealth) {
        this.maxHealth = maxHealth;
        this.health = maxHealth;
    }

    public void takeDamage(float amount) {
        health -= amount;

        if (health < 0f) {
            health = 0f;
        }
    }

    public void heal(float amount) {
        health += amount;

        if (health > maxHealth) {
            health = maxHealth;
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
}
