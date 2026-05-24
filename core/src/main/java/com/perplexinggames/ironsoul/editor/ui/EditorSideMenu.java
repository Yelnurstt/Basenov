package com.perplexinggames.ironsoul.editor.ui;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.ButtonGroup;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.perplexinggames.ironsoul.editor.EditorMode;
import com.perplexinggames.ironsoul.editor.tool.EditorToolStrategy;
import com.perplexinggames.ironsoul.editor.tool.EraseBlockTool;
import com.perplexinggames.ironsoul.editor.tool.PlaceBlockTool;
import com.perplexinggames.ironsoul.editor.tool.SelectBlockTool;
import com.perplexinggames.ironsoul.editor.tool.TerrainPointTool;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

public class EditorSideMenu {
    private final EditorToolController controller;
    private final Stage stage;
    private final Skin skin;

    private final ButtonGroup<TextButton> buttonGroup;
    private final Label modeLabel;
    private final Label toolLabel;
    private final Label hintLabel;

    private final List<ToolButtonData> toolDataList;

    private static class ToolButtonData {
        String id;
        String label;
        String hotkey;
        String hint;
        EditorMode targetMode;
        Supplier<EditorToolStrategy> toolSupplier;
        boolean enabled;
        TextButton uiButton;

        public ToolButtonData(String id, String label, String hotkey, String hint, EditorMode targetMode, Supplier<EditorToolStrategy> toolSupplier, boolean enabled) {
            this.id = id;
            this.label = label;
            this.hotkey = hotkey;
            this.hint = hint;
            this.targetMode = targetMode;
            this.toolSupplier = toolSupplier;
            this.enabled = enabled;
        }
    }

    public EditorSideMenu(EditorToolController controller, Skin skin) {
        this.controller = controller;
        this.skin = skin;
        this.stage = new Stage(new ScreenViewport());
        
        this.buttonGroup = new ButtonGroup<>();
        this.buttonGroup.setMaxCheckCount(1);
        this.buttonGroup.setMinCheckCount(0); // Allow nothing selected if in gameplay mode
        this.buttonGroup.setUncheckLast(true);

        this.toolDataList = new ArrayList<>();
        initToolData();

        Table rootTable = new Table();
        rootTable.setFillParent(true);
        rootTable.align(Align.left | Align.top);
        stage.addActor(rootTable);

        // Menu Background Panel
        Table panel = new Table();
        panel.setBackground(skin.newDrawable("white", new Color(0.1f, 0.15f, 0.2f, 0.85f)));
        panel.pad(10);
        panel.top();
        
        Label titleLabel = new Label("World Editor", skin, "subtitle");
        titleLabel.setAlignment(Align.center);
        panel.add(titleLabel).expandX().fillX().padBottom(20).row();

        // Buttons
        for (ToolButtonData data : toolDataList) {
            String buttonText = data.label + (data.hotkey != null ? " [" + data.hotkey + "]" : "");
            TextButton button = new TextButton(buttonText, skin, "toggle");
            data.uiButton = button;
            
            if (!data.enabled) {
                button.setDisabled(true);
                button.setColor(Color.DARK_GRAY);
                button.setText(data.label + " (WIP)");
            }

            button.addListener(new ChangeListener() {
                @Override
                public void changed(ChangeEvent event, Actor actor) {
                    if (button.isChecked() && data.enabled) {
                        if (data.targetMode != null) {
                            controller.setMode(data.targetMode);
                        }
                        if (data.toolSupplier != null) {
                            controller.setTool(data.toolSupplier.get());
                        }
                    }
                }
            });

            buttonGroup.add(button);
            panel.add(button).fillX().height(35).padBottom(5).row();
        }

        // Info Section
        Table infoTable = new Table();
        infoTable.setBackground(skin.newDrawable("white", new Color(0.05f, 0.05f, 0.1f, 0.8f)));
        infoTable.pad(10);
        
        modeLabel = new Label("Mode: -", skin);
        toolLabel = new Label("Tool: -", skin);
        hintLabel = new Label("Hint: -", skin);
        hintLabel.setWrap(true);

        infoTable.add(modeLabel).align(Align.left).row();
        infoTable.add(toolLabel).align(Align.left).row();
        infoTable.add(hintLabel).align(Align.left).width(200).padTop(10).row();

        panel.add(infoTable).expand().bottom().fillX();

        rootTable.add(panel).width(220).expandY().fillY();
    }

