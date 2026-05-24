package com.perplexinggames.ironsoul.entities;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Rectangle;

public class Enemy {
    private final Rectangle bounds;
    private final Texture texture;

    private float health = 50f;
    private final float damage = 10f;

    public Enemy(float x, float y, float width, float height) {
        bounds = new Rectangle(x, y, width, height);

        Pixmap pixmap = new Pixmap((int) width, (int) height, Pixmap.Format.RGBA8888);
        pixmap.setColor(Color.RED);
        pixmap.fill();
        texture = new Texture(pixmap);
        pixmap.dispose();
    }

    public void update(float delta) {
    }

    public void render(SpriteBatch batch) {
        if (!isDead()) {
            batch.draw(texture, bounds.x, bounds.y, bounds.width, bounds.height);
        }
    }

    public void takeDamage(float amount) {
        health -= amount;
        if (health < 0f) health = 0f;
    }

    public boolean isDead() {
        return health <= 0f;
    }

    public Rectangle getBounds() {
        return bounds;
    }

    public float getDamage() {
        return damage;
    }

    public float getHealth() {
        return health;
    }

    public void dispose() {
        texture.dispose();
    }
}
