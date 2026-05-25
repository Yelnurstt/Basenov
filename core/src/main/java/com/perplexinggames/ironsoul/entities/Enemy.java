package com.perplexinggames.ironsoul.entities;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Rectangle;
import com.perplexinggames.ironsoul.entities.components.AttackComponent;
import com.perplexinggames.ironsoul.entities.components.MovementComponent;

public class Enemy extends GameEntity {

    private final Rectangle bounds;
    private final Texture texture;
    private final float startX;
    private final float patrolDistance = 200f;
    private int direction = 1;
    private final AttackComponent attackComponent;
    private final MovementComponent movementComponent;
    private enum EnemyState {
        IDLE,
        ATTACK,
        DEAD
    }

    private EnemyState state = EnemyState.IDLE;
    public Enemy(float x, float y, float width, float height) {
        super(x, y, 50f);
        this.startX = x;
        attackComponent = new AttackComponent(10f);
        movementComponent = new MovementComponent(60f);
        bounds = new Rectangle(x, y, width, height);

        Pixmap pixmap = new Pixmap((int) width, (int) height, Pixmap.Format.RGBA8888);
        pixmap.setColor(Color.RED);
        pixmap.fill();

        texture = new Texture(pixmap);
        pixmap.dispose();
    }

    @Override
    public void update(float delta) {
        if (isDead()) {
            state = EnemyState.DEAD;
            return;
        }

        x += movementComponent.getSpeed() * direction * delta;

        if (x > startX + patrolDistance) {
            direction = -1;
        }

        if (x < startX) {
            direction = 1;
        }

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
        return attackComponent.getDamage();
    }

    public void dispose() {
        texture.dispose();
    }
    public boolean canAttack() {
        return state != EnemyState.DEAD;
    }
    public void setStateToAttack() {
        if (!isDead()) {
            state = EnemyState.ATTACK;
        }
    }
}
