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
import java.util.List;

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

    /** Reusable neighbor buffer — avoids a new ArrayList allocation per boid per frame. */
    private final List<Boid> neighborBuffer = new ArrayList<>();

    /** Pre-allocated proxy boid used for predator spatial queries. */
    private final Boid predatorProxy = new Boid(-1, Vec2.ZERO, Vec2.ZERO, 0f);

    public World(SimulationConfig config) {
        this.config = config;
        this.spatialGrid = createGrid();

        for (int i = 0; i < config.initialBoidCount; i++) {
            float x = (float) (Math.random() * config.worldWidth);
            float y = (float) (Math.random() * config.worldHeight);
            float vx = (float) (Math.random() * 2 - 1);
            float vy = (float) (Math.random() * 2 - 1);
            boids.add(new Boid(nextBoidId++, new Vec2(x, y),
                    new Vec2(vx, vy).setMagnitude(config.maxSpeed), config.boidStaminaMax));
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
            boid.applyForce(BoidRules.seekAttractors(boid, attractors, perceptionRadius)
                    .scale(config.foodAttractionWeight));
        }

        for (int i = 0; i < boids.size(); i++) {
            Boid boid = boids.get(i);
            updateBoidStaminaAndSprint(boid, deltaTime);
            boid.update(deltaTime, config.maxSpeed, config.boidSprintSpeedMultiplier,
                    worldWidth, worldHeight);
        }

        if (!predators.isEmpty()) {
            float predatorRadius = perceptionRadius * 2f;
            float predatorRadiusSq = predatorRadius * predatorRadius;
            for (int i = 0; i < predators.size(); i++) {
                Predator predator = predators.get(i);
                predatorProxy.setPosition(predator.getPosition());
                spatialGrid.getNeighborsInto(predatorProxy, predatorRadius, neighborBuffer);
                Vec2 chase = BoidRules.cohesionSquaredRadius(predatorProxy, neighborBuffer, predatorRadiusSq);
                predator.update(chase, deltaTime, worldWidth, worldHeight);
            }
        }
    }

    /**
     * Updates stamina and sprint state for a single boid.
     * Sprint triggers when any predator is within boidSprintTriggerRadius and stamina > 0.
     * Stamina always regenerates at the base rate; near attractors an additional bonus applies.
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

        if (predatorNear && stamina > 0f) {
            boid.setSprinting(true);
            stamina -= config.boidStaminaDrainRate * deltaTime;
        } else {
            boid.setSprinting(false);
        }

        // Always regenerate at base rate
        stamina += config.boidStaminaRegenBaseRate * deltaTime;

        // Extra bonus near a food attractor
        if (!attractors.isEmpty()) {
            float perceptionSq = config.perceptionRadius * config.perceptionRadius;
            for (int a = 0; a < attractors.size(); a++) {
                float dx = boid.getPosition().x() - attractors.get(a).position().x();
                float dy = boid.getPosition().y() - attractors.get(a).position().y();
                if (dx * dx + dy * dy < perceptionSq) {
                    stamina += config.boidStaminaRegenNearFoodBonus * deltaTime;
                    break;
                }
            }
        }

        boid.setStamina(MathUtils.clamp(stamina, 0f, config.boidStaminaMax));
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
        boids.add(new Boid(nextBoidId++, pos, vel, config.boidStaminaMax));
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

    public void addPredator(Predator pred) {
        predators.add(pred);
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
}
