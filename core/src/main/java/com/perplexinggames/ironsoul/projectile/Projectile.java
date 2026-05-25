package com.perplexinggames.ironsoul.projectile;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Rectangle;

public class Projectile {
    private float x, y;
    private float velocityX, velocityY;
    private final float radius;
    private float lifetime;
    private float rotation;
    private final TextureRegion texture;
    private boolean isDestroyed = false;

    public Projectile(float x, float y, float velocityX, float velocityY, float radius, float lifetime, TextureRegion texture) {
        this.x = x; this.y = y; this.velocityX = velocityX; this.velocityY = velocityY;
        this.radius = radius; this.lifetime = lifetime; this.texture = texture;
    }

    public void update(float delta) {
        velocityY += -1000f * delta; // Гравитация
        x += velocityX * delta;
        y += velocityY * delta;
        lifetime -= delta;
        rotation = MathUtils.atan2(velocityY, velocityX) * MathUtils.radiansToDegrees;
    }

    public void render(SpriteBatch batch) {
        float width = texture.getRegionWidth() * 0.5f;
        float height = texture.getRegionHeight() * 0.5f;
        batch.draw(texture, x - width / 2f, y - height / 2f, width / 2f, height / 2f, width, height, 1f, 1f, rotation);
    }

    public Rectangle getBounds() { return new Rectangle(x - radius, y - radius, radius * 2, radius * 2); }
    public boolean isExpired() { return lifetime <= 0f || isDestroyed; }
    public void destroy() { this.isDestroyed = true; }
    public float getX() { return x; }
    public float getY() { return y; }
}
