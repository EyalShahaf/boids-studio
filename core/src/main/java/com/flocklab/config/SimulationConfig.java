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

    public float fleeDetectionRadius = 150f;
    public float fleeForceScale = 3000f;

    public float boidStaminaMax = 100f;
    public float boidSprintTriggerRadius = 80f;
    public float boidSprintSpeedMultiplier = 1.6f;
    public float boidStaminaDrainRate = 35f;
    public float boidStaminaRegenBaseRate = 10f;
    public float boidStaminaRegenNearFoodBonus = 15f;

    public float boidHungerMax = 100f;
    public float boidHungerDrainRate = 1.5f;
    public float boidHungerRegenNearFoodRate = 30f;
    public float boidHungerFoodAttractionMultiplier = 1.5f;

    public float predatorHungerMax = 100f;
    public float predatorHungerDrainRate = 4f;
    public float predatorHungerOnEat = 40f;
    public float predatorStaminaMax = 100f;
    public float predatorStaminaDrainRate = 25f;
    public float predatorSprintTriggerRadius = 100f;
    public float predatorSprintSpeedMultiplier = 1.5f;
    public float predatorEatRadius = 12f;
    public float predatorEatChancePerSecond = 0.70f;

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
        this.fleeDetectionRadius = other.fleeDetectionRadius;
        this.fleeForceScale = other.fleeForceScale;
        this.boidStaminaMax = other.boidStaminaMax;
        this.boidSprintTriggerRadius = other.boidSprintTriggerRadius;
        this.boidSprintSpeedMultiplier = other.boidSprintSpeedMultiplier;
        this.boidStaminaDrainRate = other.boidStaminaDrainRate;
        this.boidStaminaRegenBaseRate = other.boidStaminaRegenBaseRate;
        this.boidStaminaRegenNearFoodBonus = other.boidStaminaRegenNearFoodBonus;
        this.boidHungerMax = other.boidHungerMax;
        this.boidHungerDrainRate = other.boidHungerDrainRate;
        this.boidHungerRegenNearFoodRate = other.boidHungerRegenNearFoodRate;
        this.boidHungerFoodAttractionMultiplier = other.boidHungerFoodAttractionMultiplier;
        this.predatorHungerMax = other.predatorHungerMax;
        this.predatorHungerDrainRate = other.predatorHungerDrainRate;
        this.predatorHungerOnEat = other.predatorHungerOnEat;
        this.predatorStaminaMax = other.predatorStaminaMax;
        this.predatorStaminaDrainRate = other.predatorStaminaDrainRate;
        this.predatorSprintTriggerRadius = other.predatorSprintTriggerRadius;
        this.predatorSprintSpeedMultiplier = other.predatorSprintSpeedMultiplier;
        this.predatorEatRadius = other.predatorEatRadius;
        this.predatorEatChancePerSecond = other.predatorEatChancePerSecond;
        this.initialBoidCount = other.initialBoidCount;
        this.worldWidth = other.worldWidth;
        this.worldHeight = other.worldHeight;
    }

    public void resetToDefaults() {
        this.copyFrom(new SimulationConfig());
    }
}
