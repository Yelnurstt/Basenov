package com.perplexinggames.ironsoul.entities;

import com.badlogic.gdx.utils.Array;

public class EnemySpawner {

    public Array<Enemy> spawnEnemies(Array<EnemySpawnData> spawnDataList) {

        Array<Enemy> enemies = new Array<>();

        for (EnemySpawnData spawnData : spawnDataList) {

            enemies.add(
                EnemyFactory.createEnemy(
                    spawnData.type,
                    spawnData.x,
                    spawnData.y
                )
            );
        }

        return enemies;
    }
}
