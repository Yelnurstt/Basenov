package com.perplexinggames.ironsoul.entities;

public class EnemyFactory {

    public enum EnemyType {
        BASIC,
        FAST,
        TANK,
        FLYING
    }

    public static Enemy createEnemy(
        EnemyType type,
        float x,
        float y
    ) {

        switch (type) {

            case FAST:
                return createFastEnemy(x, y);

            case TANK:
                return createTankEnemy(x, y);

            case FLYING:
                return createFlyingEnemy(x, y);

            case BASIC:
            default:
                return createBasicEnemy(x, y);
        }
    }

    private static Enemy createBasicEnemy(float x, float y) {

        Enemy enemy = new Enemy(x, y, 40, 40);

        enemy.setMaxHealth(50f);
        enemy.setHealth(50f);
        enemy.setSpeed(120f);

        return enemy;
    }

    private static Enemy createFastEnemy(float x, float y) {

        Enemy enemy = new Enemy(x, y, 32, 32);

        enemy.setMaxHealth(30f);
        enemy.setHealth(30f);
        enemy.setSpeed(220f);

        return enemy;
    }

    private static Enemy createTankEnemy(float x, float y) {

        Enemy enemy = new Enemy(x, y, 60, 60);

        enemy.setMaxHealth(200f);
        enemy.setHealth(200f);
        enemy.setSpeed(60f);

        return enemy;
    }

    private static Enemy createFlyingEnemy(float x, float y) {

        Enemy enemy = new Enemy(x, y, 36, 36);

        enemy.setMaxHealth(40f);
        enemy.setHealth(40f);
        enemy.setSpeed(160f);

        return enemy;
    }

}
