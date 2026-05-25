package com.perplexinggames.ironsoul.world.serialization;

import com.badlogic.gdx.files.FileHandle;
import com.perplexinggames.ironsoul.terrain.spline.BezierHandleMode;
import com.perplexinggames.ironsoul.terrain.spline.SplineControlPoint;
import com.perplexinggames.ironsoul.world.WorldData;
import org.junit.Test;

import java.nio.charset.StandardCharsets;
import java.nio.file.Path;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;

public class JsonWorldSerializerTest {
    @Test
    public void oldBezierPointWithoutHandlesLoadsSafely() throws Exception {
        String raw = "{\n"
            + "  worldBlocks: [{\n"
            + "    id: start-area,\n"
            + "    name: Start,\n"
            + "    width: 40,\n"
            + "    height: 30,\n"
            + "    tiles: [],\n"
            + "    terrain: [],\n"
            + "    splinePaths: [{\n"
            + "      id: spline-1,\n"
            + "      name: Path,\n"
            + "      curveType: BEZIER,\n"
            + "      closed: false,\n"
            + "      collisionEnabled: true,\n"
            + "      collisionThickness: 4,\n"
            + "      material: default,\n"
            + "      points: [{ id: p0, x: 0, y: 0 }, { id: p1, x: 100, y: 0 }]\n"
            + "    }],\n"
            + "    splineLayers: [],\n"
            + "    objects: [],\n"
            + "    enemies: [],\n"
            + "    rewards: [],\n"
            + "    triggers: [],\n"
            + "    gates: [],\n"
            + "    spawnPoints: []\n"
            + "  }],\n"
            + "  tileSize: 32,\n"
            + "  activeBlockId: start-area\n"
            + "}\n";
        Path tempFile = java.nio.file.Files.createTempFile("ironsoul-old-spline", ".json");
        java.nio.file.Files.writeString(tempFile, raw, StandardCharsets.UTF_8);

        JsonWorldSerializer serializer = new JsonWorldSerializer();
        WorldData loaded = serializer.load(new FileHandle(tempFile.toFile()), null);

        assertNotNull(loaded);
        assertFalse(loaded.worldBlocks.get(0).splinePaths.isEmpty());
        SplineControlPoint point = loaded.worldBlocks.get(0).splinePaths.get(0).getPoints().get(0);
        assertNotNull(point.handleMode);
    }

    @Test
    public void saveLoadPreservesHandleValuesAndMode() throws Exception {
        Path tempFile = java.nio.file.Files.createTempFile("ironsoul-spline-roundtrip", ".json");
        JsonWorldSerializer serializer = new JsonWorldSerializer();
        WorldData data = com.perplexinggames.ironsoul.editor.LevelEditor.createEmptyWorldData();
        data.worldBlocks.get(0).splinePaths.add(new com.perplexinggames.ironsoul.terrain.spline.SplinePath(
            "spline-1",
            "Bezier",
            java.util.List.of(
                new SplineControlPoint("p0", 0f, 0f, -10f, 0f, 25f, 5f, BezierHandleMode.MIRRORED),
                new SplineControlPoint("p1", 80f, 20f, -15f, -5f, 10f, 0f, BezierHandleMode.ALIGNED)
            ),
            com.perplexinggames.ironsoul.terrain.spline.SplineCurveType.BEZIER,
            false,
            true,
            4f,
            "default",
            null
        ));

        serializer.save(data, new FileHandle(tempFile.toFile()));
        WorldData loaded = serializer.load(new FileHandle(tempFile.toFile()), null);

        SplineControlPoint point = loaded.worldBlocks.get(0).splinePaths.get(0).getPoints().get(0);
        assertEquals(25f, point.outHandleX, 0.001f);
        assertEquals(5f, point.outHandleY, 0.001f);
        assertEquals(BezierHandleMode.MIRRORED, point.handleMode);
    }
}
