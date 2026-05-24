package com.perplexinggames.ironsoul.screens;

import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.perplexinggames.ironsoul.core.gameplay.DialogueSystem;
import com.perplexinggames.ironsoul.core.gameplay.InteractionSystem;
import com.perplexinggames.ironsoul.core.gameplay.OverlayController;
import com.perplexinggames.ironsoul.core.gamestate.GameState;
import com.perplexinggames.ironsoul.core.gamestate.GameStateManager;
import com.perplexinggames.ironsoul.core.gamestate.GameStateType;
import com.perplexinggames.ironsoul.core.gamestate.states.InventoryOpenState;
import com.perplexinggames.ironsoul.core.gamestate.states.MapOpenState;
import com.perplexinggames.ironsoul.core.gamestate.states.PauseState;
import com.perplexinggames.ironsoul.core.gamestate.states.PlayingState;
import com.perplexinggames.ironsoul.debug.DebugHud;
import com.perplexinggames.ironsoul.input.adapter.KeyboardControl;
import com.perplexinggames.ironsoul.input.adapter.LibGdxInputAdapter;
import com.perplexinggames.ironsoul.input.binding.ActionBindingProfile;
import com.perplexinggames.ironsoul.input.binding.InputAction;
import com.perplexinggames.ironsoul.input.binding.KeyboardBindingProfiles;
import com.perplexinggames.ironsoul.input.command.CommandContext;
import com.perplexinggames.ironsoul.input.command.DefaultCommandMapFactory;
import com.perplexinggames.ironsoul.input.command.InputCommandFactory;
import com.perplexinggames.ironsoul.input.mapper.ContextualInputMapper;
import com.perplexinggames.ironsoul.input.state.OverlayInputMode;
import com.perplexinggames.ironsoul.input.state.PauseInputMode;
import com.perplexinggames.ironsoul.input.state.PlayingInputMode;
import com.perplexinggames.ironsoul.input.strategy.GameInputCoordinator;
import com.perplexinggames.ironsoul.input.strategy.InputStrategy;
import com.perplexinggames.ironsoul.input.strategy.KeyboardInputStrategy;
import com.perplexinggames.ironsoul.projectile.ProjectileSystem;
import com.perplexinggames.ironsoul.tank.TankDebugRenderer;
import com.perplexinggames.ironsoul.tank.TankMovementConfig;
import com.perplexinggames.ironsoul.tank.combat.ShieldSystem;
import com.perplexinggames.ironsoul.tank.combat.WeaponSystem;
import com.perplexinggames.ironsoul.tank.controller.TankController;
import com.perplexinggames.ironsoul.tank.controller.TankModel;
import com.perplexinggames.ironsoul.terrain.SegmentTerrainCollisionProvider;
import com.perplexinggames.ironsoul.terrain.TerrainCollisionBuilder;
import com.perplexinggames.ironsoul.terrain.TerrainCollisionData;
import com.perplexinggames.ironsoul.terrain.TerrainDebugRenderer;
import com.perplexinggames.ironsoul.terrain.TerrainPath;
import com.perplexinggames.ironsoul.terrain.TerrainPoint;

import java.util.Arrays;
import java.util.EnumMap;

public class ControlDemoScreen implements Screen {
    private static final float WORLD_WIDTH = 1000f;
    private static final float WORLD_HEIGHT = 320f;

    private OrthographicCamera camera;
    private FitViewport viewport;
    private OrthographicCamera hudCamera;
    private ScreenViewport hudViewport;
    private ShapeRenderer shapeRenderer;
    private SpriteBatch spriteBatch;
    private BitmapFont font;

    private GameStateManager gameStateManager;
    private DialogueSystem dialogueSystem;
    private InteractionSystem interactionSystem;
    private OverlayController overlayController;
    private TankController tankController;
    private WeaponSystem weaponSystem;
    private ShieldSystem shieldSystem;
    private ProjectileSystem projectileSystem;
    private GameInputCoordinator inputCoordinator;
    private ActionBindingProfile<KeyboardControl> keyboardBindings;
    private TankDebugRenderer tankDebugRenderer;
    private TerrainDebugRenderer terrainDebugRenderer;
    private DebugHud debugHud;
    private TerrainPath terrainPath;
    private TerrainCollisionData terrainCollisionData;

    @Override
    public void show() {
        camera = new OrthographicCamera();
        viewport = new FitViewport(WORLD_WIDTH, WORLD_HEIGHT, camera);
        hudCamera = new OrthographicCamera();
        hudViewport = new ScreenViewport(hudCamera);
        shapeRenderer = new ShapeRenderer();
        spriteBatch = new SpriteBatch();
        font = new BitmapFont();
        font.getData().setScale(1f);
        tankDebugRenderer = new TankDebugRenderer();
        terrainDebugRenderer = new TerrainDebugRenderer();
        hudViewport.update(com.badlogic.gdx.Gdx.graphics.getWidth(), com.badlogic.gdx.Gdx.graphics.getHeight(), true);

        initializeDemo();
        debugHud = new DebugHud(font, keyboardBindings);
    }

