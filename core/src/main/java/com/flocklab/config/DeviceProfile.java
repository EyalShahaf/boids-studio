package com.flocklab.config;

import com.badlogic.gdx.Application.ApplicationType;
import com.badlogic.gdx.Gdx;

/**
 * Defines the detected class of device running the simulation.
 */
public enum DeviceProfile {
    DESKTOP,
    TABLET,
    MOBILE_SMALL;

    /**
     * Heuristically determines the device profile from screen size.
     *
     * IMPORTANT: GWT/WebGL always reports getDensity() == 1.0, so we CANNOT
     * divide by density there. Instead we rely on CSS pixel dimensions
     * (which the browser exposes to GWT directly). A typical phone in portrait
     * is 360-430 CSS px wide; a tablet is 600-900; a desktop is 900+.
     */
    public static DeviceProfile detect() {
        ApplicationType type = Gdx.app.getType();

        // Native desktop: always desktop regardless of window size
        if (type == ApplicationType.Desktop) {
            return DESKTOP;
        }

        float width  = Gdx.graphics.getWidth();
        float height = Gdx.graphics.getHeight();
        float minDim = Math.min(width, height);

        if (type == ApplicationType.WebGL) {
            // GWT reports CSS pixels directly. Density is always 1 – do NOT divide.
            if (minDim < 600f) return MOBILE_SMALL;
            if (minDim < 900f) return TABLET;
            return DESKTOP;
        }

        // Android / iOS: getDensity() is accurate on real native builds.
        float density = Gdx.graphics.getDensity();
        if (density <= 0f) density = 1.0f;
        float logicalMin = minDim / density;

        if (logicalMin < 600f) return MOBILE_SMALL;
        if (logicalMin < 900f) return TABLET;
        return DESKTOP;
    }
}
