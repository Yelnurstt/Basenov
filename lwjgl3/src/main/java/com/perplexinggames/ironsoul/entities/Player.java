package com.perplexinggames.ironsoul.entities;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.perplexinggames.ironsoul.utils.TextureGenerator;

public class Player extends Entity {
    private final Texture texture;
    private final float speed;
    private boolean hasKey;
    private final java.util.ArrayList<String> inventory;

    public Player(float x, float y, float width, float height) {
        super(x, y, width, height);
        this.speed = 200f;
        this.hasKey = false;
        this.inventory = new java.util.ArrayList<>();
        this.texture = TextureGenerator.createColoredTexture((int) width, (int) height, 0x00FF00FF);
    }

    @Override
    public void update(float delta) {
        handleInput();
        super.update(delta);
    }

    private void handleInput() {
        velocity.set(0, 0);

        if (Gdx.input.isKeyPressed(Input.Keys.W)) {
            velocity.y += speed;
        }
        if (Gdx.input.isKeyPressed(Input.Keys.S)) {
            velocity.y -= speed;
        }
        if (Gdx.input.isKeyPressed(Input.Keys.A)) {
            velocity.x -= speed;
        }
        if (Gdx.input.isKeyPressed(Input.Keys.D)) {
            velocity.x += speed;
        }

        isInteracting = Gdx.input.isKeyJustPressed(Input.Keys.E);

        if (velocity.len() > 0) {
            velocity.nor().scl(speed);
        }
    }

    @Override
    public void handleCollision() {
        x -= velocity.x * 0.1f;
        y -= velocity.y * 0.1f;
    }

    @Override
    public void render(SpriteBatch batch) {
        batch.draw(texture, x, y, width, height);
    }

    @Override
    public boolean hasKey() {
        return hasKey;
    }

    public void setHasKey(boolean hasKey) {
        this.hasKey = hasKey;
    }

    @Override
    public void addItem(String item) {
        inventory.add(item);
        if ("key".equals(item)) {
            hasKey = true;
        }
    }

    @Override
    public void die() {
        System.out.println("Player died!");
    }

    public void dispose() {
        texture.dispose();
    }
}
