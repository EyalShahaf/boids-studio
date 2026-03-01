package com.flocklab;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;

/**
 * Main game entry point — shared by desktop and HTML launchers.
 * Will be expanded in later steps to include the full simulation, renderer, and
 * UI.
 */
public class FlockLabGame extends ApplicationAdapter {

    @Override
    public void create() {
        Gdx.app.log("FlockLab", "Flock Lab initialized");
    }

    @Override
    public void render() {
        Gdx.gl.glClearColor(0.04f, 0.04f, 0.06f, 1f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
    }

    @Override
    public void dispose() {
        Gdx.app.log("FlockLab", "Flock Lab disposed");
    }
}
