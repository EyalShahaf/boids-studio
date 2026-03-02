package com.flocklab.desktop;

import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Application;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3ApplicationConfiguration;
import com.flocklab.FlockLabGame;

public class DesktopLauncher {

    public static void main(String[] args) {
        var config = new Lwjgl3ApplicationConfiguration();
        config.setTitle("Boids Studio");
        config.setWindowedMode(1280, 720);
        config.useVsync(true);
        config.setForegroundFPS(0); // 0 means no limit (will be throttled by VSync if true)

        new Lwjgl3Application(new FlockLabGame(), config);
    }
}
