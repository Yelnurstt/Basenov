package com.perplexinggames.ironsoul.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.perplexinggames.ironsoul.core.Main;

public class MainMenuScreen implements Screen {

    private Main game;
    private Stage stage;
    private Skin skin;
    private ShapeRenderer shapeRenderer;
    private float time = 0;

    public MainMenuScreen(Main game) {
        this.game = game;
    }

    @Override
    public void show() {
        stage = new Stage(new ScreenViewport());
        Gdx.input.setInputProcessor(stage);
        skin = new Skin(Gdx.files.internal("ui/uiskin.json"));
        shapeRenderer = new ShapeRenderer();

        Table table = new Table();
        table.setFillParent(true);
        stage.addActor(table);

        Label titleLabel = new Label("IRON SOUL", skin);
        titleLabel.setFontScale(3.0f);
        titleLabel.setColor(Color.valueOf("F1C40F")); // Yellowish tint

        TextButton playButton = createAnimatedButton("Play / Играть");
        TextButton editorButton = createAnimatedButton("World Editor / Редактировать мир");
        TextButton exitButton = createAnimatedButton("Exit / Выйти");

        playButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                stage.addAction(Actions.sequence(
                    Actions.fadeOut(0.3f),
                    Actions.run(() -> game.showGame())
                ));
            }
        });

        editorButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                stage.addAction(Actions.sequence(
                    Actions.fadeOut(0.3f),
                    Actions.run(() -> game.showWorldEditor())
                ));
            }
        });

        exitButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                stage.addAction(Actions.sequence(
                    Actions.fadeOut(0.3f),
                    Actions.run(() -> game.exitGame())
                ));
            }
        });

        table.add(titleLabel).padBottom(60).row();
        table.add(playButton).width(350).height(60).pad(10).row();
        table.add(editorButton).width(350).height(60).pad(10).row();
        table.add(exitButton).width(350).height(60).pad(10).row();

        // Entry animation
        table.getColor().a = 0f;
        table.addAction(Actions.fadeIn(1.0f, Interpolation.fade));
    }

    private TextButton createAnimatedButton(String text) {
        TextButton button = new TextButton(text, skin);
        button.setTransform(true);
        button.setOrigin(Align.center);
        
        button.addListener(new ClickListener() {
            @Override
            public void enter(InputEvent event, float x, float y, int pointer, Actor fromActor) {
                if (pointer == -1) { // -1 means mouse, not touch
                    button.clearActions();
                    button.addAction(Actions.parallel(
                        Actions.scaleTo(1.05f, 1.05f, 0.1f, Interpolation.fade),
                        Actions.color(Color.valueOf("3498DB"), 0.1f)
                    ));
                }
            }

            @Override
            public void exit(InputEvent event, float x, float y, int pointer, Actor toActor) {
                if (pointer == -1) {
                    button.clearActions();
                    button.addAction(Actions.parallel(
                        Actions.scaleTo(1f, 1f, 0.1f, Interpolation.fade),
                        Actions.color(Color.WHITE, 0.1f)
                    ));
                }
            }
        });
        return button;
    }

    @Override
    public void render(float delta) {
        time += delta;
        
        // Render dynamic background
        Gdx.gl.glClearColor(0.05f, 0.05f, 0.1f, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        drawBackgroundGrid();

        stage.act(Math.min(delta, 1 / 30f));
        stage.draw();
    }

    private void drawBackgroundGrid() {
        Gdx.gl.glEnable(GL20.GL_BLEND);
        Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
        
        shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
        shapeRenderer.setColor(0.2f, 0.4f, 0.8f, 0.15f);
        
        int width = Gdx.graphics.getWidth();
        int height = Gdx.graphics.getHeight();
        float cellSize = 50f;
        
        // Moving grid effect
        float offsetX = (time * 15f) % cellSize;
        float offsetY = (time * 15f) % cellSize;
        
        for (float x = offsetX; x < width; x += cellSize) {
            shapeRenderer.line(x, 0, x, height);
        }
        for (float y = offsetY; y < height; y += cellSize) {
            shapeRenderer.line(0, y, width, y);
        }
        
        shapeRenderer.end();
        Gdx.gl.glDisable(GL20.GL_BLEND);
    }

    @Override
    public void resize(int width, int height) {
        stage.getViewport().update(width, height, true);
    }

    @Override
    public void pause() { }

    @Override
    public void resume() { }

    @Override
    public void hide() { }

    @Override
    public void dispose() {
        if (stage != null) stage.dispose();
        if (skin != null) skin.dispose();
        if (shapeRenderer != null) shapeRenderer.dispose();
    }
}
