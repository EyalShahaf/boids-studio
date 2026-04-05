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
     * Attempts to heuristically determine the device profile.
     */
    public static DeviceProfile detect() {
        // Allow manual overrides during testing (if set in a property, etc)
        // For now, heuristic detection based on LibGDX environment

        ApplicationType type = Gdx.app.getType();
        
        // If it's a native desktop app, treat as desktop regardless of size
        if (type == ApplicationType.Desktop) {
            return DESKTOP;
        }
        
        // For WebGL or Android/iOS, try to guess based on resolution and density.
        // We use logical pixels if available, or just fallback to simple thresholds.
        float width = Gdx.graphics.getWidth();
        float height = Gdx.graphics.getHeight();
        
        // Get the smaller dimension to represent the screen's baseline width
        float minDim = Math.min(width, height);
        
        // Typical mobile phones have a smaller logical or physical min dimension
        // In GWT, usually width/height are the CSS pixel dimensions.
        if (type == ApplicationType.WebGL) {
            if (minDim < 600) {
                return MOBILE_SMALL;
            } else if (minDim < 900) {
                return TABLET;
            }
            return DESKTOP;
        }

        // Default fallback for mobile platforms (Android/iOS)
        if (type == ApplicationType.Android || type == ApplicationType.iOS) {
            // Rough heuristic: density can vary widely, but assuming a simple threshold
            float density = Gdx.graphics.getDensity();
            float logicalMin = minDim / density;
            
            if (logicalMin < 600) {
                return MOBILE_SMALL;
            } else {
                return TABLET; // Larger logical screen -> Tablet
            }
        }
        
        return DESKTOP;
    }
}
