package com.perplexinggames.ironsoul.entities.components;

public class AttackComponent {

    private final float damage;

    public AttackComponent(float damage) {
        this.damage = damage;
    }

    public float getDamage() {
        return damage;
    }
}
