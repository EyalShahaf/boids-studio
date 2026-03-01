package com.flocklab.ui;

import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Slider;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.flocklab.FlockLabGame;
import com.flocklab.config.Preset;
import com.flocklab.config.SimulationConfig;
import com.flocklab.sim.World;

/**
 * Side panel for controlling simulation parameters in real-time.
 */
public class ControlPanel {
    private final Stage stage;
    private final World world;
    private final FlockLabGame game;
    private final Skin skin;

    private Label boidCountLabel;

    public ControlPanel(Stage stage, Skin skin, World world, FlockLabGame game) {
        this.stage = stage;
        this.world = world;
        this.game = game;
        this.skin = skin;

        Table root = new Table();
        root.setFillParent(true);
        root.right(); // Align panel to the right edge

        Table panel = new Table(skin);
        panel.setBackground("panel_bg");
        panel.pad(15);

        buildPanel(panel);

        root.add(panel).width(250).expandY().fillY();
        stage.addActor(root);
    }

    private void buildPanel(Table panel) {
        SimulationConfig cfg = world.getConfig();

        panel.add(new Label("FLOCK LAB", skin)).padBottom(20).row();

        // --- Controls ---
        TextButton pauseBtn = new TextButton(game.isPaused() ? "Resume" : "Pause", skin);
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

        Table topButtons = new Table();
        topButtons.add(pauseBtn).width(100).padRight(10);
        topButtons.add(clearBtn).width(100);
        panel.add(topButtons).padBottom(20).row();

        // --- Sliders ---
        addSliderRow(panel, "Max Speed", 10f, 400f, 1f, cfg.maxSpeed, val -> cfg.maxSpeed = val);
        addSliderRow(panel, "Perception", 10f, 300f, 1f, cfg.perceptionRadius, val -> cfg.perceptionRadius = val);

        panel.add(new Label("--- Rule Weights ---", skin)).padTop(10).padBottom(10).row();

        addSliderRow(panel, "Separation", 0f, 50f, 0.1f, cfg.separationWeight, val -> cfg.separationWeight = val);
        addSliderRow(panel, "Alignment", 0f, 50f, 0.1f, cfg.alignmentWeight, val -> cfg.alignmentWeight = val);
        addSliderRow(panel, "Cohesion", 0f, 50f, 0.1f, cfg.cohesionWeight, val -> cfg.cohesionWeight = val);
        addSliderRow(panel, "Avoid Obstacles", 0f, 100f, 1f, cfg.obstacleAvoidanceWeight,
                val -> cfg.obstacleAvoidanceWeight = val);

        // --- Presets ---
        panel.add(new Label("--- Presets ---", skin)).padTop(20).padBottom(10).row();

        for (Preset preset : Preset.values()) {
            TextButton presetBtn = new TextButton(preset.getDisplayName(), skin);
            presetBtn.addListener(new ChangeListener() {
                @Override
                public void changed(ChangeEvent event, Actor actor) {
                    preset.apply(cfg);
                    // For a full implementation, we'd also update the slider UI values here
                }
            });
            panel.add(presetBtn).fillX().padBottom(5).row();
        }

        // --- Stats ---
        panel.add(new Label("--- Info ---", skin)).padTop(20).padBottom(10).row();
        boidCountLabel = new Label("Count: " + world.getBoids().size(), skin);
        panel.add(boidCountLabel).left().row();
    }

    private void addSliderRow(Table panel, String name, float min, float max, float step, float initial,
            ValueUpdater updater) {
        Table row = new Table();
        Label nameLabel = new Label(name, skin);
        Label valLabel = new Label(String.format("%.1f", initial), skin);

        Slider slider = new Slider(min, max, step, false, skin);
        slider.setValue(initial);
        slider.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                float val = slider.getValue();
                valLabel.setText(String.format("%.1f", val));
                updater.update(val);
            }
        });

        row.add(nameLabel).width(110).left();
        row.add(slider).width(80).padLeft(5).padRight(5);
        row.add(valLabel).width(35).right();

        panel.add(row).padBottom(10).row();
    }

    public void update() {
        if (boidCountLabel != null) {
            boidCountLabel.setText("Count: " + world.getBoids().size());
        }
    }

    @FunctionalInterface
    private interface ValueUpdater {
        void update(float val);
    }
}
