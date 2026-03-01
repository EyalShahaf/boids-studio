package com.flocklab.sim;

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

    private SpatialGrid spatialGrid;
    private int nextBoidId = 0;

    public World(SimulationConfig config) {
        this.config = config;
        this.spatialGrid = new SpatialGrid(Math.max(config.perceptionRadius, 50f), config.worldWidth,
                config.worldHeight);

        // Spawn initial boids
        for (int i = 0; i < config.initialBoidCount; i++) {
            float x = (float) (Math.random() * config.worldWidth);
            float y = (float) (Math.random() * config.worldHeight);
            float vx = (float) (Math.random() * 2 - 1);
            float vy = (float) (Math.random() * 2 - 1);

            boids.add(new Boid(nextBoidId++, new Vec2(x, y), new Vec2(vx, vy).setMagnitude(config.maxSpeed)));
        }
    }

    public void update(float deltaTime) {
        // Dynamic grid cell sizing based on perception radius
        float requiredCellSize = Math.max(config.perceptionRadius, 50f);
        spatialGrid = new SpatialGrid(requiredCellSize, config.worldWidth, config.worldHeight);

        for (Boid b : boids) {
            spatialGrid.insert(b);
        }

        for (Boid boid : boids) {
            List<Boid> neighbors = spatialGrid.getNeighbors(boid, config.perceptionRadius);

            // Calculate steerings
            Vec2 separation = BoidRules.separation(boid, neighbors, config.perceptionRadius)
                    .scale(config.separationWeight);
            Vec2 alignment = BoidRules.alignment(boid, neighbors, config.perceptionRadius)
                    .scale(config.alignmentWeight);
            Vec2 cohesion = BoidRules.cohesion(boid, neighbors, config.perceptionRadius).scale(config.cohesionWeight);

            Vec2 avoidObs = BoidRules.avoidObstacles(boid, obstacles, config.perceptionRadius)
                    .scale(config.obstacleAvoidanceWeight);
            Vec2 fleePreds = BoidRules.fleePredators(boid, predators, config.perceptionRadius)
                    .scale(config.predatorFleeWeight);
            Vec2 seekAttr = BoidRules.seekAttractors(boid, attractors, config.perceptionRadius)
                    .scale(config.foodAttractionWeight);

            // Accumulate forces
            boid.applyForce(separation);
            boid.applyForce(alignment);
            boid.applyForce(cohesion);
            boid.applyForce(avoidObs);
            boid.applyForce(fleePreds);
            boid.applyForce(seekAttr);
        }

        // Apply physics
        for (Boid b : boids) {
            b.update(deltaTime, config.maxSpeed, config.worldWidth, config.worldHeight);
        }

        // Update predators
        for (Predator p : predators) {
            // Very simple predator AI: chase center of mass of boids nearby
            List<Boid> nearby = spatialGrid.getNeighbors(new Boid(-1, p.getPosition(), Vec2.ZERO),
                    config.perceptionRadius * 2);
            Vec2 chase = BoidRules.cohesion(new Boid(-1, p.getPosition(), Vec2.ZERO), nearby,
                    config.perceptionRadius * 2);
            p.update(chase, deltaTime, config.worldWidth, config.worldHeight);
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
        boids.add(new Boid(nextBoidId++, pos, vel));
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
}
