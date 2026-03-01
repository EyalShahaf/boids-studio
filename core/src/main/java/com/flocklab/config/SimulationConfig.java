package com.flocklab.config;

/**
 * Mutable configuration object holding all tunable parameters for the
 * simulation.
 */
public class SimulationConfig {
    public float maxSpeed = 150f;
    public float perceptionRadius = 50f;

    public float separationWeight = 1.5f;
    public float alignmentWeight = 1.0f;
    public float cohesionWeight = 1.0f;

    public float obstacleAvoidanceWeight = 3.0f;
    public float predatorFleeWeight = 2.5f;
    public float foodAttractionWeight = 1.2f;

    public int initialBoidCount = 500;
    public float worldWidth = 1280f;
    public float worldHeight = 720f;

    public SimulationConfig() {
    }

    public void copyFrom(SimulationConfig other) {
        this.maxSpeed = other.maxSpeed;
        this.perceptionRadius = other.perceptionRadius;
        this.separationWeight = other.separationWeight;
        this.alignmentWeight = other.alignmentWeight;
        this.cohesionWeight = other.cohesionWeight;
        this.obstacleAvoidanceWeight = other.obstacleAvoidanceWeight;
        this.predatorFleeWeight = other.predatorFleeWeight;
        this.foodAttractionWeight = other.foodAttractionWeight;
        this.initialBoidCount = other.initialBoidCount;
        this.worldWidth = other.worldWidth;
        this.worldHeight = other.worldHeight;
    }

    public void resetToDefaults() {
        this.copyFrom(new SimulationConfig());
    }
}
