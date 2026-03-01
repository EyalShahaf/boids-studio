package com.flocklab.html;

import com.badlogic.gdx.ApplicationListener;
import com.badlogic.gdx.backends.gwt.GwtApplication;
import com.badlogic.gdx.backends.gwt.GwtApplicationConfiguration;
import com.flocklab.FlockLabGame;

public class HtmlLauncher extends GwtApplication {

    @Override
    public GwtApplicationConfiguration getConfig() {
        var config = new GwtApplicationConfiguration(1280, 720);
        config.antialiasing = true;
        return config;
    }

    @Override
    public ApplicationListener createApplicationListener() {
        return new FlockLabGame();
    }
}
