package com.perplexinggames.ironsoul.screens;
import com.perplexinggames.ironsoul.entities.Enemy;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.utils.ScreenUtils;
import com.perplexinggames.ironsoul.core.Main;
import com.perplexinggames.ironsoul.editor.EditorInputAdapter;
import com.perplexinggames.ironsoul.editor.EditorMode;
import com.perplexinggames.ironsoul.editor.LevelEditor;
import com.perplexinggames.ironsoul.editor.ui.EditorSideMenu;
import com.perplexinggames.ironsoul.entities.PhysicsTank;
import com.perplexinggames.ironsoul.level.LevelData;
import com.perplexinggames.ironsoul.level.LevelRenderer;
import com.perplexinggames.ironsoul.level.RuntimeLevel;
import com.perplexinggames.ironsoul.terrain.RuntimeTerrainCollisionProvider;
import com.perplexinggames.ironsoul.terrain.TerrainPath;
import com.perplexinggames.ironsoul.world.GateData;
import com.perplexinggames.ironsoul.world.WorldBlockData;
import com.perplexinggames.ironsoul.world.WorldData;
import com.perplexinggames.ironsoul.world.runtime.PhysicsTankRuntimeAdapter;
import com.perplexinggames.ironsoul.world.runtime.WorldSpawnResolver;
import com.perplexinggames.ironsoul.world.runtime.WorldStreamingService;
import com.perplexinggames.ironsoul.world.runtime.WorldTransitionService;
import com.perplexinggames.ironsoul.world.serialization.JsonWorldSerializer;
import com.perplexinggames.ironsoul.world.serialization.WorldSerializer;
import com.perplexinggames.ironsoul.entities.EnemyFactory;
public class LevelEditorDemoScreen implements Screen {
    private final Main game;

    private SpriteBatch batch;
    private BitmapFont font;
    private OrthographicCamera worldCamera;
    private OrthographicCamera hudCamera;
    private RuntimeLevel runtimeLevel;
    private LevelRenderer levelRenderer;
    private LevelEditor levelEditor;
    private EditorInputAdapter editorInputAdapter;
    private PhysicsTank tank;
    private Enemy enemy;
    private float damageCooldown = 0f;
    private EditorSideMenu editorSideMenu;
    private InputMultiplexer inputMultiplexer;
    private WorldStreamingService streamingService;
    private WorldTransitionService transitionService;

    public LevelEditorDemoScreen(Main game) {
        this.game = game;
    }

    @Override
    public void show() {
        batch = new SpriteBatch();
        font = new BitmapFont();
        font.setColor(Color.WHITE);

        worldCamera = new OrthographicCamera();
        hudCamera = new OrthographicCamera();
        resize(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());

        WorldSerializer worldSerializer = new JsonWorldSerializer();
        runtimeLevel = new RuntimeLevel(new LevelData("bootstrap", "Bootstrap", 40, 30, 32));
        levelRenderer = new LevelRenderer();
        levelEditor = new LevelEditor(runtimeLevel, worldSerializer);

        WorldData initialWorld = levelEditor.readWorldDataFromDefaultLocation();
        levelEditor.applyLoadedWorld(initialWorld, levelEditor.getLastLoadSourceDescription(),
            levelEditor.wasLastLoadEmptyFallback());
        levelEditor.clearHistory();

        tank = new PhysicsTank(runtimeLevel.getTileSize() * 2f, getInitialTankSpawnY(runtimeLevel.getTileSize()),
            new RuntimeTerrainCollisionProvider(runtimeLevel));
        streamingService = new WorldStreamingService(levelEditor.getWorldData());
        streamingService.setCurrentBlock(levelEditor.getActiveBlockId());
        transitionService = new WorldTransitionService(levelEditor.getWorldData(), streamingService,
            new PhysicsTankRuntimeAdapter(tank), blockId -> levelEditor.selectActiveBlock(blockId));
        spawnTankAtActiveBlock();
        enemy = EnemyFactory.createEnemy(
            EnemyFactory.EnemyType.BASIC,
            700,
            300
        );
        editorInputAdapter = new EditorInputAdapter(levelEditor, worldCamera);
        Skin skin = new Skin(Gdx.files.internal("ui/uiskin.json"));
        editorSideMenu = new EditorSideMenu(levelEditor, skin);

        inputMultiplexer = new InputMultiplexer();
        inputMultiplexer.addProcessor(editorSideMenu.getStage());
        inputMultiplexer.addProcessor(editorInputAdapter);
        Gdx.input.setInputProcessor(inputMultiplexer);

        centerCameraOnTank();
        worldCamera.update();
    }

