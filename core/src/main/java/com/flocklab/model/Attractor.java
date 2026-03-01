package com.flocklab.model;

/**
 * Point of interest that boids are drawn towards (e.g., food source).
 */
public class Attractor {
    private final Vec2 position;
    private final float strength;

    public Attractor(Vec2 position, float strength) {
        this.position = position;
        this.strength = strength;
    }

    public Vec2 position() {
        return position;
    }

    public float strength() {
        return strength;
    }
}
