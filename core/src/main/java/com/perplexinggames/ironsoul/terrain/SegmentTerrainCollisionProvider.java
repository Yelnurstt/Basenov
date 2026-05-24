package com.perplexinggames.ironsoul.terrain;

public class SegmentTerrainCollisionProvider implements TerrainCollisionProvider {
    private final TerrainPath path;

    public SegmentTerrainCollisionProvider(TerrainPath path) {
        this.path = path;
    }

    @Override
    public void getContactInfo(float x, float y, float probeLength, TerrainContactInfo outInfo) {
        outInfo.reset();

        for (TerrainSegment segment : path.getSegments()) {
            if (segment.containsX(x)) {
                float surfaceY = segment.getY(x);

                // ФИКС: Даем погрешность (-15f). Если танк въезжает на крутую гору,
                // луч может начаться чуть ниже поверхности. Регистрируем контакт все равно!
                if (y >= surfaceY - 15f && (y - probeLength) <= surfaceY + 15f) {
                    outInfo.hasContact = true;
                    outInfo.point.set(x, surfaceY);
                    outInfo.normal.set(segment.normal);
                    outInfo.angle = segment.angle;
                    return;
                }
            }
        }
    }
}
