package com.flocklab.ui;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.flocklab.sim.World;

/**
 * Top-left overlay showing FPS and boid count, updated at ~8 Hz to avoid
 * string allocation pressure on every render frame.
 */
public class StatsOverlay {
    private static final float UPDATE_INTERVAL = 0.125f; // ~8 Hz

    private final World world;
    private final Label fpsLabel;
    private final Label boidLabel;

    private float timeSinceUpdate = 0f;

    public StatsOverlay(Stage stage, Skin skin, World world) {
        this.world = world;

        Table root = new Table();
        root.setFillParent(true);
        root.top().left();
        root.pad(10);

        fpsLabel = new Label("FPS: --", skin);
        boidLabel = new Label("Boids: --", skin);
        root.add(fpsLabel).left().row();
        root.add(boidLabel).left().row();

        stage.addActor(root);
    }

    public void update(float deltaTime) {
        timeSinceUpdate += deltaTime;
        if (timeSinceUpdate >= UPDATE_INTERVAL) {
            timeSinceUpdate = 0f;
            fpsLabel.setText("FPS: " + Gdx.graphics.getFramesPerSecond());
            boidLabel.setText("Boids: " + world.getBoids().size());
        }
    }
}
