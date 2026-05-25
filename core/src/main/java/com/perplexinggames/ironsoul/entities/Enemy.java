package com.perplexinggames.ironsoul.entities;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Rectangle;
import com.perplexinggames.ironsoul.entities.components.AttackComponent;
import com.perplexinggames.ironsoul.entities.components.MovementComponent;
import com.perplexinggames.ironsoul.entities.components.AIComponent;
import com.perplexinggames.ironsoul.entities.components.CollisionComponent;
public class Enemy extends GameEntity {

    private final CollisionComponent collisionComponent;
    private final Texture texture;
    private final float startX;
    private final float patrolDistance = 200f;
    private int direction = 1;
    private final AttackComponent attackComponent;
    private final MovementComponent movementComponent;
    private final AIComponent aiComponent;


    public Enemy(float x, float y, float width, float height) {
        super(x, y, 50f);
        this.startX = x;
        aiComponent = new AIComponent();
        attackComponent = new AttackComponent(10f);
        movementComponent = new MovementComponent(60f);
        collisionComponent = new CollisionComponent(x, y, width, height);

        Pixmap pixmap = new Pixmap((int) width, (int) height, Pixmap.Format.RGBA8888);
        pixmap.setColor(Color.RED);
        pixmap.fill();

        texture = new Texture(pixmap);
        pixmap.dispose();
    }

    @Override
    public void update(float delta) {
        if (isDead()) {
            aiComponent.setState(AIComponent.AIState.DEAD);
            return;
        }

        x += movementComponent.getSpeed() * direction * delta;

        if (x > startX + patrolDistance) {
            direction = -1;
        }

        if (x < startX) {
            direction = 1;
        }

        collisionComponent.updatePosition(x, y);
    }
    @Override
    public void render(SpriteBatch batch) {
        if (!isDead()) {
            batch.draw(
                texture,
                collisionComponent.getBounds().x,
                collisionComponent.getBounds().y,
                collisionComponent.getBounds().width,
                collisionComponent.getBounds().height
            );
        }
    }

    public Rectangle getBounds() {
        return collisionComponent.getBounds();
    }

    public float getDamage() {
        return attackComponent.getDamage();
    }

    public void dispose() {
        texture.dispose();
    }
    public boolean canAttack() {
        return aiComponent.canAttack();
    }
    public void setStateToAttack() {
        if (!isDead()) {
            aiComponent.setState(AIComponent.AIState.ATTACK);
        }
    }
}
