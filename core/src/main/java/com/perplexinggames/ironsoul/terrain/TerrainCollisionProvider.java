package com.perplexinggames.ironsoul.terrain;

public interface TerrainCollisionProvider {
    // x, y - точка откуда пускаем луч вниз
    // probeLength - длина луча
    // outInfo - объект, в который запишется результат
    void getContactInfo(float x, float y, float probeLength, TerrainContactInfo outInfo);
}
