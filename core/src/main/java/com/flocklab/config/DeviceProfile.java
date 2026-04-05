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
    public static DeviceProfile detect(PlatformHints hints) {
        ApplicationType type = Gdx.app.getType();

        // Native desktop: always desktop regardless of window size
        if (type == ApplicationType.Desktop) {
            return DESKTOP;
        }

        float logicalMin;
        
        if (type == ApplicationType.WebGL && hints != null) {
            // In browsers, PhysicalPixels are usually true but CSS pixels dictate CSS layout!
            // We read the true CSS bounds via the js implementation of PlatformHints
            float width = hints.getLogicalWidth();
            float height = hints.getLogicalHeight();
            logicalMin = Math.min(width, height);
            
            // For WebGL, CSS dimensions under 600px imply Mobile, under 900px Tablet.
            if (logicalMin < 600f) return MOBILE_SMALL;
            if (logicalMin < 900f) return TABLET;
            
            // If the screen claims to be larger (e.g. tablet in landscape), but it's a touch device, 
            // ensure it's not marked as DESKTOP unless it's genuinely huge.
            if (hints.isTouchDevice() && logicalMin < 1100f) return TABLET;
            
            return DESKTOP;
        }

        float width  = Gdx.graphics.getWidth();
        float height = Gdx.graphics.getHeight();
        logicalMin = Math.min(width, height);

        if (type == ApplicationType.WebGL) {
            // Fallback for WebGL if no hints provided
            if (logicalMin < 600f) return MOBILE_SMALL;
            if (logicalMin < 900f) return TABLET;
            return DESKTOP;
        }

        // Android / iOS: getDensity() is accurate on real native builds.
        float density = Gdx.graphics.getDensity();
        if (density <= 0f) density = 1.0f;
        logicalMin = logicalMin / density;

        if (logicalMin < 600f) return MOBILE_SMALL;
        if (logicalMin < 900f) return TABLET;
        return DESKTOP;
    }
}
