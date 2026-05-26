package com.perplexinggames.ironsoul.entities;

public class EnemySpawnData {

    public final EnemyFactory.EnemyType type;
    public final float x;
    public final float y;

    public EnemySpawnData(
        EnemyFactory.EnemyType type,
        float x,
        float y
    ) {
        this.type = type;
        this.x = x;
        this.y = y;
    }
}
