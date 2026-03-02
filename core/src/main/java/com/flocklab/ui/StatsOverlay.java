package com.flocklab.ui;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.flocklab.sim.World;

/**
 * Top-left overlay showing FPS, entity counts, and Game Of Life lifecycle
 * statistics. Updated at ~8 Hz to avoid string allocation pressure every frame.
 */
public class StatsOverlay {
    private static final float UPDATE_INTERVAL = 0.125f; // ~8 Hz

    private final World world;
    private final Label fpsLabel;
    private final Label boidLabel;
    private final Label predatorLabel;
    private final Label boidsCreatedLabel;
    private final Label boidsEatenLabel;
    private final Label predatorsCreatedLabel;
    private final Label predatorsDiedLabel;

    private float timeSinceUpdate = 0f;

    public StatsOverlay(Stage stage, Skin skin, World world) {
        this.world = world;

        Table root = new Table();
        root.setFillParent(true);
        root.top().left();
        root.pad(10);

        fpsLabel = new Label("FPS: --", skin);
        boidLabel = new Label("Boids: --", skin);
        predatorLabel = new Label("Predators: --", skin);
        boidsCreatedLabel = new Label("Created: --", skin);
        boidsEatenLabel = new Label("Eaten: --", skin);
        predatorsCreatedLabel = new Label("Pred. Created: --", skin);
        predatorsDiedLabel = new Label("Starved: --", skin);

        root.add(fpsLabel).left().row();
        root.add(boidLabel).left().row();
        root.add(predatorLabel).left().row();
        root.add(new Label("--- Game Of Life ---", skin)).left().padTop(4).row();
        root.add(boidsCreatedLabel).left().row();
        root.add(boidsEatenLabel).left().row();
        root.add(predatorsCreatedLabel).left().row();
        root.add(predatorsDiedLabel).left().row();

        stage.addActor(root);
    }

    public void update(float deltaTime) {
        timeSinceUpdate += deltaTime;
        if (timeSinceUpdate >= UPDATE_INTERVAL) {
            timeSinceUpdate = 0f;
            fpsLabel.setText("FPS: " + Gdx.graphics.getFramesPerSecond());
            boidLabel.setText("Boids: " + world.getBoids().size());
            predatorLabel.setText("Predators: " + world.getPredators().size());
            boidsCreatedLabel.setText("Created: " + world.getTotalBoidsCreated());
            boidsEatenLabel.setText("Eaten: " + world.getTotalBoidsEaten());
            predatorsCreatedLabel.setText("Pred. Created: " + world.getTotalPredatorsCreated());
            predatorsDiedLabel.setText("Starved: " + world.getTotalPredatorsDiedOfHunger());
        }
    }
}
