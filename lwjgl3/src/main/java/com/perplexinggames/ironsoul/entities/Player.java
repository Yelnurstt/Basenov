package com.perplexinggames.ironsoul.entities;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.perplexinggames.ironsoul.utils.TextureGenerator;

public class Player extends Entity {
    private static final float GRAVITY = -900f;
    private static final float JUMP_FORCE = 450f;

    private final Texture texture;
    private final float speed;

    private boolean hasKey;
    private boolean grounded = false;
    private boolean physicsStarted = false;

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
        handleInput(delta);
    }

    private void handleInput(float delta) {
        if (!physicsStarted) {
            velocity.set(0, 0);

            if (Gdx.input.isKeyPressed(Input.Keys.A)
                || Gdx.input.isKeyPressed(Input.Keys.D)
                || Gdx.input.isKeyPressed(Input.Keys.LEFT)
                || Gdx.input.isKeyPressed(Input.Keys.RIGHT)
                || Gdx.input.isKeyJustPressed(Input.Keys.SPACE)) {
                physicsStarted = true;
            } else {
                return;
            }
        }

        velocity.x = 0;

        if (Gdx.input.isKeyPressed(Input.Keys.A)
            || Gdx.input.isKeyPressed(Input.Keys.LEFT)) {
            velocity.x = -speed;
        }

        if (Gdx.input.isKeyPressed(Input.Keys.D)
            || Gdx.input.isKeyPressed(Input.Keys.RIGHT)) {
            velocity.x = speed;
        }

        if (grounded && Gdx.input.isKeyJustPressed(Input.Keys.SPACE)) {
            velocity.y = JUMP_FORCE;
            grounded = false;
        }

        velocity.y += GRAVITY * delta;

        isInteracting = Gdx.input.isKeyJustPressed(Input.Keys.E);
    }

    public void setGrounded(boolean grounded) {
        this.grounded = grounded;
    }

    public boolean isGrounded() {
        return grounded;
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
