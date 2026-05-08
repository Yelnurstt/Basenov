package com.perplexinggames.ironsoul;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.perplexinggames.ironsoul.core.gameplay.DialogueSystem;
import com.perplexinggames.ironsoul.core.gameplay.InteractionSystem;
import com.perplexinggames.ironsoul.core.gameplay.OverlayController;
import com.perplexinggames.ironsoul.core.gamestate.GameState;
import com.perplexinggames.ironsoul.core.gamestate.GameStateManager;
import com.perplexinggames.ironsoul.core.gamestate.GameStateType;
import com.perplexinggames.ironsoul.core.gamestate.states.CutsceneState;
import com.perplexinggames.ironsoul.core.gamestate.states.DialogueState;
import com.perplexinggames.ironsoul.core.gamestate.states.MainMenuState;
import com.perplexinggames.ironsoul.core.gamestate.states.PauseState;
import com.perplexinggames.ironsoul.core.gamestate.states.PlayingState;
import com.perplexinggames.ironsoul.input.adapter.KeyboardControl;
import com.perplexinggames.ironsoul.input.adapter.LibGdxInputAdapter;
import com.perplexinggames.ironsoul.input.binding.ActionBindingProfile;
import com.perplexinggames.ironsoul.input.binding.KeyboardBindingProfiles;
import com.perplexinggames.ironsoul.input.command.CommandContext;
import com.perplexinggames.ironsoul.input.command.DefaultCommandMapFactory;
import com.perplexinggames.ironsoul.input.command.InputCommandFactory;
import com.perplexinggames.ironsoul.input.mapper.ContextualInputMapper;
import com.perplexinggames.ironsoul.input.state.CutsceneInputMode;
import com.perplexinggames.ironsoul.input.state.DialogueInputMode;
import com.perplexinggames.ironsoul.input.state.MainMenuInputMode;
import com.perplexinggames.ironsoul.input.state.PauseInputMode;
import com.perplexinggames.ironsoul.input.state.PlayingInputMode;
import com.perplexinggames.ironsoul.input.strategy.GameInputCoordinator;
import com.perplexinggames.ironsoul.input.strategy.InputStrategy;
import com.perplexinggames.ironsoul.input.strategy.KeyboardInputStrategy;
import com.perplexinggames.ironsoul.tank.combat.ShieldSystem;
import com.perplexinggames.ironsoul.tank.combat.WeaponSystem;
import com.perplexinggames.ironsoul.tank.controller.AimDirection;
import com.perplexinggames.ironsoul.tank.controller.TankController;
import com.perplexinggames.ironsoul.tank.controller.TankModel;
import com.perplexinggames.ironsoul.tank.controller.TankPhysicsConfig;

import java.util.EnumMap;
import java.util.Locale;

public class FirstScreen implements Screen {
    private static final float WORLD_WIDTH = 40f;
    private static final float WORLD_HEIGHT = 22.5f;

    private OrthographicCamera camera;
    private FitViewport viewport;
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
    private GameInputCoordinator inputCoordinator;

    @Override
    public void show() {
        camera = new OrthographicCamera();
        viewport = new FitViewport(WORLD_WIDTH, WORLD_HEIGHT, camera);
        shapeRenderer = new ShapeRenderer();
        spriteBatch = new SpriteBatch();
        font = new BitmapFont();
        font.getData().setScale(1.1f);

        initializeArchitecture();
    }

    private void initializeArchitecture() {
        // The screen is the composition root. It wires states, strategies, and receivers,
        // then steps out of the way so gameplay systems remain decoupled from libGDX input.
        gameStateManager = new GameStateManager(createStates(), GameStateType.PLAYING);
        dialogueSystem = new DialogueSystem();
        overlayController = new OverlayController();
        interactionSystem = new InteractionSystem(dialogueSystem, gameStateManager);

        TankPhysicsConfig physicsConfig = TankPhysicsConfig.defaultConfig();
        TankModel tankModel = new TankModel(6f, physicsConfig.getGroundY(), 2.6f, 1.6f);
        tankController = new TankController(tankModel, physicsConfig);
        weaponSystem = new WeaponSystem(tankController);
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

        ActionBindingProfile<KeyboardControl> keyboardBindings = KeyboardBindingProfiles.createDefault();
        InputCommandFactory commandFactory = DefaultCommandMapFactory.createDefault();
        ContextualInputMapper<KeyboardControl> keyboardMapper = new ContextualInputMapper<>(
            keyboardBindings,
            commandFactory
        );
        InputStrategy keyboardStrategy = new KeyboardInputStrategy(new LibGdxInputAdapter(), keyboardMapper);
        inputCoordinator = new GameInputCoordinator(keyboardStrategy, commandContext);
    }

