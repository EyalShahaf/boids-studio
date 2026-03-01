package com.flocklab.html;

import com.badlogic.gdx.ApplicationListener;
import com.badlogic.gdx.backends.gwt.GwtApplication;
import com.badlogic.gdx.backends.gwt.GwtApplicationConfiguration;
import com.flocklab.FlockLabGame;

public class HtmlLauncher extends GwtApplication {

    @Override
    public GwtApplicationConfiguration getConfig() {
        // Resizable application, matches browser window
        GwtApplicationConfiguration config = new GwtApplicationConfiguration(true);
        config.padHorizontal = 0;
        config.padVertical = 0;
        config.antialiasing = true;
        config.alpha = true; // Often fixes WebGL context issues in some browsers
        return config;
    }

    @Override
    public ApplicationListener createApplicationListener() {
        return new FlockLabGame();
    }
}
