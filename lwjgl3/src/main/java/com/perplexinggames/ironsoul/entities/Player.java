package com.perplexinggames.ironsoul.entities;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.MathUtils;

public class Player extends Entity {
    private static final float GRAVITY = -900f;
    private static final float JUMP_FORCE = 450f;

    private final Texture hullTexture;
    private final Texture tracksTexture;
    private final Texture turretTexture;

    private final TextureRegion hullRegion;
    private final TextureRegion tracksRegion;
    private final TextureRegion turretRegion;

    private final float speed;
    private boolean hasKey;
    private boolean grounded = false;
    private boolean physicsStarted = false;

    private boolean facingRight = true;

    // курсор мыши
    private float aimX;
    private float aimY;

    private final java.util.ArrayList<String> inventory;

    public Player(float x, float y) {
        super(x, y, 80, 60);
        this.speed = 200f;
        this.hasKey = false;
        this.inventory = new java.util.ArrayList<>();

        this.hullTexture = new Texture(Gdx.files.internal("hull.png"));
        this.tracksTexture = new Texture(Gdx.files.internal("tracks.png"));
        this.turretTexture = new Texture(Gdx.files.internal("turret.png"));

        this.hullRegion = new TextureRegion(hullTexture);
        this.tracksRegion = new TextureRegion(tracksTexture);
        this.turretRegion = new TextureRegion(turretTexture);

        this.width = 80f;
        float drawScale = this.width / hullTexture.getWidth();
        float tracksH = tracksTexture.getHeight() * drawScale;
        float hullH = hullTexture.getHeight() * drawScale;

        this.height = (tracksH * 0.45f) + hullH;
    }

    public void setAimTarget(float x, float y) {
        this.aimX = x;
        this.aimY = y;
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

        // Движение
        if (Gdx.input.isKeyPressed(Input.Keys.A) || Gdx.input.isKeyPressed(Input.Keys.LEFT)) {
            velocity.x = -speed;
            facingRight = false;
        }

        if (Gdx.input.isKeyPressed(Input.Keys.D) || Gdx.input.isKeyPressed(Input.Keys.RIGHT)) {
            velocity.x = speed;
            facingRight = true;
        }

        //движение влево
        if (facingRight && hullRegion.isFlipX()) {
            hullRegion.flip(true, false);
            tracksRegion.flip(true, false);
            turretRegion.flip(true, false);
        }
        else if (!facingRight && !hullRegion.isFlipX()) {
            hullRegion.flip(true, false);
            tracksRegion.flip(true, false);
            turretRegion.flip(true, false);
        }

        // Прыжок
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
        float drawScale = width / hullRegion.getRegionWidth();

        // 1. Гусеницы
        float tracksW = tracksRegion.getRegionWidth() * drawScale;
        float tracksH = tracksRegion.getRegionHeight() * drawScale;
        float tracksX = x + (width - tracksW) / 2f;
        float tracksY = y;

        // 2. Корпус
        float hullW = width;
        float hullH = hullRegion.getRegionHeight() * drawScale;
        float hullX = x;
        float hullY = y + tracksH * 0.45f; // Наезжает на гусеницы

        // 3. Пушка
        float gunScaleX = drawScale * 1.1f;
        float gunScaleY = drawScale * 0.9f;
        float gunW = turretRegion.getRegionWidth() * gunScaleX;
        float gunH = turretRegion.getRegionHeight() * gunScaleY;

        // !!! НАСТРОЙКА ПРИЦЕЛИВАНИЯ !!!
        float pivotXRatio = 0.5f;
        float pivotYRatio = 0.8f;

        float pivotX = facingRight ? (hullX + hullW * pivotXRatio) : (hullX + hullW * (1.0f - pivotXRatio));
        float pivotY = hullY + hullH * pivotYRatio;

        float gunOriginX = facingRight ? 10f : (gunW - 10f); // Сдвинули origin чуть внутрь казенника
        float gunOriginY = gunH / 2f; // По вертикальному центру

        float gunDrawX = pivotX - gunOriginX;
        float gunDrawY = pivotY - gunOriginY;

        float angleRad = MathUtils.atan2(aimY - pivotY, aimX - pivotX);
        float angleDeg = angleRad * MathUtils.radiansToDegrees;

        float gunRotation = facingRight ? angleDeg : (angleDeg - 180f);

        // --- ОТРИСОВКА ---
        // 1. Гусеницы
        batch.draw(tracksRegion, tracksX, tracksY, tracksW, tracksH);

        // 2. Пушка (Рисуем ДО корпуса, чтобы она "спряталась" под кабину)
        batch.draw(turretRegion, gunDrawX, gunDrawY, gunOriginX, gunOriginY, gunW, gunH, 1f, 1f, gunRotation);

        // 3. Корпус
        batch.draw(hullRegion, hullX, hullY, hullW, hullH);
    }

    @Override
    public boolean hasKey() { return hasKey; }

    public void setHasKey(boolean hasKey) { this.hasKey = hasKey; }

    @Override
    public void addItem(String item) {
        inventory.add(item);
        if ("key".equals(item)) hasKey = true;
    }

    @Override
    public void die() { System.out.println("Player died!"); }

    public void dispose() {
        hullTexture.dispose();
        tracksTexture.dispose();
        turretTexture.dispose();
    }
}
