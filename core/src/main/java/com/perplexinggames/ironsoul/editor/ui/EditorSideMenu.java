package com.perplexinggames.ironsoul.editor.ui;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.ButtonGroup;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane;
import com.badlogic.gdx.scenes.scene2d.ui.SelectBox;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.ui.TextField;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.perplexinggames.ironsoul.editor.EditorMode;
import com.perplexinggames.ironsoul.editor.LevelEditor;
import com.perplexinggames.ironsoul.editor.tool.EditorToolStrategy;
import com.perplexinggames.ironsoul.editor.tool.EraseBlockTool;
import com.perplexinggames.ironsoul.editor.tool.GateTool;
import com.perplexinggames.ironsoul.editor.tool.PlaceBlockTool;
import com.perplexinggames.ironsoul.editor.tool.SelectBlockTool;
import com.perplexinggames.ironsoul.editor.tool.SpawnPointTool;
import com.perplexinggames.ironsoul.editor.tool.TerrainPointTool;
import com.perplexinggames.ironsoul.editor.tool.WorldMarkerTool;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

public class EditorSideMenu {
    private final LevelEditor levelEditor;
    private final Stage stage;
    private final Skin skin;
    private final ButtonGroup<TextButton> buttonGroup;
    private final List<ToolButtonData> toolDataList;
    private final Table rootTable;

    private final Label modeLabel;
    private final Label toolLabel;
    private final Label hintLabel;
    private final Label validationLabel;
    private final SelectBox<String> blockSelect;
    private final TextField blockIdField;
    private final TextField blockNameField;
    private final TextField blockWidthField;
    private final TextField blockHeightField;
    private final Label selectedGateLabel;
    private final Label selectedSpawnLabel;

    private List<String> lastBlockIds = List.of();
    private boolean menuVisible = true;

    private static class ToolButtonData {
        String id;
        String label;
        String hotkey;
        String hint;
        EditorMode targetMode;
        Supplier<EditorToolStrategy> toolSupplier;
        TextButton uiButton;

        ToolButtonData(String id, String label, String hotkey, String hint, EditorMode targetMode,
                       Supplier<EditorToolStrategy> toolSupplier) {
            this.id = id;
            this.label = label;
            this.hotkey = hotkey;
            this.hint = hint;
            this.targetMode = targetMode;
            this.toolSupplier = toolSupplier;
        }
    }

