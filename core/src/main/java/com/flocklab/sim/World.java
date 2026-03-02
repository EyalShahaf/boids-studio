package com.flocklab.sim;

import com.badlogic.gdx.math.MathUtils;
import com.flocklab.config.SimulationConfig;
import com.flocklab.model.Attractor;
import com.flocklab.model.Boid;
import com.flocklab.model.Obstacle;
import com.flocklab.model.Predator;
import com.flocklab.model.Vec2;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

/**
 * The top-level simulation orchestrator.
 * Maintains state of all entities and steps the physics forward.
 */
public class World {
    private final SimulationConfig config;
    private final List<Boid> boids = new ArrayList<>();
    private final List<Obstacle> obstacles = new ArrayList<>();
    private final List<Attractor> attractors = new ArrayList<>();
    private final List<Predator> predators = new ArrayList<>();

    public enum CursorMode {
        BOID, OBSTACLE, ATTRACTOR, PREDATOR
    }

    private CursorMode currentMode = CursorMode.BOID;

    private SpatialGrid spatialGrid;
    private int nextBoidId = 0;
    private int nextPredatorId = 0;

    private final Random random = new Random();

    /** Reusable neighbor buffer — avoids a new ArrayList allocation per boid per frame. */
    private final List<Boid> neighborBuffer = new ArrayList<>();

    /** Pre-allocated proxy boid used for predator spatial queries. */
    private final Boid predatorProxy = new Boid(-1, Vec2.ZERO, Vec2.ZERO, 0f, 0f);

    // ---- Game Of Life statistics ----
    private int totalBoidsCreated = 0;
    private int totalBoidsEaten = 0;
    private int totalPredatorsCreated = 0;
    private int totalPredatorsDiedOfHunger = 0;

    public World(SimulationConfig config) {
        this.config = config;
        this.spatialGrid = createGrid();

        for (int i = 0; i < config.initialBoidCount; i++) {
            float x = (float) (Math.random() * config.worldWidth);
            float y = (float) (Math.random() * config.worldHeight);
            float vx = (float) (Math.random() * 2 - 1);
            float vy = (float) (Math.random() * 2 - 1);
            boids.add(new Boid(nextBoidId++, new Vec2(x, y),
                    new Vec2(vx, vy).setMagnitude(config.maxSpeed),
                    config.boidStaminaMax, config.boidHungerMax));
            totalBoidsCreated++;
        }
    }

    public void update(float deltaTime) {
        ensureGridMatchesConfig();
        spatialGrid.clear();

        for (int i = 0; i < boids.size(); i++) {
            spatialGrid.insert(boids.get(i));
        }

        float perceptionRadius = config.perceptionRadius;
        float perceptionRadiusSq = perceptionRadius * perceptionRadius;
        float worldWidth = config.worldWidth;
        float worldHeight = config.worldHeight;

        // --- Boid steering forces ---
        for (int i = 0; i < boids.size(); i++) {
            Boid boid = boids.get(i);
            spatialGrid.getNeighborsInto(boid, perceptionRadius, neighborBuffer);

            Vec2 flockForce = BoidRules.flock(boid, neighborBuffer, perceptionRadiusSq,
                    config.separationWeight, config.alignmentWeight, config.cohesionWeight);

            boid.applyForce(flockForce);
            boid.applyForce(BoidRules.avoidObstacles(boid, obstacles, perceptionRadius)
                    .scale(config.obstacleAvoidanceWeight));
            boid.applyForce(BoidRules.fleePredators(boid, predators,
                    config.fleeDetectionRadius, config.fleeForceScale)
                    .scale(config.predatorFleeWeight));
            // Hungry boids seek food more urgently; pull scales up as hunger depletes
            float boidHungerFraction = boid.getHunger() / config.boidHungerMax;
            float foodFactor = 1f + (1f - boidHungerFraction) * config.boidHungerFoodAttractionMultiplier;
            boid.applyForce(BoidRules.seekAttractors(boid, attractors, perceptionRadius)
                    .scale(config.foodAttractionWeight * foodFactor));
        }

        // --- Boid physics + stamina ---
        for (int i = 0; i < boids.size(); i++) {
            Boid boid = boids.get(i);
            updateBoidStaminaAndSprint(boid, deltaTime);
            boid.update(deltaTime, config.maxSpeed, config.boidSprintSpeedMultiplier,
                    worldWidth, worldHeight);
        }

        // --- Predator update ---
        if (!predators.isEmpty()) {
            float predatorRadius = perceptionRadius * 2f;
            float predatorRadiusSq = predatorRadius * predatorRadius;

            Set<Integer> eatenBoidIds = new HashSet<>();
            List<Predator> deadPredators = new ArrayList<>();

            for (int i = 0; i < predators.size(); i++) {
                Predator predator = predators.get(i);
                predatorProxy.setPosition(predator.getPosition());
                spatialGrid.getNeighborsInto(predatorProxy, predatorRadius, neighborBuffer);

                Vec2 baseChase = BoidRules.cohesionSquaredRadius(predatorProxy, neighborBuffer, predatorRadiusSq);
                Vec2 chase = updatePredatorHungerAndSprint(predator, neighborBuffer, baseChase, deltaTime, deadPredators);

                predator.update(chase, deltaTime, config.predatorSprintSpeedMultiplier, worldWidth, worldHeight);
                handlePredatorEating(predator, boids, eatenBoidIds, deltaTime);
            }

            removeDeadEntities(eatenBoidIds, deadPredators);
        }
    }

