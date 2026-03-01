package com.flocklab.model;

/**
 * A point in space that attracts boids (e.g., food or a waypoint).
 * Strength determines how powerfully boids are drawn to it.
 */
public record Attractor(Vec2 position, float strength) {
}
