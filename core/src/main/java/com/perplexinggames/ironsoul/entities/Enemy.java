package com.perplexinggames.ironsoul.entities;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Rectangle;

public class Enemy extends GameEntity {

    private final Rectangle bounds;
    private final Texture texture;

    private final float damage = 10f;

    public Enemy(float x, float y, float width, float height) {
        super(x, y, 50f);

        bounds = new Rectangle(x, y, width, height);

        Pixmap pixmap = new Pixmap((int) width, (int) height, Pixmap.Format.RGBA8888);
        pixmap.setColor(Color.RED);
        pixmap.fill();

        texture = new Texture(pixmap);
        pixmap.dispose();
    }

    @Override
    public void update(float delta) {
        bounds.setPosition(x, y);
    }

    @Override
    public void render(SpriteBatch batch) {
        if (!isDead()) {
            batch.draw(texture, bounds.x, bounds.y, bounds.width, bounds.height);
        }
    }

    public Rectangle getBounds() {
        return bounds;
    }

    public float getDamage() {
        return damage;
    }

    public void dispose() {
        texture.dispose();
    }
}
