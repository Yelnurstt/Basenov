package com.perplexinggames.ironsoul.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.GridPoint2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.ScreenUtils;
import com.perplexinggames.ironsoul.editor.EditorInputAdapter;
import com.perplexinggames.ironsoul.editor.EditorMode;
import com.perplexinggames.ironsoul.editor.LevelEditor;
import com.perplexinggames.ironsoul.entities.Player;
import com.perplexinggames.ironsoul.gameplay.GameplayController;
import com.perplexinggames.ironsoul.input.PlayerInputController;
import com.perplexinggames.ironsoul.level.BlockData;
import com.perplexinggames.ironsoul.level.LevelCollision;
import com.perplexinggames.ironsoul.level.LevelData;
import com.perplexinggames.ironsoul.level.LevelRenderer;
import com.perplexinggames.ironsoul.level.RuntimeLevel;
import com.perplexinggames.ironsoul.level.serialization.JsonLevelSerializer;
import com.perplexinggames.ironsoul.level.serialization.LevelSerializer;
import com.perplexinggames.ironsoul.physics.BasicPhysicsController;

public class LevelEditorDemoScreen implements Screen {
    private SpriteBatch batch;
    private BitmapFont font;
    private OrthographicCamera worldCamera;
    private OrthographicCamera hudCamera;
    private RuntimeLevel runtimeLevel;
    private LevelCollision levelCollision;
    private LevelRenderer levelRenderer;
    private LevelEditor levelEditor;
    private EditorInputAdapter editorInputAdapter;
    private Player player;
    private GameplayController gameplayController;

    @Override
    public void show() {
        batch = new SpriteBatch();
        font = new BitmapFont();
        font.setColor(Color.WHITE);

        worldCamera = new OrthographicCamera();
        hudCamera = new OrthographicCamera();
        resize(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());

        LevelSerializer levelSerializer = new JsonLevelSerializer();
        runtimeLevel = new RuntimeLevel(LevelEditor.createEmptyLevelData());
        levelCollision = new LevelCollision(runtimeLevel);
        levelRenderer = new LevelRenderer();
        levelEditor = new LevelEditor(runtimeLevel, levelSerializer);

        LevelData initialLevelData = levelEditor.readLevelDataFromDefaultLocation();
        levelEditor.applyLoadedLevel(initialLevelData, levelEditor.getLastLoadSourceDescription(),
            levelEditor.wasLastLoadEmptyFallback());
        levelEditor.clearHistory();

        float playerSize = runtimeLevel.getTileSize();
        player = new Player(playerSize * 2f, playerSize * 2f);
        gameplayController = new GameplayController(
            runtimeLevel,
            player,
            new PlayerInputController(player),
            new BasicPhysicsController(player, runtimeLevel, levelCollision)
        );

        editorInputAdapter = new EditorInputAdapter(levelEditor, worldCamera);
        Gdx.input.setInputProcessor(editorInputAdapter);

        centerCameraOnPlayer();
        worldCamera.update();
    }

    @Override
    public void render(float delta) {
        update(delta);

        ScreenUtils.clear(0.09f, 0.1f, 0.12f, 1f);

        if (levelEditor.getMode() == EditorMode.EDITOR) {
            levelRenderer.renderEditor(runtimeLevel, worldCamera, levelEditor.getHoveredCell(), levelEditor.getSelectedCell());
        } else {
            levelRenderer.renderGameplay(runtimeLevel, worldCamera);
        }

        batch.setProjectionMatrix(worldCamera.combined);
        batch.begin();
        player.render(batch);
        batch.end();

        renderOverlay();
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
    }

    @Override
    public void pause() {
    }

    @Override
    public void resume() {
    }

    @Override
    public void hide() {
        if (Gdx.input.getInputProcessor() == editorInputAdapter) {
            Gdx.input.setInputProcessor(null);
        }
    }

    @Override
    public void dispose() {
        if (Gdx.input.getInputProcessor() == editorInputAdapter) {
            Gdx.input.setInputProcessor(null);
        }
        batch.dispose();
        font.dispose();
        levelRenderer.dispose();
        player.dispose();
    }

    private void update(float delta) {
        editorInputAdapter.update(delta);

        Vector3 mousePos = new Vector3(Gdx.input.getX(), Gdx.input.getY(), 0f);
        worldCamera.unproject(mousePos);
        player.setAimTarget(mousePos.x, mousePos.y);

        if (levelEditor.getMode() == EditorMode.GAMEPLAY) {
            updateGameplay(delta);
        } else {
            clampCameraToLevelBounds();
            worldCamera.update();
        }
    }

    private void updateGameplay(float delta) {
        gameplayController.update(delta);
        centerCameraOnPlayer();
        clampCameraToLevelBounds();
        worldCamera.update();
    }

    private void centerCameraOnPlayer() {
        worldCamera.position.set(player.getX() + player.getWidth() * 0.5f,
            player.getY() + player.getHeight() * 0.5f, 0f);
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
        font.draw(batch, buildOverlayText(), 12f, hudCamera.viewportHeight - 12f);
        batch.end();
    }

    private String buildOverlayText() {
        if (levelEditor.getMode() == EditorMode.GAMEPLAY) {
            return "MODE: GAMEPLAY\nF2: editor mode";
        }

        BlockData selectedBlock = levelEditor.getSelectedBlock();
        String hoveredCell = formatCell(levelEditor.getHoveredCell());
        String selectedCell = selectedBlock == null ? "none"
            : selectedBlock.type + " @ (" + selectedBlock.x + ", " + selectedBlock.y + ")";

        return new StringBuilder()
            .append("MODE: EDITOR\n")
            .append("TOOL: ").append(levelEditor.getCurrentToolName()).append('\n')
            .append("BLOCKS: ").append(runtimeLevel.getBlockCount()).append('\n')
            .append("HOVER: ").append(hoveredCell).append('\n')
            .append("SELECTED: ").append(selectedCell).append('\n')
            .append("STATUS: ").append(levelEditor.getLastStatusMessage()).append('\n')
            .append("CONTROLS: F1 gameplay | 1 place | 2 erase | 3 select\n")
            .append("S save | L load | Ctrl+Z undo | Ctrl+Y redo | Arrows move camera")
            .toString();
    }

    private String formatCell(GridPoint2 cell) {
        if (cell == null) {
            return "none";
        }
        if (!runtimeLevel.isInside(cell.x, cell.y)) {
            return "(" + cell.x + ", " + cell.y + ") [out]";
        }
        return "(" + cell.x + ", " + cell.y + ")";
    }
}
