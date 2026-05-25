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
import com.perplexinggames.ironsoul.editor.command.AddSplineLayerCommand;
import com.perplexinggames.ironsoul.editor.command.CreateSplinePathCommand;
import com.perplexinggames.ironsoul.editor.command.DeleteSplinePathCommand;
import com.perplexinggames.ironsoul.editor.command.RemoveSplineLayerCommand;
import com.perplexinggames.ironsoul.editor.command.UpdateSplineLayerPropertiesCommand;
import com.perplexinggames.ironsoul.editor.command.UpdateSplinePathPropertiesCommand;
import com.perplexinggames.ironsoul.editor.tool.EditorToolStrategy;
import com.perplexinggames.ironsoul.editor.tool.EraseBlockTool;
import com.perplexinggames.ironsoul.editor.tool.GateTool;
import com.perplexinggames.ironsoul.editor.tool.PlaceBlockTool;
import com.perplexinggames.ironsoul.editor.tool.SelectBlockTool;
import com.perplexinggames.ironsoul.editor.tool.SplineEditTool;
import com.perplexinggames.ironsoul.editor.tool.SplinePenTool;
import com.perplexinggames.ironsoul.editor.tool.SpawnPointTool;
import com.perplexinggames.ironsoul.editor.tool.WorldMarkerTool;
import com.perplexinggames.ironsoul.terrain.spline.SplineCurveType;
import com.perplexinggames.ironsoul.terrain.spline.SplineLayer;
import com.perplexinggames.ironsoul.terrain.spline.SplinePath;
import com.perplexinggames.ironsoul.terrain.spline.SplineTileMode;

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
    private final SelectBox<String> splineSelect;
    private final SelectBox<String> splineLayerSelect;
    private final SelectBox<String> splineCurveSelect;
    private final SelectBox<String> splineTileModeSelect;
    private final TextField splineNameField;
    private final TextField splineSpriteField;
    private final TextField splineDepthField;
    private final TextField splineParallaxField;
    private final TextField splineWidthField;
    private final TextField splineOffsetField;
    private final TextField splineThicknessField;
    private final Label splineSelectionLabel;

    private List<String> lastBlockIds = List.of();
    private List<String> lastSplineIds = List.of();
    private List<String> lastSplineLayerIds = List.of();
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

        // --- Top Toolbar ---
        Table topToolbar = new Table();
        topToolbar.setBackground(skin.newDrawable("white", new Color(0.12f, 0.18f, 0.24f, 1f)));
        topToolbar.pad(5f);
        
        Label titleLabel = new Label("Iron Soul Editor", skin, "subtitle");
        topToolbar.add(titleLabel).left().padRight(20f);

        TextButton saveBtn = new TextButton("Save", skin);
        saveBtn.addListener(new ChangeListener() { @Override public void changed(ChangeEvent e, Actor a) { levelEditor.executeCommand(new com.perplexinggames.ironsoul.editor.command.SaveLevelCommand(levelEditor)); } });
        TextButton loadBtn = new TextButton("Load", skin);
        loadBtn.addListener(new ChangeListener() { @Override public void changed(ChangeEvent e, Actor a) { levelEditor.executeCommand(new com.perplexinggames.ironsoul.editor.command.LoadLevelCommand(levelEditor)); } });
        TextButton playBtn = new TextButton("Play [F1]", skin);
        playBtn.addListener(new ChangeListener() { @Override public void changed(ChangeEvent e, Actor a) { levelEditor.setMode(EditorMode.GAMEPLAY); } });
        TextButton stopBtn = new TextButton("Stop [F2]", skin);
        stopBtn.addListener(new ChangeListener() { @Override public void changed(ChangeEvent e, Actor a) { levelEditor.setMode(EditorMode.EDITOR); } });
        TextButton snapBtn = new TextButton("Grid Snap", skin, "toggle");
        snapBtn.addListener(new ChangeListener() { @Override public void changed(ChangeEvent e, Actor a) { levelEditor.getSnapService().setEnabled(snapBtn.isChecked()); } });

        topToolbar.add(saveBtn).padRight(5f);
        topToolbar.add(loadBtn).padRight(15f);
        topToolbar.add(playBtn).padRight(5f);
        topToolbar.add(stopBtn).padRight(15f);
        topToolbar.add(snapBtn).padRight(5f);
        topToolbar.add().expandX();
        
        TextButton closeButton = new TextButton("Hide UI [F3]", skin);
        closeButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                setMenuVisible(false);
            }
        });
        topToolbar.add(closeButton).right();

        rootTable.add(topToolbar).expandX().fillX().colspan(3).row();

        // --- Left Tool Palette ---
        Table leftPalette = new Table();
        leftPalette.setBackground(skin.newDrawable("white", new Color(0.07f, 0.11f, 0.15f, 0.96f)));
        leftPalette.top().pad(10f);

        Label toolTitle = new Label("Tools", skin, "subtitle");
        leftPalette.add(toolTitle).padBottom(10f).row();

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
            leftPalette.add(button).width(120f).height(34f).padBottom(4f).row();
        }
        rootTable.add(leftPalette).width(140f).expandY().fillY();

        // --- Center Viewport (Empty) ---
        Table centerViewport = new Table();
        rootTable.add(centerViewport).expand().fill();

        // --- Right Inspector ---
        Table rightInspector = new Table();
        rightInspector.setBackground(skin.newDrawable("white", new Color(0.07f, 0.11f, 0.15f, 0.96f)));
        rightInspector.top().pad(10f);

        Table inspectorContent = new Table();
        inspectorContent.top();

        inspectorContent.add(buildBlockPanel()).expandX().fillX().padBottom(8f).row();
        inspectorContent.add(buildSplinePanel()).expandX().fillX().padBottom(8f).row();
        inspectorContent.add(buildGatePanel()).expandX().fillX().padBottom(8f).row();

        ScrollPane inspectorScroll = new ScrollPane(inspectorContent, skin);
        inspectorScroll.setFadeScrollBars(false);
        inspectorScroll.setScrollingDisabled(true, false);
        rightInspector.add(inspectorScroll).expand().fill();

        rootTable.add(rightInspector).width(320f).expandY().fillY().row();

        // --- Bottom Status Bar ---
        Table bottomStatusBar = new Table();
        bottomStatusBar.setBackground(skin.newDrawable("white", new Color(0.12f, 0.18f, 0.24f, 1f)));
        bottomStatusBar.pad(5f, 10f, 5f, 10f);

        modeLabel = new Label("Mode: -", skin);
        toolLabel = new Label("Tool: -", skin);
        hintLabel = new Label("Hint: -", skin);
        validationLabel = new Label("Validation: -", skin);

        bottomStatusBar.add(modeLabel).padRight(15f);
        bottomStatusBar.add(toolLabel).padRight(15f);
        bottomStatusBar.add(hintLabel).expandX().left();
        bottomStatusBar.add(validationLabel).right();

        rootTable.add(bottomStatusBar).expandX().fillX().colspan(3);

        blockSelect = (SelectBox<String>) inspectorContent.findActor("blockSelect");
        blockIdField = (TextField) inspectorContent.findActor("blockIdField");
        blockNameField = (TextField) inspectorContent.findActor("blockNameField");
        blockWidthField = (TextField) inspectorContent.findActor("blockWidthField");
        blockHeightField = (TextField) inspectorContent.findActor("blockHeightField");
        splineSelect = (SelectBox<String>) inspectorContent.findActor("splineSelect");
        splineLayerSelect = (SelectBox<String>) inspectorContent.findActor("splineLayerSelect");
        splineCurveSelect = (SelectBox<String>) inspectorContent.findActor("splineCurveSelect");
        splineTileModeSelect = (SelectBox<String>) inspectorContent.findActor("splineTileModeSelect");
        splineNameField = (TextField) inspectorContent.findActor("splineNameField");
        splineSpriteField = (TextField) inspectorContent.findActor("splineSpriteField");
        splineDepthField = (TextField) inspectorContent.findActor("splineDepthField");
        splineParallaxField = (TextField) inspectorContent.findActor("splineParallaxField");
        splineWidthField = (TextField) inspectorContent.findActor("splineWidthField");
        splineOffsetField = (TextField) inspectorContent.findActor("splineOffsetField");
        splineThicknessField = (TextField) inspectorContent.findActor("splineThicknessField");
        splineSelectionLabel = (Label) inspectorContent.findActor("splineSelectionLabel");
        selectedGateLabel = (Label) inspectorContent.findActor("selectedGateLabel");
        selectedSpawnLabel = (Label) inspectorContent.findActor("selectedSpawnLabel");
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

    private Table buildSplinePanel() {
        Table panel = createSection("Spline Terrain");

        Label selectionLabel = new Label("Spline: none", skin);
        selectionLabel.setName("splineSelectionLabel");
        selectionLabel.setWrap(true);
        panel.add(selectionLabel).width(234f).left().padBottom(6f).row();

        SelectBox<String> splineBox = new SelectBox<>(skin);
        splineBox.setName("splineSelect");
        splineBox.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                String selected = splineBox.getSelected();
                if (selected != null && !"none".equals(selected)) {
                    levelEditor.selectSplinePath(selected);
                }
            }
        });
        panel.add(createFieldLabel("Spline path")).left().padBottom(3f).row();
        panel.add(splineBox).expandX().fillX().padBottom(6f).row();

        SelectBox<String> layerBox = new SelectBox<>(skin);
        layerBox.setName("splineLayerSelect");
        layerBox.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                String selected = layerBox.getSelected();
                if (selected != null && !"none".equals(selected)) {
                    levelEditor.selectSplineLayer(selected);
                }
            }
        });
        panel.add(createFieldLabel("Spline layer")).left().padBottom(3f).row();
        panel.add(layerBox).expandX().fillX().padBottom(6f).row();

        TextField nameField = new TextField("", skin);
        nameField.setName("splineNameField");
        nameField.setMessageText("Spline name");
        panel.add(createFieldLabel("Spline name")).left().padBottom(3f).row();
        panel.add(nameField).expandX().fillX().padBottom(6f).row();

        SelectBox<String> curveBox = new SelectBox<>(skin);
        curveBox.setName("splineCurveSelect");
        curveBox.setItems("LINEAR", "CATMULL_ROM", "BEZIER");
        panel.add(createFieldLabel("Curve type")).left().padBottom(3f).row();
        panel.add(curveBox).expandX().fillX().padBottom(6f).row();

        TextField thicknessField = new TextField("", skin);
        thicknessField.setName("splineThicknessField");
        thicknessField.setMessageText("collision width");
        panel.add(createFieldLabel("Collision thickness")).left().padBottom(3f).row();
        panel.add(thicknessField).expandX().fillX().padBottom(6f).row();

        TextField spriteField = new TextField("", skin);
        spriteField.setName("splineSpriteField");
        spriteField.setMessageText("assets path");
        panel.add(createFieldLabel("Layer sprite")).left().padBottom(3f).row();
        panel.add(spriteField).expandX().fillX().padBottom(6f).row();

        TextField depthField = new TextField("", skin);
        depthField.setName("splineDepthField");
        depthField.setMessageText("0");
        TextField parallaxField = new TextField("", skin);
        parallaxField.setName("splineParallaxField");
        parallaxField.setMessageText("1.0");
        Table row1 = new Table();
        row1.add(depthField).width(114f).padRight(6f);
        row1.add(parallaxField).width(114f);
        panel.add(createFieldLabel("Depth / parallax")).left().padBottom(3f).row();
        panel.add(row1).left().padBottom(6f).row();

        TextField widthField = new TextField("", skin);
        widthField.setName("splineWidthField");
        widthField.setMessageText("width");
        TextField offsetField = new TextField("", skin);
        offsetField.setName("splineOffsetField");
        offsetField.setMessageText("offset");
        Table row2 = new Table();
        row2.add(widthField).width(114f).padRight(6f);
        row2.add(offsetField).width(114f);
        panel.add(createFieldLabel("Width / offset")).left().padBottom(3f).row();
        panel.add(row2).left().padBottom(6f).row();

        SelectBox<String> tileModeBox = new SelectBox<>(skin);
        tileModeBox.setName("splineTileModeSelect");
        tileModeBox.setItems("STRETCH", "REPEAT");
        panel.add(createFieldLabel("Tile mode")).left().padBottom(3f).row();
        panel.add(tileModeBox).expandX().fillX().padBottom(6f).row();

        Table actionRow1 = new Table();
        TextButton createButton = new TextButton("Create Path", skin);
        createButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                String pathId = levelEditor.createSplinePathId();
                String layerId = levelEditor.createSplineLayerId();
                SplinePath path = levelEditor.createDefaultSplinePath(pathId, "Spline " + pathId);
                SplineLayer layer = levelEditor.createDefaultSplineLayer(layerId, pathId, "Main Terrain", 0);
                levelEditor.executeCommand(new CreateSplinePathCommand(levelEditor, path, layer));
            }
        });
        TextButton deleteButton = new TextButton("Delete Path", skin);
        deleteButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                if (levelEditor.getSelectedSplinePathId() != null) {
                    levelEditor.executeCommand(new DeleteSplinePathCommand(levelEditor, levelEditor.getSelectedSplinePathId()));
                }
            }
        });
        actionRow1.add(createButton).width(114f).padRight(6f);
        actionRow1.add(deleteButton).width(114f);
        panel.add(actionRow1).left().padBottom(4f).row();

        Table actionRow2 = new Table();
        TextButton addLayerButton = new TextButton("Add Layer", skin);
        addLayerButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                if (levelEditor.getSelectedSplinePathId() == null) {
                    return;
                }
                String layerId = levelEditor.createSplineLayerId();
                int nextDepth = levelEditor.getSplineLayersForPath(levelEditor.getSelectedSplinePathId()).size();
                SplineLayer layer = levelEditor.createDefaultSplineLayer(layerId, levelEditor.getSelectedSplinePathId(),
                    "Layer " + layerId, nextDepth);
                levelEditor.executeCommand(new AddSplineLayerCommand(levelEditor, layer));
            }
        });
        TextButton removeLayerButton = new TextButton("Remove Layer", skin);
        removeLayerButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                if (levelEditor.getSelectedSplineLayerId() != null) {
                    levelEditor.executeCommand(new RemoveSplineLayerCommand(levelEditor, levelEditor.getSelectedSplineLayerId()));
                }
            }
        });
        actionRow2.add(addLayerButton).width(114f).padRight(6f);
        actionRow2.add(removeLayerButton).width(114f);
        panel.add(actionRow2).left().padBottom(4f).row();

        Table actionRow3 = new Table();
        TextButton applyPathButton = new TextButton("Apply Path", skin);
        applyPathButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                if (levelEditor.getSelectedSplinePathId() == null) {
                    return;
                }
                SplinePath updated = levelEditor.updateSplinePathProperties(
                    levelEditor.getSelectedSplinePathId(),
                    nameField.getText(),
                    SplineCurveType.valueOf(curveBox.getSelected()),
                    levelEditor.getSelectedSplinePath() != null && levelEditor.getSelectedSplinePath().closed,
                    levelEditor.getSelectedSplinePath() == null || levelEditor.getSelectedSplinePath().collisionEnabled,
                    parseFloat(thicknessField.getText(), 4f),
                    "editor-dirt"
                );
                if (updated != null) {
                    levelEditor.executeCommand(new UpdateSplinePathPropertiesCommand(levelEditor, updated));
                }
            }
        });
        TextButton applyLayerButton = new TextButton("Apply Layer", skin);
        applyLayerButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                if (levelEditor.getSelectedSplineLayerId() == null) {
                    return;
                }
                SplineLayer updated = levelEditor.updateSplineLayerProperties(
                    levelEditor.getSelectedSplineLayerId(),
                    spriteField.getText(),
                    parseInt(depthField.getText(), 0),
                    parseFloat(parallaxField.getText(), 1f),
                    parseFloat(offsetField.getText(), 0f),
                    parseFloat(widthField.getText(), 32f),
                    SplineTileMode.valueOf(tileModeBox.getSelected()),
                    true,
                    levelEditor.getSelectedSplineLayer() != null && levelEditor.getSelectedSplineLayer().collisionEnabled
                );
                if (updated != null) {
                    levelEditor.executeCommand(new UpdateSplineLayerPropertiesCommand(levelEditor, updated));
                }
            }
        });
        actionRow3.add(applyPathButton).width(114f).padRight(6f);
        actionRow3.add(applyLayerButton).width(114f);
        panel.add(actionRow3).left().padBottom(4f).row();

        Table actionRow4 = new Table();
        TextButton toggleClosedButton = new TextButton("Toggle Closed", skin);
        toggleClosedButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                SplinePath selectedPath = levelEditor.getSelectedSplinePath();
                if (selectedPath == null) {
                    return;
                }
                SplinePath updated = selectedPath.copy();
                updated.closed = !updated.closed;
                levelEditor.executeCommand(new UpdateSplinePathPropertiesCommand(levelEditor, updated));
            }
        });
        TextButton toggleVisibleButton = new TextButton("Toggle Visible", skin);
        toggleVisibleButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                SplineLayer selectedLayer = levelEditor.getSelectedSplineLayer();
                if (selectedLayer == null) {
                    return;
                }
                SplineLayer updated = selectedLayer.copy();
                updated.visible = !updated.visible;
                levelEditor.executeCommand(new UpdateSplineLayerPropertiesCommand(levelEditor, updated));
            }
        });
        actionRow4.add(toggleClosedButton).width(114f).padRight(6f);
        actionRow4.add(toggleVisibleButton).width(114f);
        panel.add(actionRow4).left().padBottom(4f).row();

        Table actionRow5 = new Table();
        TextButton togglePathCollisionButton = new TextButton("Path Collision", skin);
        togglePathCollisionButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                SplinePath selectedPath = levelEditor.getSelectedSplinePath();
                if (selectedPath == null) {
                    return;
                }
                SplinePath updated = selectedPath.copy();
                updated.collisionEnabled = !updated.collisionEnabled;
                levelEditor.executeCommand(new UpdateSplinePathPropertiesCommand(levelEditor, updated));
            }
        });
        TextButton toggleLayerCollisionButton = new TextButton("Layer Collision", skin);
        toggleLayerCollisionButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                SplineLayer selectedLayer = levelEditor.getSelectedSplineLayer();
                if (selectedLayer == null) {
                    return;
                }
                SplineLayer updated = selectedLayer.copy();
                updated.collisionEnabled = !updated.collisionEnabled;
                levelEditor.executeCommand(new UpdateSplineLayerPropertiesCommand(levelEditor, updated));
            }
        });
        actionRow5.add(togglePathCollisionButton).width(114f).padRight(6f);
        actionRow5.add(toggleLayerCollisionButton).width(114f);
        panel.add(actionRow5).left();

        return panel;
    }

    private void initToolData() {
        toolDataList.add(new ToolButtonData("place", "Place", "1", "Click to place a solid tile.", EditorMode.EDITOR, PlaceBlockTool::new));
        toolDataList.add(new ToolButtonData("erase", "Delete", "2", "Click to erase solid tiles.", EditorMode.EDITOR, EraseBlockTool::new));
        toolDataList.add(new ToolButtonData("select", "Select", "3", "Click to select a solid tile.", EditorMode.EDITOR, SelectBlockTool::new));
        toolDataList.add(new ToolButtonData("splinePen", "Spline Pen", "4", "LMB add control points. RMB deletes selected point.", EditorMode.EDITOR, SplinePenTool::new));
        toolDataList.add(new ToolButtonData("splineEdit", "Spline Edit", "Q", "Drag spline control points to reshape all layers.", EditorMode.EDITOR, SplineEditTool::new));
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
        syncSplineEditor();
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

    private void syncSplineEditor() {
        List<String> splineIds = new ArrayList<>();
        for (SplinePath splinePath : levelEditor.getSplinePaths()) {
            splineIds.add(splinePath.id);
        }
        if (splineIds.isEmpty()) {
            splineIds = List.of("none");
        }
        if (!splineIds.equals(lastSplineIds)) {
            lastSplineIds = new ArrayList<>(splineIds);
            splineSelect.setItems(new Array<>(splineIds.toArray(new String[0])));
        }

        if (levelEditor.getSelectedSplinePathId() != null && splineSelect.getItems().contains(levelEditor.getSelectedSplinePathId(), false)) {
            splineSelect.setSelected(levelEditor.getSelectedSplinePathId());
        }

        List<String> layerIds = new ArrayList<>();
        if (levelEditor.getSelectedSplinePathId() != null) {
            for (SplineLayer layer : levelEditor.getSplineLayersForPath(levelEditor.getSelectedSplinePathId())) {
                layerIds.add(layer.id);
            }
        }
        if (layerIds.isEmpty()) {
            layerIds = List.of("none");
        }
        if (!layerIds.equals(lastSplineLayerIds)) {
            lastSplineLayerIds = new ArrayList<>(layerIds);
            splineLayerSelect.setItems(new Array<>(layerIds.toArray(new String[0])));
        }
        if (levelEditor.getSelectedSplineLayerId() != null && splineLayerSelect.getItems().contains(levelEditor.getSelectedSplineLayerId(), false)) {
            splineLayerSelect.setSelected(levelEditor.getSelectedSplineLayerId());
        }

        SplinePath selectedPath = levelEditor.getSelectedSplinePath();
        if (selectedPath == null) {
            splineSelectionLabel.setText("Spline: none");
        } else {
            splineSelectionLabel.setText("Spline: " + selectedPath.name + " | " + selectedPath.getPoints().size() + " pts");
            if (stage.getKeyboardFocus() != splineNameField) {
                splineNameField.setText(selectedPath.name);
            }
            if (stage.getKeyboardFocus() != splineThicknessField) {
                splineThicknessField.setText(String.valueOf(selectedPath.collisionThickness));
            }
            splineCurveSelect.setSelected((selectedPath.curveType == null ? SplineCurveType.LINEAR : selectedPath.curveType).name());
        }

        SplineLayer selectedLayer = levelEditor.getSelectedSplineLayer();
        if (selectedLayer != null) {
            if (stage.getKeyboardFocus() != splineSpriteField) {
                splineSpriteField.setText(selectedLayer.spritePath == null ? "" : selectedLayer.spritePath);
            }
            if (stage.getKeyboardFocus() != splineDepthField) {
                splineDepthField.setText(String.valueOf(selectedLayer.renderDepth));
            }
            if (stage.getKeyboardFocus() != splineParallaxField) {
                splineParallaxField.setText(String.valueOf(selectedLayer.parallaxFactor));
            }
            if (stage.getKeyboardFocus() != splineWidthField) {
                splineWidthField.setText(String.valueOf(selectedLayer.visualWidth));
            }
            if (stage.getKeyboardFocus() != splineOffsetField) {
                splineOffsetField.setText(String.valueOf(selectedLayer.verticalOffset));
            }
            splineTileModeSelect.setSelected((selectedLayer.tileMode == null ? SplineTileMode.STRETCH : selectedLayer.tileMode).name());
        }
    }

    private int parseInt(String text, int fallback) {
        try {
            return Integer.parseInt(text.trim());
        } catch (Exception exception) {
            return fallback;
        }
    }

    private float parseFloat(String text, float fallback) {
        try {
            return Float.parseFloat(text.trim());
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
