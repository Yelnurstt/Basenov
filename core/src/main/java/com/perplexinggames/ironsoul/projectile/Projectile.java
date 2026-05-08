package com.perplexinggames.ironsoul.projectile;

public class Projectile {
    private float x;
    private float y;
    private final float velocityX;
    private final float velocityY;
    private final float radius;
    private float lifetime;

    public Projectile(float x, float y, float velocityX, float velocityY, float radius, float lifetime) {
        this.x = x;
        this.y = y;
        this.velocityX = velocityX;
        this.velocityY = velocityY;
        this.radius = radius;
        this.lifetime = lifetime;
    }

    public void update(float delta) {
        x += velocityX * delta;
        y += velocityY * delta;
        lifetime -= delta;
    }

    public float getX() {
        return x;
    }

    public float getY() {
        return y;
    }

    public float getRadius() {
        return radius;
    }

    public boolean isExpired(float worldWidth, float worldHeight) {
        return lifetime <= 0f
            || x < -radius
            || x > worldWidth + radius
            || y < -radius
            || y > worldHeight + radius;
    }
}