    private void initToolData() {
        toolDataList.add(new ToolButtonData("place", "Place", "1", "Click to place a block, drag to paint.", EditorMode.EDITOR, PlaceBlockTool::new, true));
        toolDataList.add(new ToolButtonData("erase", "Delete", "2", "Click to erase a block, drag to erase multiple.", EditorMode.EDITOR, EraseBlockTool::new, true));
        toolDataList.add(new ToolButtonData("select", "Select", "3", "Click to select a block.", EditorMode.EDITOR, SelectBlockTool::new, true));
        toolDataList.add(new ToolButtonData("terrain", "Terrain", "4", "LMB add/move point, RMB remove.", EditorMode.EDITOR, TerrainPointTool::new, true));
        
        toolDataList.add(new ToolButtonData("move", "Move", null, "Not implemented yet.", EditorMode.EDITOR, null, false));
        toolDataList.add(new ToolButtonData("gate", "Gate", null, "Not implemented yet.", EditorMode.EDITOR, null, false));
        toolDataList.add(new ToolButtonData("enemy", "Enemy", null, "Not implemented yet.", EditorMode.EDITOR, null, false));
        toolDataList.add(new ToolButtonData("trigger", "Trigger", null, "Not implemented yet.", EditorMode.EDITOR, null, false));
        toolDataList.add(new ToolButtonData("spawn", "Spawn Point", null, "Not implemented yet.", EditorMode.EDITOR, null, false));
        toolDataList.add(new ToolButtonData("reward", "Reward", null, "Not implemented yet.", EditorMode.EDITOR, null, false));
        
        toolDataList.add(new ToolButtonData("playtest", "Play Test", "F1", "Test your level. Drive with A/D, Aim with Mouse.", EditorMode.GAMEPLAY, null, true));
    }

    public void act(float delta) {
        stage.act(delta);
        syncWithController();
    }

    public void draw() {
        stage.draw();
    }

    public void resize(int width, int height) {
        stage.getViewport().update(width, height, true);
    }

    public void dispose() {
        stage.dispose();
    }

    public Stage getStage() {
        return stage;
    }

    private void syncWithController() {
        EditorMode currentMode = controller.getMode();
        String currentTool = controller.getCurrentToolName();

        modeLabel.setText("Mode: " + currentMode.name());
        toolLabel.setText("Tool: " + (currentMode == EditorMode.GAMEPLAY ? "None" : currentTool));

        ToolButtonData activeData = null;

        for (ToolButtonData data : toolDataList) {
            boolean shouldBeChecked = false;
            
            if (currentMode == EditorMode.GAMEPLAY) {
                if (data.id.equals("playtest")) {
                    shouldBeChecked = true;
                    activeData = data;
                }
            } else {
                if (data.toolSupplier != null) {
                    // Check if current tool matches by name
                    EditorToolStrategy tempTool = data.toolSupplier.get();
                    if (tempTool.getName().equals(currentTool)) {
                        shouldBeChecked = true;
                        activeData = data;
                    }
                }
            }

            if (data.uiButton.isChecked() != shouldBeChecked) {
                data.uiButton.setChecked(shouldBeChecked);
            }
        }

        if (activeData != null) {
            hintLabel.setText("Hint: " + activeData.hint);
        } else {
            hintLabel.setText("Hint: Select a tool.");
        }
    }
}