    private EnumMap<GameStateType, GameState> createStates() {
        EnumMap<GameStateType, GameState> states = new EnumMap<>(GameStateType.class);
        states.put(GameStateType.MAIN_MENU, new MainMenuState(new MainMenuInputMode()));
        states.put(GameStateType.PLAYING, new PlayingState(new PlayingInputMode()));
        states.put(GameStateType.PAUSE, new PauseState(new PauseInputMode()));
        states.put(GameStateType.DIALOGUE, new DialogueState(new DialogueInputMode()));
        states.put(GameStateType.CUTSCENE, new CutsceneState(new CutsceneInputMode()));
        return states;
    }

    @Override
    public void render(float delta) {
        float frameDelta = Math.min(delta, 1f / 30f);
        inputCoordinator.update(frameDelta);

        if (gameStateManager.getCurrentState().updatesSimulation()) {
            tankController.update(frameDelta);
            weaponSystem.update(frameDelta);
        }

        ScreenUtils.clear(0.05f, 0.07f, 0.09f, 1f);
        viewport.apply();
        camera.position.set(WORLD_WIDTH * 0.5f, WORLD_HEIGHT * 0.5f, 0f);
        camera.update();

        renderWorld();
        renderHud();
    }

    private void renderWorld() {
        shapeRenderer.setProjectionMatrix(camera.combined);
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);

        shapeRenderer.setColor(new Color(0.16f, 0.19f, 0.22f, 1f));
        shapeRenderer.rect(0f, 0f, WORLD_WIDTH, 2f);

        shapeRenderer.setColor(new Color(0.28f, 0.31f, 0.35f, 1f));
        shapeRenderer.rect(0f, 0f, 0.6f, WORLD_HEIGHT);
        shapeRenderer.rect(WORLD_WIDTH - 0.6f, 0f, 0.6f, WORLD_HEIGHT);

        Color tankColor = switch (tankController.getCurrentState().getId()) {
            case GROUNDED -> new Color(0.72f, 0.73f, 0.75f, 1f);
            case AIRBORNE -> new Color(0.86f, 0.72f, 0.38f, 1f);
            case WALL_SLIDE -> new Color(0.89f, 0.53f, 0.32f, 1f);
            case DASH -> new Color(0.52f, 0.8f, 0.95f, 1f);
            case STUNNED -> new Color(0.67f, 0.4f, 0.4f, 1f);
        };

        TankModel model = tankController.getModel();
        float bodyHeight = tankController.isCrouching() ? model.getHeight() * 0.7f : model.getHeight();
        shapeRenderer.setColor(tankColor);
        shapeRenderer.rect(model.getX(), model.getY(), model.getWidth(), bodyHeight);

        shapeRenderer.setColor(new Color(0.2f, 0.2f, 0.22f, 1f));
        shapeRenderer.rect(model.getX() + 0.25f, model.getY() - 0.2f, model.getWidth() - 0.5f, 0.25f);

        shapeRenderer.setColor(new Color(0.85f, 0.9f, 0.92f, 1f));
        float turretBaseX = model.getX() + model.getWidth() * 0.5f;
        float turretBaseY = model.getY() + bodyHeight * 0.7f;
        float cannonLength = 1.4f;
        float xDirection = model.getFacingDirection().sign();
        float yDirection = switch (weaponSystem.getAimDirection()) {
            case UP -> 0.85f;
            case DOWN -> -0.65f;
            case FORWARD -> 0f;
        };
        shapeRenderer.rectLine(
            turretBaseX,
            turretBaseY,
            turretBaseX + (cannonLength * xDirection),
            turretBaseY + cannonLength * yDirection,
            0.18f
        );