    private void initializeDemo() {
        gameStateManager = new GameStateManager(createStates(), GameStateType.PLAYING);
        dialogueSystem = new DialogueSystem();
        overlayController = new OverlayController();
        interactionSystem = new InteractionSystem(dialogueSystem, gameStateManager);
        interactionSystem.setDialogueEnabled(false);

        terrainPath = createDemoTerrainPath();
        terrainCollisionData = new TerrainCollisionBuilder().build(terrainPath);
        TankMovementConfig movementConfig = TankMovementConfig.demoDefault(WORLD_WIDTH);
        TankModel tankModel = new TankModel(60f, 150f, 58f, 28f);
        tankController = new TankController(
            tankModel,
            movementConfig,
            new SegmentTerrainCollisionProvider(Arrays.asList(terrainCollisionData))
        );
        tankController.snapToGround();
        projectileSystem = new ProjectileSystem(WORLD_WIDTH, WORLD_HEIGHT);
        weaponSystem = new WeaponSystem(tankController, projectileSystem);
        shieldSystem = new ShieldSystem(tankController);

        CommandContext commandContext = new CommandContext(
            tankController,
            weaponSystem,
            shieldSystem,
            interactionSystem,
            overlayController,
            dialogueSystem,
            gameStateManager
        );

        keyboardBindings = KeyboardBindingProfiles.createDefault();
        InputCommandFactory commandFactory = DefaultCommandMapFactory.createDefault();
        ContextualInputMapper<KeyboardControl> keyboardMapper = new ContextualInputMapper<>(keyboardBindings, commandFactory);
        InputStrategy keyboardStrategy = new KeyboardInputStrategy(new LibGdxInputAdapter(), keyboardMapper);
        inputCoordinator = new GameInputCoordinator(keyboardStrategy, commandContext);
    }

    private EnumMap<GameStateType, GameState> createStates() {
        EnumMap<GameStateType, GameState> states = new EnumMap<>(GameStateType.class);
        states.put(GameStateType.PLAYING, new PlayingState(new PlayingInputMode()));
        states.put(GameStateType.PAUSED, new PauseState(new PauseInputMode()));
        states.put(GameStateType.MAP_OPEN, new MapOpenState(new OverlayInputMode(
            InputAction.PAUSE,
            InputAction.MAP,
            InputAction.INVENTORY
        )));
        states.put(GameStateType.INVENTORY_OPEN, new InventoryOpenState(new OverlayInputMode(
            InputAction.PAUSE,
            InputAction.MAP,
            InputAction.INVENTORY
        )));
        return states;
    }

    @Override
    public void render(float delta) {
        float frameDelta = Math.min(delta, 1f / 30f);
        inputCoordinator.update(frameDelta);

        if (gameStateManager.getCurrentState().updatesSimulation()) {
            tankController.update(frameDelta);
            weaponSystem.update(frameDelta);
            projectileSystem.update(frameDelta);
        }

        ScreenUtils.clear(0.05f, 0.07f, 0.09f, 1f);
        viewport.apply();
        camera.position.set(WORLD_WIDTH * 0.5f, WORLD_HEIGHT * 0.5f, 0f);
        camera.update();

        shapeRenderer.setProjectionMatrix(camera.combined);

        renderWorld();
        tankDebugRenderer.render(shapeRenderer, tankController, weaponSystem);
        projectileSystem.render(shapeRenderer);
        renderOverlayTint();

        hudViewport.apply();
        spriteBatch.setProjectionMatrix(hudCamera.combined);
        debugHud.render(
            spriteBatch,
            hudViewport.getWorldWidth(),
            hudViewport.getWorldHeight(),
            inputCoordinator,
            gameStateManager,
            tankController,
            weaponSystem,
            projectileSystem,
            interactionSystem,
            overlayController
        );
    }

    private void renderWorld() {
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        shapeRenderer.setColor(new Color(0.08f, 0.1f, 0.13f, 1f));
        shapeRenderer.rect(0f, 0f, WORLD_WIDTH, WORLD_HEIGHT);
        shapeRenderer.end();

        terrainDebugRenderer.render(shapeRenderer, terrainPath, terrainCollisionData);
    }

    private TerrainPath createDemoTerrainPath() {
        return new TerrainPath(
            "control-demo-terrain",
            Arrays.asList(
                new TerrainPoint("p0", 0f, 100f),
                new TerrainPoint("p1", 200f, 100f),
                new TerrainPoint("p2", 350f, 180f),
                new TerrainPoint("p3", 550f, 180f),
                new TerrainPoint("p4", 700f, 80f),
                new TerrainPoint("p5", 850f, 80f),
                new TerrainPoint("p6", 1000f, 220f)
            ),
            TerrainPath.CurveType.LINEAR,
            "demo-dirt",
            5f,
            1f
        );
    }

    private void renderOverlayTint() {
        if (!overlayController.isMapOpen() && !overlayController.isInventoryOpen() && !gameStateManager.isPaused()) {
            return;
        }

        Color overlayColor = gameStateManager.isPaused()
            ? new Color(0.1f, 0.08f, 0.04f, 0.42f)
            : overlayController.isMapOpen()
            ? new Color(0.04f, 0.12f, 0.18f, 0.38f)
            : new Color(0.05f, 0.16f, 0.08f, 0.38f);

        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        shapeRenderer.setColor(overlayColor);
        shapeRenderer.rect(0f, 0f, WORLD_WIDTH, WORLD_HEIGHT);
        shapeRenderer.end();
    }

    @Override
    public void resize(int width, int height) {
        if (width <= 0 || height <= 0) {
            return;
        }
        viewport.update(width, height, true);
        hudViewport.update(width, height, true);
    }

    @Override
    public void pause() {
    }

    @Override
    public void resume() {
    }

    @Override
    public void hide() {
    }

    @Override
    public void dispose() {
        shapeRenderer.dispose();
        spriteBatch.dispose();
        font.dispose();
    }
}
