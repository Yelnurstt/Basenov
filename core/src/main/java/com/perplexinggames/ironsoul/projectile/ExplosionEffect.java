package com.perplexinggames.ironsoul.projectile;

import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;

public class ExplosionEffect {
    private final float x, y;
    private float stateTime = 0f;
    private final Animation<TextureRegion> animation;
    private final float scale;
    private final float rotation; // Добавили угол поворота

    public ExplosionEffect(float x, float y, Animation<TextureRegion> animation, float scale, float rotation) {
        this.x = x;
        this.y = y;
        this.animation = animation;
        this.scale = scale;
        this.rotation = rotation;
    }

    public void update(float delta) {
        stateTime += delta;
    }

    public void render(SpriteBatch batch) {
        TextureRegion currentFrame = animation.getKeyFrame(stateTime, false);
        float width = currentFrame.getRegionWidth() * scale;
        float height = currentFrame.getRegionHeight() * scale;

        // Рисуем с учетом origin (центра) и поворота (rotation)
        batch.draw(currentFrame,
            x - width / 2f, y - height / 2f,
            width / 2f, height / 2f,          // Origin X, Y (центр)
            width, height,                    // Ширина, Высота
            1f, 1f,                           // Scale X, Scale Y
            rotation);                        // Угол поворота
    }

    public boolean isFinished() {
        return animation.isAnimationFinished(stateTime);
    }
}