    @Override
    public void render(float delta) {
        if (Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)) {
            game.showMainMenu();
            return;
        }
        if (Gdx.input.isKeyJustPressed(Input.Keys.F3) && editorSideMenu != null) {
            editorSideMenu.toggleVisibility();
        }

        update(delta);

        ScreenUtils.clear(0.09f, 0.1f, 0.12f, 1f);

        WorldBlockData activeBlock = levelEditor.getActiveBlock();
        if (levelEditor.getMode() == EditorMode.EDITOR) {
            levelRenderer.renderEditor(
                runtimeLevel,
                activeBlock,
                worldCamera,
                levelEditor.getHoveredCell(),
                levelEditor.getSelectedCell(),
                levelEditor.getSelectedTerrainPoint()
            );
        } else {
            levelRenderer.renderGameplay(runtimeLevel, activeBlock, worldCamera);
        }

        batch.setProjectionMatrix(worldCamera.combined);
        batch.begin();
        tank.render(batch);
        enemy.render(batch);
        batch.end();

        renderOverlay();

        editorSideMenu.act(delta);
        editorSideMenu.draw();
    }

    @Override
    public void resize(int width, int height) {
        if (width <= 0 || height <= 0) {
            return;
        }

        worldCamera.setToOrtho(false, width, height);
        hudCamera.setToOrtho(false, width, height);
        worldCamera.update();
        hudCamera.update();
        if (editorSideMenu != null) {
            editorSideMenu.resize(width, height);
        }
    }

    @Override
    public void pause() {
    }

    @Override
    public void resume() {
    }

    @Override
    public void hide() {
        if (Gdx.input.getInputProcessor() == inputMultiplexer) {
            Gdx.input.setInputProcessor(null);
        }
    }

    @Override
    public void dispose() {
        if (Gdx.input.getInputProcessor() == inputMultiplexer) {
            Gdx.input.setInputProcessor(null);
        }
        batch.dispose();
        font.dispose();
        enemy.dispose();
        levelRenderer.dispose();
        tank.dispose();
        if (editorSideMenu != null) {
            editorSideMenu.dispose();
        }
    }

    private void update(float delta) {
        editorInputAdapter.update(delta);

        Vector3 mousePos = new Vector3(Gdx.input.getX(), Gdx.input.getY(), 0f);
        worldCamera.unproject(mousePos);
        tank.setAimTarget(mousePos.x, mousePos.y);

        if (levelEditor.getMode() == EditorMode.GAMEPLAY) {
            updateGameplay(delta);
        } else {
            worldCamera.update();
        }
    }

    private void updateGameplay(float delta) {
        synchronizeGameplayBlockSelection();
        tank.update(delta);
        transitionService.update(levelEditor.getActiveBlock(), Gdx.input.isKeyJustPressed(Input.Keys.E));
        enemy.update(delta);

        if (damageCooldown > 0f) {
            damageCooldown -= delta;
        }
        centerCameraOnTank();
        clampCameraToLevelBounds();
        worldCamera.update();
        if (!enemy.isDead()
            && enemy.canAttack()
            && tank.getBounds().overlaps(enemy.getBounds())
            && damageCooldown <= 0f) {
            enemy.setStateToAttack();
            damageCooldown = 1f;

            System.out.println("Tank damaged by enemy!");

            enemy.takeDamage(10f);

            System.out.println("Enemy HP: " + enemy.getHealth());

            if (enemy.isDead()) {
                System.out.println("ENEMY DEAD");
            }
        }
    }

    private void synchronizeGameplayBlockSelection() {
        if (streamingService == null) {
            return;
        }
        if (!levelEditor.getActiveBlockId().equals(streamingService.getCurrentBlock())) {
            streamingService.setCurrentBlock(levelEditor.getActiveBlockId());
            spawnTankAtActiveBlock();
        }
    }

    private void spawnTankAtActiveBlock() {
        WorldBlockData activeBlock = levelEditor.getActiveBlock();
        if (activeBlock == null) {
            return;
        }
        if (!activeBlock.spawnPoints.isEmpty()) {
            transitionService.moveTankToSpawnPoint(
                WorldSpawnResolver.resolveSafeSpawnPoint(activeBlock, activeBlock.spawnPoints.get(0), levelEditor.getWorldData().tileSize)
            );
        } else {
            tank.setWorldPosition(runtimeLevel.getTileSize() * 2f, getInitialTankSpawnY(runtimeLevel.getTileSize()));
        }
    }

    private void centerCameraOnTank() {
        worldCamera.position.set(tank.physics.x, tank.physics.y, 0f);
    }

    private void clampCameraToLevelBounds() {
        float halfWidth = worldCamera.viewportWidth * worldCamera.zoom * 0.5f;
        float halfHeight = worldCamera.viewportHeight * worldCamera.zoom * 0.5f;
        float worldWidth = runtimeLevel.getPixelWidth();
        float worldHeight = runtimeLevel.getPixelHeight();

        if (worldWidth <= halfWidth * 2f) {
            worldCamera.position.x = worldWidth * 0.5f;
        } else {
            worldCamera.position.x = Math.max(halfWidth, Math.min(worldCamera.position.x, worldWidth - halfWidth));
        }

        if (worldHeight <= halfHeight * 2f) {
            worldCamera.position.y = worldHeight * 0.5f;
        } else {
            worldCamera.position.y = Math.max(halfHeight, Math.min(worldCamera.position.y, worldHeight - halfHeight));
        }
    }

    private void renderOverlay() {
        batch.setProjectionMatrix(hudCamera.combined);
        batch.begin();
        String overlayText = buildOverlayText();
        if (!overlayText.isBlank()) {
            font.draw(batch, overlayText, 12f, hudCamera.viewportHeight - 12f);
        }
        String interactionText = transitionService == null ? null : transitionService.getActiveInteractionText();
        if (interactionText != null && !interactionText.isBlank()) {
            font.draw(batch, interactionText, hudCamera.viewportWidth * 0.35f, 48f);
        }
        batch.end();
    }

    private String buildOverlayText() {
        if (levelEditor.getMode() == EditorMode.GAMEPLAY) {
            return new StringBuilder()
                .append("MODE: GAMEPLAY\n")
                .append("BLOCK: ").append(levelEditor.getActiveBlockId()).append('\n')
                .append("LOADED: ").append(String.join(", ", streamingService.getLoadedBlocks())).append('\n')
                .append("CONNECTED: ").append(String.join(", ", streamingService.getConnectedBlocks())).append('\n')
                .append("ACTIVE GATE: ").append(formatGate(transitionService.getActiveGate())).append('\n')
                .append("LAST TRANSITION: ").append(transitionService.getLastTransitionLog()).append('\n')
                .append("F2: editor mode | F3: toggle menu | E: interact gate")
                .toString();
        }

        if (editorSideMenu != null && editorSideMenu.isMenuVisible()) {
            return "";
        }

        WorldBlockData activeBlock = levelEditor.getActiveBlock();
        return new StringBuilder()
            .append("MODE: EDITOR\n")
            .append("BLOCK: ").append(activeBlock == null ? "none" : activeBlock.id).append('\n')
            .append("SOLID: ").append(runtimeLevel.getBlockCount())
            .append(" | TERRAIN: ").append(runtimeLevel.getTerrainPointCount()).append(" pts\n")
            .append("MMB drag: pan | Wheel: zoom | F1: gameplay | F3: show menu")
            .toString();
    }

    private String formatGate(GateData gate) {
        if (gate == null) {
            return "none";
        }
        return gate.id + " -> " + gate.targetBlockId + "/" + gate.targetSpawnPointId + " [" + gate.transitionType + "]";
    }

    private float getInitialTankSpawnY(float tileSize) {
        float highestTerrainY = 0f;
        for (TerrainPath terrainPath : runtimeLevel.getTerrainPaths()) {
            for (com.perplexinggames.ironsoul.terrain.TerrainPoint point : terrainPath.getPoints()) {
                highestTerrainY = Math.max(highestTerrainY, point.getY());
            }
        }

        float spawnMargin = tileSize * 4f;
        float minSpawnY = tileSize * 3f;
        float maxSpawnY = runtimeLevel.getPixelHeight() - tileSize * 2f;
        return MathUtils.clamp(highestTerrainY + spawnMargin, minSpawnY, Math.max(minSpawnY, maxSpawnY));
    }
}