        if (shieldSystem.isRaised()) {
            shapeRenderer.setColor(new Color(0.4f, 0.86f, 0.74f, 0.55f));
            shapeRenderer.circle(
                turretBaseX + xDirection * 0.9f,
                model.getY() + bodyHeight * 0.55f,
                0.75f,
                24
            );
        }

        shapeRenderer.end();
    }

    private void renderHud() {
        spriteBatch.setProjectionMatrix(camera.combined);
        spriteBatch.begin();
        font.setColor(Color.WHITE);

        float startX = 0.8f;
        float startY = WORLD_HEIGHT - 0.8f;
        float lineHeight = 1.05f;

        font.draw(spriteBatch, "Iron Soul Input Architecture Demo", startX, startY);
        font.draw(spriteBatch, "Strategy: " + inputCoordinator.getActiveStrategyName(), startX, startY - lineHeight);
        font.draw(spriteBatch, "Game State: " + gameStateManager.getCurrentState().getName(), startX, startY - lineHeight * 2f);
        font.draw(spriteBatch, "Tank State: " + tankController.getCurrentState().getName(), startX, startY - lineHeight * 3f);
        font.draw(spriteBatch, "Tank Action: " + tankController.getLastAction(), startX, startY - lineHeight * 4f);
        font.draw(spriteBatch, "Combat: " + weaponSystem.getLastCombatAction(), startX, startY - lineHeight * 5f);
        font.draw(spriteBatch, "Shield: " + shieldSystem.getStatus(), startX, startY - lineHeight * 6f);
        font.draw(spriteBatch, "Overlay: " + overlayController.getLastOverlayAction(), startX, startY - lineHeight * 7f);
        font.draw(spriteBatch, "Interact: " + interactionSystem.getLastInteraction(), startX, startY - lineHeight * 8f);

        TankModel model = tankController.getModel();
        font.draw(
            spriteBatch,
            String.format(
                Locale.US,
                "Pos(%.2f, %.2f) Vel(%.2f, %.2f) Aim(%s) DashCD(%.2f)",
                model.getX(),
                model.getY(),
                model.getVelocityX(),
                model.getVelocityY(),
                weaponSystem.getAimDirection(),
                tankController.getDashCooldownRemaining()
            ),
            startX,
            startY - lineHeight * 9f
        );

        font.draw(spriteBatch, "Controls: A/D move, Space jump, Shift dash, Ctrl crouch", startX, startY - lineHeight * 11f);
        font.draw(spriteBatch, "Combat: J primary, K secondary, L hold/release charge, Q ram, E shield", startX, startY - lineHeight * 12f);
        font.draw(spriteBatch, "Utility: W/S aim, F interact/advance dialogue, M map, I inventory, Esc pause", startX, startY - lineHeight * 13f);

        if (dialogueSystem.isActive()) {
            font.setColor(new Color(0.96f, 0.91f, 0.72f, 1f));
            font.draw(spriteBatch, "Dialogue: " + dialogueSystem.getCurrentLine(), startX, 2.6f);
        } else if (overlayController.isMapOpen()) {
            font.setColor(new Color(0.7f, 0.86f, 0.97f, 1f));
            font.draw(spriteBatch, "Map overlay active", startX, 2.6f);
        } else if (overlayController.isInventoryOpen()) {
            font.setColor(new Color(0.78f, 0.94f, 0.76f, 1f));
            font.draw(spriteBatch, "Inventory overlay active", startX, 2.6f);
        } else if (gameStateManager.isPaused()) {
            font.setColor(new Color(0.98f, 0.86f, 0.7f, 1f));
            font.draw(spriteBatch, "Paused: gameplay commands blocked by PauseInputMode", startX, 2.6f);
        }

        spriteBatch.end();
    }

    @Override
    public void resize(int width, int height) {
        if (width <= 0 || height <= 0) {
            return;
        }
        viewport.update(width, height, true);
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
