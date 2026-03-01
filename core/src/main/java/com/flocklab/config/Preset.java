package com.flocklab.config;

/**
 * Pre-defined configurations for different flocking behaviors.
 */
public enum Preset {
    DEFAULT("Classic Boids") {
        @Override
        public void apply(SimulationConfig config) {
            config.resetToDefaults();
        }
    },
    TIGHT_FLOCK("Tight Flock") {
        @Override
        public void apply(SimulationConfig config) {
            config.resetToDefaults();
            config.cohesionWeight = 2.5f;
            config.alignmentWeight = 1.5f;
            config.separationWeight = 1.0f;
            config.maxSpeed = 180f;
        }
    },
    CHAOTIC_SWARM("Chaotic Swarm") {
        @Override
        public void apply(SimulationConfig config) {
            config.resetToDefaults();
            config.separationWeight = 3.0f;
            config.alignmentWeight = 0.2f;
            config.cohesionWeight = 0.5f;
            config.maxSpeed = 250f;
            config.perceptionRadius = 40f;
        }
    },
    SCHOOL_OF_FISH("School of Fish") {
        @Override
        public void apply(SimulationConfig config) {
            config.resetToDefaults();
            config.alignmentWeight = 3.0f;
            config.cohesionWeight = 1.5f;
            config.separationWeight = 1.2f;
            config.perceptionRadius = 80f;
        }
    },
    CALM_BIRDS("Calm Birds") {
        @Override
        public void apply(SimulationConfig config) {
            config.resetToDefaults();
            config.maxSpeed = 80f;
            config.alignmentWeight = 0.8f;
            config.cohesionWeight = 0.5f;
            config.separationWeight = 1.0f;
            config.perceptionRadius = 100f;
        }
    };

    private final String displayName;

    Preset(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }

    public abstract void apply(SimulationConfig config);
}
