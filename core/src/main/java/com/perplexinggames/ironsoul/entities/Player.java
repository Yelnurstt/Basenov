package com.perplexinggames.ironsoul.entities;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.MathUtils;
import com.perplexinggames.ironsoul.physics.BasicPhysicsController;

public class Player extends Entity {
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
        handleLegacyInput(delta);
    }

    private void handleLegacyInput(float delta) {
        if (!physicsStarted) {
            velocity.set(0f, 0f);

            if (Gdx.input.isKeyPressed(Input.Keys.A)
                || Gdx.input.isKeyPressed(Input.Keys.D)
                || Gdx.input.isKeyPressed(Input.Keys.LEFT)
                || Gdx.input.isKeyPressed(Input.Keys.RIGHT)
                || Gdx.input.isKeyJustPressed(Input.Keys.SPACE)) {
                startPhysics();
            } else {
                return;
            }
        }

        stopHorizontalMovement();

        if (Gdx.input.isKeyPressed(Input.Keys.A) || Gdx.input.isKeyPressed(Input.Keys.LEFT)) {
            moveLeft();
        }

        if (Gdx.input.isKeyPressed(Input.Keys.D) || Gdx.input.isKeyPressed(Input.Keys.RIGHT)) {
            moveRight();
        }

        if (Gdx.input.isKeyJustPressed(Input.Keys.SPACE)) {
            jump();
        }

        applyGravity(BasicPhysicsController.GRAVITY, delta);
        isInteracting = Gdx.input.isKeyJustPressed(Input.Keys.E);
    }

    public void startPhysics() {
        physicsStarted = true;
    }

    public boolean isPhysicsStarted() {
        return physicsStarted;
    }

    public void stopHorizontalMovement() {
        velocity.x = 0f;
    }

    public void moveLeft() {
        velocity.x = -speed;
        setFacingRight(false);
    }

    public void moveRight() {
        velocity.x = speed;
        setFacingRight(true);
    }

    public void jump() {
        if (!grounded) {
            return;
        }

        velocity.y = JUMP_FORCE;
        grounded = false;
    }

    public void applyGravity(float gravity, float delta) {
        velocity.y += gravity * delta;
    }

    private void setFacingRight(boolean facingRight) {
        if (this.facingRight == facingRight) {
            return;
        }

        this.facingRight = facingRight;
        hullRegion.flip(true, false);
        tracksRegion.flip(true, false);
        turretRegion.flip(true, false);
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

        float tracksW = tracksRegion.getRegionWidth() * drawScale;
        float tracksH = tracksRegion.getRegionHeight() * drawScale;
        float tracksX = x + (width - tracksW) / 2f;
        float tracksY = y;

        float hullW = width;
        float hullH = hullRegion.getRegionHeight() * drawScale;
        float hullX = x;
        float hullY = y + tracksH * 0.45f;

        float gunScaleX = drawScale * 1.1f;
        float gunScaleY = drawScale * 0.9f;
        float gunW = turretRegion.getRegionWidth() * gunScaleX;
        float gunH = turretRegion.getRegionHeight() * gunScaleY;

        float pivotXRatio = 0.5f;
        float pivotYRatio = 0.8f;

        float pivotX = facingRight ? (hullX + hullW * pivotXRatio) : (hullX + hullW * (1.0f - pivotXRatio));
        float pivotY = hullY + hullH * pivotYRatio;

        float gunOriginX = facingRight ? 10f : (gunW - 10f);
        float gunOriginY = gunH / 2f;

        float gunDrawX = pivotX - gunOriginX;
        float gunDrawY = pivotY - gunOriginY;

        float angleRad = MathUtils.atan2(aimY - pivotY, aimX - pivotX);
        float angleDeg = angleRad * MathUtils.radiansToDegrees;
        float gunRotation = facingRight ? angleDeg : (angleDeg - 180f);

        batch.draw(tracksRegion, tracksX, tracksY, tracksW, tracksH);
        batch.draw(turretRegion, gunDrawX, gunDrawY, gunOriginX, gunOriginY, gunW, gunH, 1f, 1f, gunRotation);
        batch.draw(hullRegion, hullX, hullY, hullW, hullH);
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
        hullTexture.dispose();
        tracksTexture.dispose();
        turretTexture.dispose();
    }
}