    /**
     * Updates stamina, hunger, and sprint state for a single boid.
     *
     * Sprint: triggers when any predator is within boidSprintTriggerRadius and stamina > 0.
     * Stamina: always regenerates at base rate; eating near an attractor gives a bonus.
     * Hunger: always drains slowly; eating near an attractor replenishes it.
     * Eating near food also replenishes stamina via the existing near-food bonus.
     */
    private void updateBoidStaminaAndSprint(Boid boid, float deltaTime) {
        float sprintRadiusSq = config.boidSprintTriggerRadius * config.boidSprintTriggerRadius;
        boolean predatorNear = false;
        for (int p = 0; p < predators.size(); p++) {
            float dx = boid.getPosition().x() - predators.get(p).getPosition().x();
            float dy = boid.getPosition().y() - predators.get(p).getPosition().y();
            if (dx * dx + dy * dy < sprintRadiusSq) {
                predatorNear = true;
                break;
            }
        }

        float stamina = boid.getStamina();
        float hunger = boid.getHunger();

        // Sprint drains stamina; no predator nearby resets sprint
        if (predatorNear && stamina > 0f) {
            boid.setSprinting(true);
            stamina -= config.boidStaminaDrainRate * deltaTime;
        } else {
            boid.setSprinting(false);
        }

        // Hunger drains continuously (slower than predators)
        hunger -= config.boidHungerDrainRate * deltaTime;

        // Always regenerate stamina at base rate
        stamina += config.boidStaminaRegenBaseRate * deltaTime;

        // Near a food attractor: eat — replenish both hunger and stamina
        if (!attractors.isEmpty()) {
            float perceptionSq = config.perceptionRadius * config.perceptionRadius;
            for (int a = 0; a < attractors.size(); a++) {
                float dx = boid.getPosition().x() - attractors.get(a).position().x();
                float dy = boid.getPosition().y() - attractors.get(a).position().y();
                if (dx * dx + dy * dy < perceptionSq) {
                    hunger += config.boidHungerRegenNearFoodRate * deltaTime;
                    stamina += config.boidStaminaRegenNearFoodBonus * deltaTime;
                    break;
                }
            }
        }

        boid.setStamina(MathUtils.clamp(stamina, 0f, config.boidStaminaMax));
        boid.setHunger(MathUtils.clamp(hunger, 0f, config.boidHungerMax));
    }

    /**
     * Updates predator hunger and stamina/sprint state.
     * Returns the hunger-scaled chase force to use for movement.
     * Marks the predator dead (adds to deadPredators) when hunger reaches zero.
     */
    private Vec2 updatePredatorHungerAndSprint(Predator predator, List<Boid> nearbyBoids,
            Vec2 baseChase, float deltaTime, List<Predator> deadPredators) {
        // Drain hunger
        float hunger = predator.getHunger() - config.predatorHungerDrainRate * deltaTime;
        predator.setHunger(MathUtils.clamp(hunger, 0f, config.predatorHungerMax));

        if (predator.getHunger() <= 0f) {
            deadPredators.add(predator);
            return Vec2.ZERO;
        }

        // Hunger-scaled chase force: starving predators chase up to 3x harder
        float hungerFraction = predator.getHunger() / config.predatorHungerMax;
        float hungerFactor = 1f + (1f - hungerFraction) * 2f;
        Vec2 scaledChase = baseChase.scale(hungerFactor);

        // Sprint when any nearby boid is within sprint radius
        float sprintRadiusSq = config.predatorSprintTriggerRadius * config.predatorSprintTriggerRadius;
        boolean boidNear = false;
        for (int n = 0; n < nearbyBoids.size(); n++) {
            float dx = predator.getPosition().x() - nearbyBoids.get(n).getPosition().x();
            float dy = predator.getPosition().y() - nearbyBoids.get(n).getPosition().y();
            if (dx * dx + dy * dy < sprintRadiusSq) {
                boidNear = true;
                break;
            }
        }

        float stamina = predator.getStamina();
        if (boidNear && stamina > 0f) {
            predator.setSprinting(true);
            stamina -= config.predatorStaminaDrainRate * deltaTime;
        } else {
            predator.setSprinting(false);
        }
        predator.setStamina(MathUtils.clamp(stamina, 0f, config.predatorStaminaMax));

        return scaledChase;
    }

