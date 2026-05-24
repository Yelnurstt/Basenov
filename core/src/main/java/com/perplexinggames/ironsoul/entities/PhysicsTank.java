package com.perplexinggames.ironsoul.entities;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector3;
import com.perplexinggames.ironsoul.physics.TankPhysicsController;
import com.perplexinggames.ironsoul.tank.controller.FacingDirection;
import com.perplexinggames.ironsoul.terrain.TerrainCollisionProvider;

public class PhysicsTank {
    public final TankPhysicsController physics;

    private final Texture hullTex, tracksTex, turretTex;
    private final TextureRegion hullReg, tracksReg, turretReg;

    private float width = 80f;
    private boolean facingRight = true;

    private float aimX, aimY;
    private float currentTurretAngle = 0f;
    private final float turretSpeed = 5f; // Инерция башни

    public PhysicsTank(float x, float y, TerrainCollisionProvider terrain) {
        this.physics = new TankPhysicsController(terrain, x, y);
        this.hullTex = new Texture(Gdx.files.internal("hull.png"));
        this.tracksTex = new Texture(Gdx.files.internal("tracks.png"));
        this.turretTex = new Texture(Gdx.files.internal("turret.png"));

        this.hullReg = new TextureRegion(hullTex);
        this.tracksReg = new TextureRegion(tracksTex);
        this.turretReg = new TextureRegion(turretTex);
    }

    public void setAimTarget(float x, float y) {
        this.aimX = x;
        this.aimY = y;
    }

    public void update(float delta) {
        float inputAxis = 0;
        if (Gdx.input.isKeyPressed(Input.Keys.A) || Gdx.input.isKeyPressed(Input.Keys.LEFT)) {
            inputAxis = -1f;
            facingRight = false;
        }
        if (Gdx.input.isKeyPressed(Input.Keys.D) || Gdx.input.isKeyPressed(Input.Keys.RIGHT)) {
            inputAxis = 1f;
            facingRight = true;
        }

        // Обновляем спрайты, если поменяли направление
        if (facingRight && hullReg.isFlipX()) {
            hullReg.flip(true, false);
            tracksReg.flip(true, false);
            turretReg.flip(true, false);
        } else if (!facingRight && !hullReg.isFlipX()) {
            hullReg.flip(true, false);
            tracksReg.flip(true, false);
            turretReg.flip(true, false);
        }

        physics.update(delta, inputAxis);
        updateTurretAim(delta);
    }

    private void updateTurretAim(float delta) {
        // 1. Создаем матрицу танка (сдвиг + наклон)
        Matrix4 tankMatrix = new Matrix4();
        tankMatrix.setToTranslation(physics.x, physics.y, 0);
        tankMatrix.rotate(0, 0, 1, physics.rotation);

        // 2. Инвертируем матрицу, чтобы перевести координаты мыши в локальные координаты танка
        Matrix4 invMatrix = new Matrix4(tankMatrix).inv();
        Vector3 localAim = new Vector3(aimX, aimY, 0);
        localAim.mul(invMatrix); // Теперь мышь находится в системе координат, где танк стоит ровно в (0,0)

        // 3. Вычисляем пивот башни (локальный)
        float drawScale = width / hullTex.getWidth();
        float hullH = hullTex.getHeight() * drawScale;
        float tracksH = tracksTex.getHeight() * drawScale;
        float hullY = tracksH * 0.45f;

        float pivotXRatio = 0.5f;
        float pivotYRatio = 0.8f;
        float hullX = -width / 2f;
        float pivotX = facingRight ? (hullX + width * pivotXRatio) : (hullX + width * (1.0f - pivotXRatio));
        float pivotY = hullY + hullH * pivotYRatio;

        // 4. Считаем идеальный угол башни к мышке
        float targetAngle = MathUtils.atan2(localAim.y - pivotY, localAim.x - pivotX) * MathUtils.radiansToDegrees;

        // 5. ОГРАНИЧЕНИЕ УГЛОВ (не стрелять в свой корпус)
        if (facingRight) {
            // Разрешаем от -10 (чуть вниз) до 190 (чуть назад)
            if (targetAngle < -10 && targetAngle > -90) targetAngle = -10;
            if (targetAngle > 190 || targetAngle <= -90) targetAngle = 190;
        } else {
            // Для левой стороны лимиты инвертированы: от 190 (вниз-влево) до -10 (вниз-вправо)
            if (targetAngle > -170 && targetAngle < -90) targetAngle = -170;
            if (targetAngle < 10 && targetAngle >= -90) targetAngle = 10;
        }

        // 6. Плавно доводим текущий угол до нужного (инерция башни)
        currentTurretAngle = MathUtils.lerpAngleDeg(currentTurretAngle, targetAngle, turretSpeed * delta);
    }

    public void render(SpriteBatch batch) {
        Matrix4 originalMatrix = batch.getTransformMatrix().cpy();

        Matrix4 tankMatrix = new Matrix4();
        tankMatrix.setToTranslation(physics.x, physics.y, 0);
        tankMatrix.rotate(0, 0, 1, physics.rotation);
        batch.setTransformMatrix(tankMatrix); // Применяем пространство танка!

        float drawScale = width / hullTex.getWidth();
        float tracksW = tracksTex.getWidth() * drawScale;
        float tracksH = tracksTex.getHeight() * drawScale;
        float hullW = width;
        float hullH = hullTex.getHeight() * drawScale;

        // 1. Гусеницы
        batch.draw(tracksReg, -tracksW / 2f, 0, tracksW, tracksH);

        // 2. Башня (сзади корпуса)
        float gunW = turretTex.getWidth() * drawScale * 1.1f;
        float gunH = turretTex.getHeight() * drawScale * 0.9f;
        float hullX = -hullW / 2f;
        float hullY = tracksH * 0.45f;

        float pivotX = facingRight ? (hullX + hullW * 0.5f) : (hullX + hullW * 0.5f);
        float pivotY = hullY + hullH * 0.8f;

        float gunOriginX = facingRight ? 10f : (gunW - 10f);
        float gunOriginY = gunH / 2f;

        // Рисуем башню
        batch.draw(turretReg,
            pivotX - gunOriginX, pivotY - gunOriginY,
            gunOriginX, gunOriginY,
            gunW, gunH, 1f, 1f, currentTurretAngle);

        // 3. Корпус поверх башни
        batch.draw(hullReg, hullX, hullY, hullW, hullH);

        // Возвращаем оригинальную матрицу камеры
        batch.setTransformMatrix(originalMatrix);
    }

    public void dispose() {
        hullTex.dispose();
        tracksTex.dispose();
        turretTex.dispose();
    }

    public Rectangle getBounds() {
        float height = getVisualHeight();
        return new Rectangle(physics.x - width * 0.5f, physics.y, width, height);
    }

    public void setWorldPosition(float x, float y) {
        physics.x = x;
        physics.y = y;
        physics.velocity.setZero();
    }

    public void setFacingDirection(FacingDirection direction) {
        if (direction == null) {
            return;
        }
        facingRight = direction == FacingDirection.RIGHT;
    }

    public float getBodyWidth() {
        return width;
    }

    public float getBodyHeight() {
        return getVisualHeight();
    }

    private float getVisualHeight() {
        float drawScale = width / hullTex.getWidth();
        float tracksHeight = tracksTex.getHeight() * drawScale;
        float hullHeight = hullTex.getHeight() * drawScale;
        return tracksHeight + hullHeight;
    }
}
