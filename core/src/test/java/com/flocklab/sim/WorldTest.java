package com.flocklab.sim;

import com.flocklab.config.SimulationConfig;
import com.flocklab.model.Boid;
import com.flocklab.model.Obstacle;
import com.flocklab.model.Vec2;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class WorldTest {

    @Test
    void testWorldStability() {
        SimulationConfig config = new SimulationConfig();
        config.initialBoidCount = 100;
        World world = new World(config);

        // Step 1000 frames to ensure no position/velocity goes to NaN or Infinity
        for (int i = 0; i < 1000; i++) {
            world.update(0.016f); // ~60fps step
        }

        for (Boid b : world.getBoids()) {
            assertTrue(Float.isFinite(b.getPosition().x()));
            assertTrue(Float.isFinite(b.getPosition().y()));
            assertTrue(Float.isFinite(b.getVelocity().x()));
            assertTrue(Float.isFinite(b.getVelocity().y()));
        }
    }

    @Test
    void testEdgeWrapping() {
        SimulationConfig config = new SimulationConfig();
        config.initialBoidCount = 0;
        config.worldWidth = 100f;
        config.worldHeight = 100f;
        World world = new World(config);

        // Place boid moving out of right edge
        world.addBoid(new Vec2(99f, 50f), new Vec2(10f, 0f));

        // Step enough to cross the boundary
        world.update(0.2f); // pos.x should be 99 + 2 = 101 -> wrap to 1

        Boid b = world.getBoids().get(0);
        assertTrue(b.getPosition().x() < 5f, "Boid should have wrapped to the left side");
        assertTrue(b.getPosition().x() > 0f);
    }

    @Test
    void testSeparation() {
        SimulationConfig config = new SimulationConfig();
        config.initialBoidCount = 0;
        config.separationWeight = 50f; // High separation
        config.alignmentWeight = 0f;
        config.cohesionWeight = 0f;
        config.maxSpeed = 100f;
        World world = new World(config);

        // Place two boids very close, static velocity
        world.addBoid(new Vec2(50f, 50f), new Vec2(0f, 0f));
        world.addBoid(new Vec2(51f, 50f), new Vec2(0f, 0f));

        float initialDistance = world.getBoids().get(0).getPosition().distanceTo(world.getBoids().get(1).getPosition());

        for (int i = 0; i < 5; i++)
            world.update(0.016f);

        float newDistance = world.getBoids().get(0).getPosition().distanceTo(world.getBoids().get(1).getPosition());

        assertTrue(newDistance > initialDistance, "Boids should have moved apart due to separation");
    }

    @Test
    void testCohesion() {
        SimulationConfig config = new SimulationConfig();
        config.initialBoidCount = 0;
        config.separationWeight = 0f;
        config.alignmentWeight = 0f;
        config.cohesionWeight = 50f; // High cohesion
        config.perceptionRadius = 100f;
        World world = new World(config);

        // Place boids spread out
        world.addBoid(new Vec2(20f, 50f), new Vec2(0f, 0f));
        world.addBoid(new Vec2(80f, 50f), new Vec2(0f, 0f));

        float initialDistance = world.getBoids().get(0).getPosition().distanceTo(world.getBoids().get(1).getPosition());

        for (int i = 0; i < 5; i++)
            world.update(0.016f);

        float newDistance = world.getBoids().get(0).getPosition().distanceTo(world.getBoids().get(1).getPosition());

        assertTrue(newDistance < initialDistance, "Boids should have moved closer due to cohesion");
    }

    @Test
    void testObstacleAvoidance() {
        SimulationConfig config = new SimulationConfig();
        config.initialBoidCount = 0;
        config.separationWeight = 0f;
        config.alignmentWeight = 0f;
        config.cohesionWeight = 0f;
        config.obstacleAvoidanceWeight = 100f; // High avoidance
        config.perceptionRadius = 100f;
        World world = new World(config);

        // Boid heading straight right, slightly offset on Y to allow deflection
        world.addBoid(new Vec2(10f, 51f), new Vec2(50f, 0f));

        // Obstacle straight ahead
        world.addObstacle(new Obstacle(new Vec2(50f, 50f), 10f));

        assertEquals(0f, world.getBoids().get(0).getVelocity().y());

        for (int i = 0; i < 10; i++)
            world.update(0.016f);

        assertTrue(Math.abs(world.getBoids().get(0).getVelocity().y()) > 0.01f,
                "Boid should have deflected to avoid the obstacle");
    }
}
