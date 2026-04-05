package com.flocklab.config;

/**
 * Interface to pass platform-specific hints (like native browser properties)
 * to the core module without coupling it to a specific backend.
 */
public interface PlatformHints {
    /** Returns true if the device is touch-capable. */
    boolean isTouchDevice();

    /** Returns the true CSS logical width (immune to devicePixelRatio scaling). */
    int getLogicalWidth();

    /** Returns the true CSS logical height. */
    int getLogicalHeight();
}