    public EditorSideMenu(LevelEditor levelEditor, Skin skin) {
        this.levelEditor = levelEditor;
        this.skin = skin;
        this.stage = new Stage(new ScreenViewport());
        this.buttonGroup = new ButtonGroup<>();
        this.buttonGroup.setMaxCheckCount(1);
        this.buttonGroup.setMinCheckCount(0);
        this.buttonGroup.setUncheckLast(true);
        this.toolDataList = new ArrayList<>();
        initToolData();

        rootTable = new Table();
        rootTable.setFillParent(true);
        rootTable.align(Align.left | Align.top);
        stage.addActor(rootTable);

        Table frame = new Table();
        frame.setBackground(skin.newDrawable("white", new Color(0.07f, 0.11f, 0.15f, 0.96f)));
        frame.pad(10f);
        frame.top();

        Table header = new Table();
        header.setBackground(skin.newDrawable("white", new Color(0.12f, 0.18f, 0.24f, 1f)));
        header.pad(8f, 10f, 8f, 10f);

        Label titleLabel = new Label("World Editor", skin, "subtitle");
        titleLabel.setAlignment(Align.left);
        Label subtitleLabel = new Label("Blocks, gates, spawns and world flow", skin);
        subtitleLabel.setColor(new Color(0.75f, 0.84f, 0.92f, 1f));
        subtitleLabel.setFontScale(0.9f);

        Table titleStack = new Table();
        titleStack.add(titleLabel).left().row();
        titleStack.add(subtitleLabel).left().padTop(2f);

        TextButton closeButton = new TextButton("Hide [F3]", skin);
        closeButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                setMenuVisible(false);
            }
        });

        header.add(titleStack).expandX().fillX().left().padRight(8f);
        header.add(closeButton).width(96f).right();
        frame.add(header).expandX().fillX().padBottom(8f).row();

        Table content = new Table();
        content.top();

        Table toolPanel = createSection("Tools");
        int column = 0;
        for (ToolButtonData data : toolDataList) {
            String buttonText = data.label + (data.hotkey != null ? " [" + data.hotkey + "]" : "");
            TextButton button = new TextButton(buttonText, skin, "toggle");
            data.uiButton = button;
            button.addListener(new ChangeListener() {
                @Override
                public void changed(ChangeEvent event, Actor actor) {
                    if (!button.isChecked()) {
                        return;
                    }
                    if (data.targetMode != null) {
                        levelEditor.setMode(data.targetMode);
                    }
                    if (data.toolSupplier != null) {
                        levelEditor.setTool(data.toolSupplier.get());
                    }
                }
            });
            buttonGroup.add(button);
            toolPanel.add(button).width(126f).height(34f).pad(0f, column == 0 ? 0f : 4f, 6f, column == 0 ? 4f : 0f);
            column++;
            if (column == 2) {
                toolPanel.row();
                column = 0;
            }
        }
        if (column != 0) {
            toolPanel.row();
        }
        content.add(toolPanel).expandX().fillX().padBottom(8f).row();

        content.add(buildBlockPanel()).expandX().fillX().padBottom(8f).row();
        content.add(buildGatePanel()).expandX().fillX().padBottom(8f).row();

        Table infoTable = createSection("Session");
        modeLabel = new Label("Mode: -", skin);
        toolLabel = new Label("Tool: -", skin);
        hintLabel = new Label("Hint: -", skin);
        hintLabel.setWrap(true);
        validationLabel = new Label("Validation: -", skin);
        validationLabel.setWrap(true);
        Label footerHint = new Label("F3 hides the menu. F1/F2 switch runtime and editor.", skin);
        footerHint.setWrap(true);
        footerHint.setColor(new Color(0.73f, 0.82f, 0.9f, 1f));

        infoTable.add(modeLabel).align(Align.left).row();
        infoTable.add(toolLabel).align(Align.left).row();
        infoTable.add(hintLabel).align(Align.left).width(234f).padTop(8f).row();
        infoTable.add(validationLabel).align(Align.left).width(234f).padTop(8f).row();
        infoTable.add(footerHint).align(Align.left).width(234f).padTop(10f).row();
        content.add(infoTable).expandX().fillX().row();

        ScrollPane scrollPane = new ScrollPane(content, skin);
        scrollPane.setFadeScrollBars(false);
        scrollPane.setScrollingDisabled(true, false);
        frame.add(scrollPane).width(300f).expand().fill().row();

        rootTable.add(frame).width(320f).expandY().fillY().pad(10f);

        blockSelect = (SelectBox<String>) content.findActor("blockSelect");
        blockIdField = (TextField) content.findActor("blockIdField");
        blockNameField = (TextField) content.findActor("blockNameField");
        blockWidthField = (TextField) content.findActor("blockWidthField");
        blockHeightField = (TextField) content.findActor("blockHeightField");
        selectedGateLabel = (Label) content.findActor("selectedGateLabel");
        selectedSpawnLabel = (Label) content.findActor("selectedSpawnLabel");
    }

    private Table buildBlockPanel() {
        Table panel = createSection("Active Block");

        SelectBox<String> selectBox = new SelectBox<>(skin);
        selectBox.setName("blockSelect");
        selectBox.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                String selected = selectBox.getSelected();
                if (selected != null) {
                    levelEditor.selectActiveBlock(selected);
                }
            }
        });
        panel.add(createFieldLabel("Selected block")).left().padBottom(4f).row();
        panel.add(selectBox).expandX().fillX().padBottom(8f).row();

        TextField idField = new TextField("", skin);
        idField.setMessageText("block-id");
        idField.setName("blockIdField");
        panel.add(createFieldLabel("Block id")).left().padBottom(3f).row();
        panel.add(idField).expandX().fillX().padBottom(4f).row();

        TextField nameField = new TextField("", skin);
        nameField.setMessageText("Block name");
        nameField.setName("blockNameField");
        panel.add(createFieldLabel("Display name")).left().padBottom(3f).row();
        panel.add(nameField).expandX().fillX().padBottom(6f).row();

        Table sizeTable = new Table();
        TextField widthField = new TextField("", skin);
        widthField.setName("blockWidthField");
        widthField.setMessageText("width");
        TextField heightField = new TextField("", skin);
        heightField.setName("blockHeightField");
        heightField.setMessageText("height");
        sizeTable.add(widthField).width(114f).padRight(6f);
        sizeTable.add(heightField).width(114f);
        panel.add(createFieldLabel("Block size")).left().padBottom(3f).row();
        panel.add(sizeTable).left().padBottom(6f).row();

        Table buttonTable = new Table();
        TextButton createButton = new TextButton("Create", skin);
        createButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                levelEditor.createBlock(idField.getText(), nameField.getText(), parseInt(widthField.getText(), 20),
                    parseInt(heightField.getText(), 15));
            }
        });
        TextButton applyButton = new TextButton("Apply", skin);
        applyButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                levelEditor.updateActiveBlockProperties(idField.getText(), nameField.getText(),
                    parseInt(widthField.getText(), 20), parseInt(heightField.getText(), 15));
            }
        });
        buttonTable.add(createButton).width(114f).padRight(6f);
        buttonTable.add(applyButton).width(114f);
        panel.add(buttonTable).left();

        return panel;
    }

    private Table buildGatePanel() {
        Table panel = createSection("Gate Setup");

        Label gateLabel = new Label("Gate: none", skin);
        gateLabel.setName("selectedGateLabel");
        gateLabel.setWrap(true);
        panel.add(createFieldLabel("Selected gate")).left().padBottom(3f).row();
        panel.add(gateLabel).width(234f).left().padBottom(6f).row();

        Label spawnLabel = new Label("Spawn: none", skin);
        spawnLabel.setName("selectedSpawnLabel");
        spawnLabel.setWrap(true);
        panel.add(createFieldLabel("Selected spawn")).left().padBottom(3f).row();
        panel.add(spawnLabel).width(234f).left().padBottom(8f).row();

        Table row1 = new Table();
        TextButton typeButton = new TextButton("Toggle Type", skin);
        typeButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                levelEditor.cycleSelectedGateTransitionType();
            }
        });
        TextButton stateButton = new TextButton("Toggle State", skin);
        stateButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                levelEditor.cycleSelectedGateState();
            }
        });
        row1.add(typeButton).width(114f).padRight(6f);
        row1.add(stateButton).width(114f);
        panel.add(row1).left().padBottom(4f).row();

        Table row2 = new Table();
        TextButton targetButton = new TextButton("Next Target", skin);
        targetButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                levelEditor.cycleSelectedGateTargetBlock();
            }
        });
        TextButton spawnButton = new TextButton("Next Spawn", skin);
        spawnButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                levelEditor.cycleSelectedGateTargetSpawnPoint();
            }
        });
        row2.add(targetButton).width(114f).padRight(6f);
        row2.add(spawnButton).width(114f);
        panel.add(row2).left();

        return panel;
    }

    private void initToolData() {
        toolDataList.add(new ToolButtonData("place", "Place", "1", "Click to place a solid tile.", EditorMode.EDITOR, PlaceBlockTool::new));
        toolDataList.add(new ToolButtonData("erase", "Delete", "2", "Click to erase solid tiles.", EditorMode.EDITOR, EraseBlockTool::new));
        toolDataList.add(new ToolButtonData("select", "Select", "3", "Click to select a solid tile.", EditorMode.EDITOR, SelectBlockTool::new));
        toolDataList.add(new ToolButtonData("terrain", "Terrain", "4", "LMB add/move point, RMB remove.", EditorMode.EDITOR, TerrainPointTool::new));
        toolDataList.add(new ToolButtonData("gate", "Gate", "5", "Click to create/select a gate.", EditorMode.EDITOR, GateTool::new));
        toolDataList.add(new ToolButtonData("spawn", "Spawn", "6", "Click to create or move a spawn point.", EditorMode.EDITOR, SpawnPointTool::new));
        toolDataList.add(new ToolButtonData("object", "Object", "7", "Click to place a generic world object.", EditorMode.EDITOR,
            () -> new WorldMarkerTool("OBJECT", LevelEditor.MarkerLayer.OBJECT)));
        toolDataList.add(new ToolButtonData("enemy", "Enemy", "8", "Click to place an enemy marker.", EditorMode.EDITOR,
            () -> new WorldMarkerTool("ENEMY", LevelEditor.MarkerLayer.ENEMY)));
        toolDataList.add(new ToolButtonData("reward", "Reward", "9", "Click to place a reward marker.", EditorMode.EDITOR,
            () -> new WorldMarkerTool("REWARD", LevelEditor.MarkerLayer.REWARD)));
        toolDataList.add(new ToolButtonData("trigger", "Trigger", "0", "Click to place a trigger marker.", EditorMode.EDITOR,
            () -> new WorldMarkerTool("TRIGGER", LevelEditor.MarkerLayer.TRIGGER)));
        toolDataList.add(new ToolButtonData("playtest", "Play Test", "F1", "Runtime world streaming and gates.", EditorMode.GAMEPLAY, null));
    }

    public void act(float delta) {
        stage.act(delta);
        if (menuVisible) {
            syncWithController();
        }
    }

    public void draw() {
        if (menuVisible) {
            stage.draw();
        }
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

    public boolean isMenuVisible() {
        return menuVisible;
    }

    public void toggleVisibility() {
        setMenuVisible(!menuVisible);
    }

    public void setMenuVisible(boolean menuVisible) {
        this.menuVisible = menuVisible;
        rootTable.setVisible(menuVisible);
        rootTable.setTouchable(menuVisible ? com.badlogic.gdx.scenes.scene2d.Touchable.enabled
            : com.badlogic.gdx.scenes.scene2d.Touchable.disabled);
        if (!menuVisible) {
            stage.unfocusAll();
        } else {
            syncWithController();
        }
    }

    private void syncWithController() {
        EditorMode currentMode = levelEditor.getMode();
        String currentTool = levelEditor.getCurrentToolName();

        modeLabel.setText("Mode: " + currentMode.name());
        toolLabel.setText("Tool: " + (currentMode == EditorMode.GAMEPLAY ? "None" : currentTool));
        validationLabel.setText(levelEditor.getValidationSummary());

        ToolButtonData activeData = null;
        for (ToolButtonData data : toolDataList) {
            boolean shouldBeChecked = false;
            if (currentMode == EditorMode.GAMEPLAY) {
                shouldBeChecked = "playtest".equals(data.id);
            } else if (data.toolSupplier != null) {
                shouldBeChecked = data.toolSupplier.get().getName().equals(currentTool);
            }
            if (data.uiButton.isChecked() != shouldBeChecked) {
                data.uiButton.setChecked(shouldBeChecked);
            }
            if (shouldBeChecked) {
                activeData = data;
            }
        }

        hintLabel.setText(activeData == null ? "Hint: Select a tool." : "Hint: " + activeData.hint);
        syncBlockEditor();
        syncGateEditor();
    }

    private void syncBlockEditor() {
        List<String> blockIds = levelEditor.getBlockIds();
        if (!blockIds.equals(lastBlockIds)) {
            lastBlockIds = new ArrayList<>(blockIds);
            blockSelect.setItems(new Array<>(blockIds.toArray(new String[0])));
        }
        if (levelEditor.getActiveBlockId() != null && blockSelect.getItems().size > 0) {
            blockSelect.setSelected(levelEditor.getActiveBlockId());
        }

        if (levelEditor.getActiveBlock() == null) {
            return;
        }
        if (stage.getKeyboardFocus() != blockIdField) {
            blockIdField.setText(levelEditor.getActiveBlock().id);
        }
        if (stage.getKeyboardFocus() != blockNameField) {
            blockNameField.setText(levelEditor.getActiveBlock().name);
        }
        if (stage.getKeyboardFocus() != blockWidthField) {
            blockWidthField.setText(String.valueOf(levelEditor.getActiveBlock().width));
        }
        if (stage.getKeyboardFocus() != blockHeightField) {
            blockHeightField.setText(String.valueOf(levelEditor.getActiveBlock().height));
        }
    }

    private void syncGateEditor() {
        if (levelEditor.getSelectedGate() == null) {
            selectedGateLabel.setText("Gate: none");
        } else {
            selectedGateLabel.setText(
                "Gate: " + levelEditor.getSelectedGate().id
                    + " | " + levelEditor.getSelectedGate().transitionType
                    + " | " + levelEditor.getSelectedGate().state
            );
        }
        if (levelEditor.getSelectedSpawnPoint() == null) {
            selectedSpawnLabel.setText("Spawn: none");
        } else {
            selectedSpawnLabel.setText("Spawn: " + levelEditor.getSelectedSpawnPoint().id);
        }
    }

    private int parseInt(String text, int fallback) {
        try {
            return Integer.parseInt(text.trim());
        } catch (Exception exception) {
            return fallback;
        }
    }

    private Table createSection(String title) {
        Table panel = new Table();
        panel.setBackground(skin.newDrawable("white", new Color(0.10f, 0.14f, 0.19f, 0.98f)));
        panel.pad(10f);

        Label titleLabel = new Label(title, skin);
        titleLabel.setColor(new Color(0.96f, 0.98f, 1f, 1f));
        panel.add(titleLabel).left().padBottom(8f).row();
        return panel;
    }

    private Label createFieldLabel(String text) {
        Label label = new Label(text, skin);
        label.setColor(new Color(0.72f, 0.82f, 0.9f, 1f));
        return label;
    }
}
