package com.perplexinggames.ironsoul.entities;

public class EnemyFactory {

    public enum EnemyType {
        BASIC
    }

    public static Enemy createEnemy(EnemyType type, float x, float y) {
        switch (type) {
            case BASIC:
            default:
                return new Enemy(x, y, 40, 40);
        }
    }
}