    /**
     * Checks boids near the predator and probabilistically eats them.
     * Eating chance scales with hunger (starving predators eat more frantically).
     * Eaten boid IDs are collected in eatenBoidIds for deferred removal.
     */
    private void handlePredatorEating(Predator predator, List<Boid> allBoids,
            Set<Integer> eatenBoidIds, float deltaTime) {
        float eatRadiusSq = config.predatorEatRadius * config.predatorEatRadius;
        float baseChance = config.predatorEatChancePerSecond * deltaTime;
        float hungerFraction = predator.getHunger() / config.predatorHungerMax;
        float hungerFactor = 1f + (1f - hungerFraction); // up to 2x when starving
        float p = Math.min(baseChance * hungerFactor, 1f);

        for (int i = 0; i < allBoids.size(); i++) {
            Boid boid = allBoids.get(i);
            if (eatenBoidIds.contains(boid.getId())) continue;

            float dx = predator.getPosition().x() - boid.getPosition().x();
            float dy = predator.getPosition().y() - boid.getPosition().y();
            if (dx * dx + dy * dy < eatRadiusSq) {
                if (random.nextFloat() < p) {
                    eatenBoidIds.add(boid.getId());
                    float newHunger = predator.getHunger() + config.predatorHungerOnEat;
                    predator.setHunger(MathUtils.clamp(newHunger, 0f, config.predatorHungerMax));
                    totalBoidsEaten++;
                }
            }
        }
    }

    /**
     * Removes eaten boids and dead (starved) predators after all updates are done,
     * avoiding ConcurrentModificationException.
     */
    private void removeDeadEntities(Set<Integer> eatenBoidIds, List<Predator> deadPredators) {
        if (!eatenBoidIds.isEmpty()) {
            boids.removeIf(b -> eatenBoidIds.contains(b.getId()));
        }
        if (!deadPredators.isEmpty()) {
            totalPredatorsDiedOfHunger += deadPredators.size();
            predators.removeAll(deadPredators);
        }
    }

    private SpatialGrid createGrid() {
        return new SpatialGrid(Math.max(config.perceptionRadius, 50f), config.worldWidth, config.worldHeight);
    }

    private void ensureGridMatchesConfig() {
        float requiredCellSize = Math.max(config.perceptionRadius, 50f);
        if (Math.abs(spatialGrid.getCellSize() - requiredCellSize) > 0.0001f
                || spatialGrid.getCols() != (int) Math.ceil(config.worldWidth / requiredCellSize)
                || spatialGrid.getRows() != (int) Math.ceil(config.worldHeight / requiredCellSize)) {
            spatialGrid = createGrid();
        }
    }

    public SimulationConfig getConfig() {
        return config;
    }

    public List<Boid> getBoids() {
        return Collections.unmodifiableList(boids);
    }

    public List<Obstacle> getObstacles() {
        return Collections.unmodifiableList(obstacles);
    }

    public List<Attractor> getAttractors() {
        return Collections.unmodifiableList(attractors);
    }

    public List<Predator> getPredators() {
        return Collections.unmodifiableList(predators);
    }

    public void addBoid(Vec2 pos, Vec2 vel) {
        boids.add(new Boid(nextBoidId++, pos, vel, config.boidStaminaMax, config.boidHungerMax));
        totalBoidsCreated++;
    }

    public void addObstacle(Obstacle obs) {
        obstacles.add(obs);
    }

    public boolean removeObstacleNear(Vec2 pos, float radius) {
        for (int i = 0; i < obstacles.size(); i++) {
            if (obstacles.get(i).center().distanceTo(pos) <= radius) {
                obstacles.remove(i);
                return true;
            }
        }
        return false;
    }

    public void addAttractor(Attractor att) {
        attractors.add(att);
    }

    /** Spawns a predator at the given world position with full stamina and hunger. */
    public void spawnPredator(Vec2 pos) {
        predators.add(new Predator(nextPredatorId++, pos, Vec2.ZERO,
                config.maxSpeed * 1.5f, config.predatorStaminaMax, config.predatorHungerMax));
        totalPredatorsCreated++;
    }

    /** @deprecated Use {@link #spawnPredator(Vec2)} to ensure proper stamina/hunger initialisation. */
    @Deprecated
    public void addPredator(Predator pred) {
        predators.add(pred);
        totalPredatorsCreated++;
    }

    public void clearAll() {
        boids.clear();
        obstacles.clear();
        attractors.clear();
        predators.clear();
    }

    public CursorMode getCursorMode() {
        return currentMode;
    }

    public void setCursorMode(CursorMode mode) {
        this.currentMode = mode;
    }

    // ---- Statistics getters ----

    public int getTotalBoidsCreated() {
        return totalBoidsCreated;
    }

    public int getTotalBoidsEaten() {
        return totalBoidsEaten;
    }

    public int getTotalPredatorsCreated() {
        return totalPredatorsCreated;
    }

    public int getTotalPredatorsDiedOfHunger() {
        return totalPredatorsDiedOfHunger;
    }
}
