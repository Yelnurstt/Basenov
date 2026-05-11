package com.perplexinggames.ironsoul.entities;


import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.perplexinggames.ironsoul.utils.TextureGenerator;

public class Player extends Entity {
    private Texture texture;
    private float speed;
    private boolean hasKey;
    private java.util.ArrayList<String> inventory;

    public Player(float x, float y, float width, float height) {
        super(x, y, width, height);
        this.speed = 200f;
        this.hasKey = false;
        this.inventory = new java.util.ArrayList<>();

        // Временная текстура (замените на свою)
        // В конструкторе Player.java замените эту строку:
// this.texture = new Texture("player.png");

// НА ЭТО:
        this.texture = TextureGenerator.createColoredTexture((int)width, (int)height, 0x00FF00FF);
        // Если текстуры нет, создайте простой квадрат
        // или используйте запасной вариант
    }

    @Override
    public void update(float delta) {
        // Обработка ввода
        handleInput();

        // Применение физики
        super.update(delta);

        // Ограничение движения (временно, пока нет карты)
        if (x < 0) x = 0;
        if (y < 0) y = 0;
        if (x > 800) x = 800;
        if (y > 600) y = 600;
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

        // Взаимодействие (клавиша E)
        if (Gdx.input.isKeyJustPressed(Input.Keys.E)) {
            isInteracting = true;
        } else {
            isInteracting = false;
        }

        // Нормализация диагонального движения
        if (velocity.len() > 0) {
            velocity.nor().scl(speed);
        }
    }

    @Override
    public void handleCollision() {
        // Откат при столкновении со стеной
        // Можно реализовать более сложную логику
        x -= velocity.x * 0.1f;
        y -= velocity.y * 0.1f;
    }

    @Override
    public void render(SpriteBatch batch) {
        if (texture != null) {
            batch.draw(texture, x, y, width, height);
        } else {
            // Временный прямоугольник, если нет текстуры
            batch.setColor(0, 1, 0, 1);
            // Нужно будет использовать ShapeRenderer для прямоугольника
            // Или создайте временную текстуру
        }
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
        if (item.equals("key")) {
            hasKey = true;
        }
    }

    @Override
    public void die() {
        System.out.println("Player died!");
        // Логика смерти: перезапуск уровня, уменьшение жизней и т.д.
    }

    public void dispose() {
        if (texture != null) {
            texture.dispose();
        }
    }
}
