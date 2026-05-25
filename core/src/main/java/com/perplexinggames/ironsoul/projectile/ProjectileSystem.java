package com.perplexinggames.ironsoul.projectile;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.utils.Array;

public class ProjectileSystem {
    private final Array<Projectile> activeProjectiles = new Array<>();
    private final Array<ExplosionEffect> activeExplosions = new Array<>();

    private final Texture bulletTex, expTex1, expTex2, expTex3;
    private final TextureRegion bulletRegion;
    private final Animation<TextureRegion> explosionAnimation;

    public ProjectileSystem() {
        bulletTex = new Texture(Gdx.files.internal("tank_bulletFly4.png"));
        expTex1 = new Texture(Gdx.files.internal("tank_explosion2.png"));
        expTex2 = new Texture(Gdx.files.internal("tank_explosion3.png"));
        expTex3 = new Texture(Gdx.files.internal("tank_explosion4.png"));

        bulletRegion = new TextureRegion(bulletTex);

        Array<TextureRegion> frames = new Array<>();
        frames.add(new TextureRegion(expTex1));
        frames.add(new TextureRegion(expTex2));
        frames.add(new TextureRegion(expTex3));

        explosionAnimation = new Animation<>(0.08f, frames);
    }

    public void spawnPhysicsShot(float x, float y, float dirX, float dirY, float speed) {
        activeProjectiles.add(new Projectile(x, y, dirX * speed, dirY * speed, 10f, 4.0f, bulletRegion));
    }

    public void spawnExplosion(float x, float y) {
        activeExplosions.add(new ExplosionEffect(x, y, explosionAnimation, 1.0f));
    }

    public void update(float delta) {
        for (int i = activeProjectiles.size - 1; i >= 0; i--) {
            Projectile p = activeProjectiles.get(i);
            p.update(delta);
            if (p.isExpired()) {
                activeProjectiles.removeIndex(i);
            }
        }

        for (int i = activeExplosions.size - 1; i >= 0; i--) {
            ExplosionEffect exp = activeExplosions.get(i);
            exp.update(delta);
            if (exp.isFinished()) activeExplosions.removeIndex(i);
        }
    }

    public void render(SpriteBatch batch) {
        for (Projectile p : activeProjectiles) p.render(batch);
        for (ExplosionEffect exp : activeExplosions) exp.render(batch);
    }

    public Array<Projectile> getProjectiles() {
        return activeProjectiles;
    }

    public int getActiveProjectileCount() {
        return activeProjectiles.size;
    }

    public void dispose() {
        bulletTex.dispose();
        expTex1.dispose();
        expTex2.dispose();
        expTex3.dispose();
    }
}
