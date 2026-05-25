package com.perplexinggames.ironsoul.projectile;

import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;

public class ExplosionEffect {
    private final float x, y;
    private float stateTime = 0f;
    private final Animation<TextureRegion> animation;
    private final float scale;

    public ExplosionEffect(float x, float y, Animation<TextureRegion> animation, float scale) {
        this.x = x; this.y = y; this.animation = animation; this.scale = scale;
    }

    public void update(float delta) { stateTime += delta; }

    public void render(SpriteBatch batch) {
        TextureRegion currentFrame = animation.getKeyFrame(stateTime, false);
        float width = currentFrame.getRegionWidth() * scale;
        float height = currentFrame.getRegionHeight() * scale;
        batch.draw(currentFrame, x - width / 2f, y - height / 2f, width, height);
    }

    public boolean isFinished() { return animation.isAnimationFinished(stateTime); }
}
