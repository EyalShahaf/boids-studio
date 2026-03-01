package com.flocklab.ui;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.flocklab.sim.World;

/**
 * Top-left overlay showing FPS and basic metrics.
 */
public class StatsOverlay {
    private final World world;
    private final Label fpsLabel;

    public StatsOverlay(Stage stage, Skin skin, World world) {
        this.world = world;

        Table root = new Table();
        root.setFillParent(true);
        root.top().left();
        root.pad(10);

        fpsLabel = new Label("FPS: ", skin);
        root.add(fpsLabel).left().row();

        stage.addActor(root);
    }

    public void update() {
        int fps = Gdx.graphics.getFramesPerSecond();
        fpsLabel.setText("FPS: " + fps);
    }
}
