package com.flocklab.ui;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Cell;
import com.badlogic.gdx.scenes.scene2d.ui.Dialog;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Slider;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.flocklab.AppVersion;
import com.flocklab.FlockLabGame;
import com.flocklab.config.Preset;
import com.flocklab.config.SimulationConfig;
import com.flocklab.sim.World;

import java.util.ArrayList;
import java.util.List;

/**
 * Side panel for controlling simulation parameters in real-time.
 * Supports retraction and tool selection.
 */
public class ControlPanel {
    private final Stage stage;
    private final World world;
    private final FlockLabGame game;
    private final Skin skin;

    private final Table panel;
    private final Cell<Table> panelCell;
    private Cell<Table> innerPanelCell;
    private boolean isRetracted = false;

    private Label boidCountLabel;
    private Label predatorCountLabel;
    private final List<Runnable> uiSyncers = new ArrayList<>();
    private final List<TextButton> toolButtons = new ArrayList<>();

    private float uiUpdateTimer = 0f;
    private static final float UI_UPDATE_INTERVAL = 0.125f; // ~8 Hz

    public ControlPanel(Table root, Stage stage, Skin skin, World world, FlockLabGame game) {
        this.stage = stage;
        this.world = world;
        this.game = game;
        this.skin = skin;

        // Container: panel on the left, toggle button flush against the screen's right edge
        Table container = new Table();
        panelCell = root.add(container).width(250).expandY().fillY();

        panel = new Table(skin);
        panel.setBackground("panel_bg");
        panel.pad(15);
        buildPanel(panel);

        // Toggle button is placed AFTER the panel so it always sits at the screen edge
        final TextButton toggleBtn = new TextButton("<", skin);
        toggleBtn.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                if (event.getTarget() != toggleBtn) return;

                isRetracted = !isRetracted;
                panel.setVisible(!isRetracted);
                toggleBtn.setText(isRetracted ? ">" : "<");
                // Collapse / restore the panel cell width so the toggle stays at screen edge
                innerPanelCell.width(isRetracted ? 0 : 210).minWidth(0);
                panelCell.width(isRetracted ? 40 : 250);
                root.invalidateHierarchy();
            }
        });

        // Panel first (left), toggle last (right / screen edge)
        innerPanelCell = container.add(panel).width(210).expandY().fillY();
        container.add(toggleBtn).width(40).expandY().fillY();
    }

    private void buildPanel(Table panel) {
        SimulationConfig cfg = world.getConfig();

        panel.add(new Label("Boids Studio", skin)).padBottom(15).row();

        // --- Tools ---
        panel.add(new Label("--- Tools ---", skin)).padBottom(10).row();
        Table tools = new Table();
        addToolButton(tools, "Boids", World.CursorMode.BOID);
        addToolButton(tools, "Obs", World.CursorMode.OBSTACLE);
        addToolButton(tools, "Attr", World.CursorMode.ATTRACTOR);
        addToolButton(tools, "Pred", World.CursorMode.PREDATOR);
        panel.add(tools).padBottom(15).row();

        // --- Controls ---
        final TextButton pauseBtn = new TextButton(game.isPaused() ? "Resume" : "Pause", skin);
        pauseBtn.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                game.setPaused(!game.isPaused());
                pauseBtn.setText(game.isPaused() ? "Resume" : "Pause");
            }
        });

        TextButton clearBtn = new TextButton("Clear All", skin);
        clearBtn.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                world.clearAll();
            }
        });

        final boolean[] lifeBarsOn = { game.getWorldRenderer().getBoidRenderer().isShowLifeBars() };
        final TextButton lifeBarsBtn = new TextButton(lifeBarsOn[0] ? "Life Bars: ON" : "Life Bars: OFF", skin);
        lifeBarsBtn.setColor(lifeBarsOn[0] ? Color.CYAN : Color.WHITE);
        lifeBarsBtn.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                lifeBarsOn[0] = !lifeBarsOn[0];
                game.getWorldRenderer().getBoidRenderer().setShowLifeBars(lifeBarsOn[0]);
                lifeBarsBtn.setText(lifeBarsOn[0] ? "Life Bars: ON" : "Life Bars: OFF");
                lifeBarsBtn.setColor(lifeBarsOn[0] ? Color.CYAN : Color.WHITE);
            }
        });

        Table topButtons = new Table();
        topButtons.add(pauseBtn).width(90).padRight(5);
        topButtons.add(clearBtn).width(90);
        panel.add(topButtons).padBottom(5).row();
        panel.add(lifeBarsBtn).fillX().height(30).padBottom(15).row();

        // --- Sliders ---
        addSliderRow(panel, "Max Speed", 10f, 400f, 1f, () -> cfg.maxSpeed, val -> cfg.maxSpeed = val);
        addSliderRow(panel, "Perception", 10f, 300f, 1f, () -> cfg.perceptionRadius, val -> cfg.perceptionRadius = val);

        panel.add(new Label("--- Rule Weights ---", skin)).padTop(5).padBottom(5).row();
        addSliderRow(panel, "Separation", 0f, 50f, 0.1f, () -> cfg.separationWeight, val -> cfg.separationWeight = val);
        addSliderRow(panel, "Alignment", 0f, 50f, 0.1f, () -> cfg.alignmentWeight, val -> cfg.alignmentWeight = val);
        addSliderRow(panel, "Cohesion", 0f, 50f, 0.1f, () -> cfg.cohesionWeight, val -> cfg.cohesionWeight = val);
        addSliderRow(panel, "Avoid Obstacles", 0f, 100f, 1f, () -> cfg.obstacleAvoidanceWeight,
                val -> cfg.obstacleAvoidanceWeight = val);

        // --- Presets ---
        panel.add(new Label("--- Presets ---", skin)).padTop(10).padBottom(5).row();
        Table presetTable = new Table();
        for (final Preset preset : Preset.values()) {
            TextButton presetBtn = new TextButton(preset.getDisplayName(), skin);
            presetBtn.addListener(new ChangeListener() {
                @Override
                public void changed(ChangeEvent event, Actor actor) {
                    preset.apply(cfg);
                    for (Runnable syncer : uiSyncers)
                        syncer.run();
                }
            });
            presetTable.add(presetBtn).fillX().padBottom(2).row();
        }
        panel.add(presetTable).fillX().row();

        // --- Info ---
        panel.add(new Label("--- Info ---", skin)).padTop(10).padBottom(5).row();
        boidCountLabel = new Label("Boids Count: " + world.getBoids().size(), skin);
        panel.add(boidCountLabel).left().padBottom(3).row();
        predatorCountLabel = new Label("Predators: " + world.getPredators().size(), skin);
        panel.add(predatorCountLabel).left().padBottom(10).row();

        // --- Project Details ---
        TextButton detailsBtn = new TextButton("Project Details", skin);
        detailsBtn.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                showDetailsDialog();
            }
        });
        panel.add(detailsBtn).fillX().height(35);
    }

    private void addToolButton(Table table, String name, final World.CursorMode mode) {
        final TextButton btn = new TextButton(name, skin);
        toolButtons.add(btn);
        btn.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                world.setCursorMode(mode);
                updateToolSelection();
            }
        });
        table.add(btn).width(50).pad(2);
        updateToolSelection();
    }

    private void updateToolSelection() {
        World.CursorMode current = world.getCursorMode();
        int idx = current.ordinal();
        for (int i = 0; i < toolButtons.size(); i++) {
            toolButtons.get(i).setColor(i == idx ? Color.CYAN : Color.WHITE);
        }
    }

    private void showDetailsDialog() {
        Dialog dialog = new Dialog("Boids Studio", skin);
        dialog.pad(20);
        Table content = dialog.getContentTable();
        content.left();
        content.add(new Label("Developed by: Eyal Shahaf", skin)).left().row();
        content.add(new Label("Version: " + AppVersion.VERSION, skin)).left().padBottom(5).row();
        content.add(new Label("Interactive Simulation", skin)).left().row();
        content.add(new Label("Controls:", skin)).padTop(10).left().row();
        content.add(new Label("- Left Click: Use selected tool", skin)).left().row();
        content.add(new Label("- Right Click: Remove objects", skin)).left().row();
        content.add(new Label("- Scroll: Zoom camera", skin)).left().row();

        TextButton githubBtn = new TextButton("View GitHub", skin);
        githubBtn.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                Gdx.net.openURI("https://github.com/EyalShahaf/boids-studio");
            }
        });
        dialog.button("Close");
        dialog.getButtonTable().add(githubBtn).width(120).padLeft(10);
        dialog.show(stage);
    }

    private void addSliderRow(Table panel, String name, float min, float max, float step, final ValueProvider provider,
            final ValueUpdater updater) {
        Table row = new Table();
        Label nameLabel = new Label(name, skin);
        final Label valLabel = new Label(String.valueOf(Math.round(provider.get() * 10f) / 10f), skin);
        final Slider slider = new Slider(min, max, step, false, skin);
        slider.setValue(provider.get());
        slider.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                float val = slider.getValue();
                valLabel.setText(String.valueOf(Math.round(val * 10f) / 10f));
                updater.update(val);
            }
        });
        uiSyncers.add(() -> slider.setValue(provider.get()));
        row.add(nameLabel).width(100).left();
        row.add(slider).width(70).padLeft(5).padRight(5);
        row.add(valLabel).width(35).right();
        panel.add(row).padBottom(5).row();
    }

    public void update(float deltaTime) {
        uiUpdateTimer += deltaTime;
        if (uiUpdateTimer >= UI_UPDATE_INTERVAL) {
            uiUpdateTimer = 0f;
            if (boidCountLabel != null) {
                boidCountLabel.setText("Boids Count: " + world.getBoids().size());
            }
            if (predatorCountLabel != null) {
                predatorCountLabel.setText("Predators: " + world.getPredators().size());
            }
        }
    }

    @FunctionalInterface
    private interface ValueUpdater {
        void update(float val);
    }

    @FunctionalInterface
    private interface ValueProvider {
        float get();
    }
}
