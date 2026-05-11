package com.perplexinggames.ironsoul.entities;



import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;

public abstract class Entity {
    protected float x, y;
    protected float width, height;
    protected float health;
    protected float maxHealth;
    protected Vector2 velocity;
    protected float movementMultiplier;
    protected float friction;
    protected boolean isInteracting;

    public Entity(float x, float y, float width, float height) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        this.velocity = new Vector2(0, 0);
        this.movementMultiplier = 1.0f;
        this.friction = 0.9f;
        this.isInteracting = false;
        this.health = 100;
        this.maxHealth = 100;
    }

    // Геттеры для коллизий
    public Rectangle getBoundingBox() {
        return new Rectangle(x, y, width, height);
    }

    // Обработка урона
    public void takeDamage(float damage) {
        health -= damage;
        if (health <= 0) {
            die();
        }
    }

    public void heal(float amount) {
        health = Math.min(maxHealth, health + amount);
    }

    public void die() {
        // Будет переопределено в наследниках
    }

    // Взаимодействие
    public boolean isInteracting() {
        return isInteracting;
    }

    public void setInteracting(boolean interacting) {
        isInteracting = interacting;
    }

    // Инвентарь (упрощенно)
    public boolean hasKey() {
        return false; // Будет реализовано в Player
    }

    public void addItem(String item) {
        // Будет реализовано в Player
    }

    // Движение и физика
    public void setMovementMultiplier(float multiplier) {
        this.movementMultiplier = multiplier;
    }

    public void setFriction(float friction) {
        this.friction = friction;
    }

    public void applyForce(Vector2 force) {
        velocity.add(force);
    }

    public void update(float delta) {
        // Применение движения
        x += velocity.x * delta;
        y += velocity.y * delta;

        // Применение трения
        velocity.scl(friction);

        // Остановка при очень маленькой скорости
        if (Math.abs(velocity.x) < 0.01f) velocity.x = 0;
        if (Math.abs(velocity.y) < 0.01f) velocity.y = 0;
    }

    public void handleCollision() {
        // Откат при столкновении
        // Будет переопределено в наследниках
    }

    // Рендеринг
    public abstract void render(SpriteBatch batch);

    // Геттеры и сеттеры
    public float getX() { return x; }
    public float getY() { return y; }
    public void setX(float x) { this.x = x; }
    public void setY(float y) { this.y = y; }
    public float getWidth() { return width; }
    public float getHeight() { return height; }
    public float getHealth() { return health; }
    public float getMaxHealth() { return maxHealth; }
    public Vector2 getVelocity() { return velocity; }
}
