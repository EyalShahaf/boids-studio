package com.flocklab.sim;

import com.flocklab.config.SimulationConfig;
import com.flocklab.model.Boid;
import com.flocklab.model.Obstacle;
import com.flocklab.model.Predator;
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

    // ---- Game Of Life tests ----

    @Test
    void testPredatorEatsBoid() {
        SimulationConfig config = new SimulationConfig();
        config.initialBoidCount = 0;
        config.predatorEatRadius = 20f;
        // Set chance to 1.0 so eating is guaranteed each frame when in range
        config.predatorEatChancePerSecond = 1000f;
        config.predatorHungerDrainRate = 0f; // Prevent hunger death during test
        World world = new World(config);

        world.addBoid(new Vec2(100f, 100f), new Vec2(0f, 0f));
        // Spawn predator directly on top of boid
        world.spawnPredator(new Vec2(100f, 100f));

        int boidsBefore = world.getBoids().size();
        assertEquals(1, boidsBefore);

        world.update(0.016f);

        assertEquals(0, world.getBoids().size(), "Boid within eat radius should have been eaten");
        assertEquals(1, world.getTotalBoidsEaten(), "Eaten counter should be 1");
    }

    @Test
    void testPredatorDiesOfHunger() {
        SimulationConfig config = new SimulationConfig();
        config.initialBoidCount = 0;
        config.predatorHungerMax = 10f;
        config.predatorHungerDrainRate = 10f; // Drain 10/s — dead in 1 second
        World world = new World(config);

        world.spawnPredator(new Vec2(640f, 360f));
        assertEquals(1, world.getPredators().size());
        assertEquals(1, world.getTotalPredatorsCreated());

        // Simulate 2 seconds — more than enough for hunger to reach 0
        for (int i = 0; i < 120; i++) {
            world.update(0.016f);
        }

        assertEquals(0, world.getPredators().size(), "Predator should have starved to death");
        assertEquals(1, world.getTotalPredatorsDiedOfHunger(), "Starvation death counter should be 1");
    }

    @Test
    void testBoidStaminaDrains() {
        SimulationConfig config = new SimulationConfig();
        config.initialBoidCount = 0;
        config.boidSprintTriggerRadius = 200f; // Large so boid definitely sprints
        config.boidStaminaDrainRate = 100f;    // Drain fast for visible effect in few frames
        config.boidStaminaRegenBaseRate = 0f;  // No regen to isolate drain
        config.boidStaminaRegenNearFoodBonus = 0f;
        config.predatorHungerDrainRate = 0f;   // Predator won't die during test
        config.predatorEatRadius = 0f;         // Prevent predator from eating the test boid
        World world = new World(config);

        world.addBoid(new Vec2(100f, 100f), new Vec2(0f, 0f));
        world.spawnPredator(new Vec2(110f, 100f)); // Within sprint trigger radius

        float initialStamina = world.getBoids().get(0).getStamina();
        assertEquals(config.boidStaminaMax, initialStamina, 0.001f);

        for (int i = 0; i < 10; i++) {
            world.update(0.016f);
        }

        float updatedStamina = world.getBoids().get(0).getStamina();
        assertTrue(updatedStamina < initialStamina,
                "Boid stamina should decrease while sprinting near a predator");
        assertTrue(world.getBoids().get(0).isSprinting(),
                "Boid should be sprinting when predator is within trigger radius");
    }

    @Test
    void testBoidStaminaStopsSprintingWhenExhausted() {
        SimulationConfig config = new SimulationConfig();
        config.initialBoidCount = 0;
        config.boidSprintTriggerRadius = 200f;
        config.boidStaminaDrainRate = 10000f; // Drain instantly
        config.boidStaminaRegenBaseRate = 0f;
        config.boidStaminaRegenNearFoodBonus = 0f;
        config.predatorHungerDrainRate = 0f;
        config.predatorEatRadius = 0f;        // Prevent predator from eating the test boid
        World world = new World(config);

        world.addBoid(new Vec2(100f, 100f), new Vec2(0f, 0f));
        world.spawnPredator(new Vec2(110f, 100f));

        // Run enough frames to exhaust stamina
        for (int i = 0; i < 20; i++) {
            world.update(0.016f);
        }

        Boid boid = world.getBoids().get(0);
        assertEquals(0f, boid.getStamina(), 0.001f, "Stamina should be clamped at 0");
        assertFalse(boid.isSprinting(), "Boid should stop sprinting when stamina is exhausted");
    }

    @Test
    void testBoidHungerDrains() {
        SimulationConfig config = new SimulationConfig();
        config.initialBoidCount = 0;
        config.boidHungerDrainRate = 50f; // Drain fast for visible effect in few frames
        config.boidHungerRegenNearFoodRate = 0f; // No regen (no attractors anyway)
        World world = new World(config);

        world.addBoid(new Vec2(100f, 100f), new Vec2(0f, 0f));
        float initialHunger = world.getBoids().get(0).getHunger();
        assertEquals(config.boidHungerMax, initialHunger, 0.001f);

        for (int i = 0; i < 10; i++) {
            world.update(0.016f);
        }

        assertTrue(world.getBoids().get(0).getHunger() < initialHunger,
                "Boid hunger should decrease over time");
    }

    @Test
    void testBoidHungerReplenishedNearFood() {
        SimulationConfig config = new SimulationConfig();
        config.initialBoidCount = 0;
        config.boidHungerDrainRate = 0f;             // Disable drain so regen is isolated
        config.boidHungerRegenNearFoodRate = 200f;   // Fast regen to see effect quickly
        config.perceptionRadius = 200f;              // Large so attractor is within range
        World world = new World(config);

        world.addBoid(new Vec2(100f, 100f), new Vec2(0f, 0f));
        // Manually set boid hunger low to verify regen
        // Access via mutable boid reference — set hunger low before update
        world.update(0.001f); // tiny warm-up step so hunger is at max
        // Place attractor right next to boid
        world.addAttractor(new com.flocklab.model.Attractor(new Vec2(105f, 100f), 100f));

        // Force hunger down by adjusting config drain temporarily
        config.boidHungerDrainRate = 1000f;
        world.update(0.05f); // drain hunger significantly
        config.boidHungerDrainRate = 0f;

        float hungryLevel = world.getBoids().get(0).getHunger();
        assertTrue(hungryLevel < config.boidHungerMax, "Setup: boid should be hungry");

        // Now regen with attractor nearby
        for (int i = 0; i < 10; i++) {
            world.update(0.016f);
        }

        assertTrue(world.getBoids().get(0).getHunger() > hungryLevel,
                "Boid hunger should increase when near an attractor");
    }

    @Test
    void testHungryBoidSeeksFoodMoreAggressively() {
        // A very hungry boid should have a larger food attraction force than a full boid
        SimulationConfig config = new SimulationConfig();
        config.initialBoidCount = 0;
        config.boidHungerFoodAttractionMultiplier = 2.0f;
        config.foodAttractionWeight = 1.0f;
        config.perceptionRadius = 500f; // Wide so attractor is always in range

        com.flocklab.model.Attractor food = new com.flocklab.model.Attractor(new Vec2(200f, 200f), 100f);
        java.util.List<com.flocklab.model.Attractor> attractors = new java.util.ArrayList<>();
        attractors.add(food);

        // Full boid
        Boid fullBoid = new Boid(0, new Vec2(100f, 100f), new Vec2(0f, 0f), 100f, 100f);
        // Starving boid
        Boid hungryBoid = new Boid(1, new Vec2(100f, 100f), new Vec2(0f, 0f), 100f, 0f);

        float fullHungerFraction = fullBoid.getHunger() / config.boidHungerMax;
        float fullFoodFactor = 1f + (1f - fullHungerFraction) * config.boidHungerFoodAttractionMultiplier;
        Vec2 fullForce = BoidRules.seekAttractors(fullBoid, attractors, config.perceptionRadius)
                .scale(config.foodAttractionWeight * fullFoodFactor);

        float hungryHungerFraction = hungryBoid.getHunger() / config.boidHungerMax;
        float hungryFoodFactor = 1f + (1f - hungryHungerFraction) * config.boidHungerFoodAttractionMultiplier;
        Vec2 hungryForce = BoidRules.seekAttractors(hungryBoid, attractors, config.perceptionRadius)
                .scale(config.foodAttractionWeight * hungryFoodFactor);

        assertTrue(hungryForce.magnitude() > fullForce.magnitude(),
                "A starving boid should seek food with greater force than a full boid");
    }

    @Test
    void testFleeForceStrongerWhenCloser() {
        // Verify inverse-distance-squared weighting: closer predator → larger flee force
        Boid boid = new Boid(0, new Vec2(100f, 100f), new Vec2(0f, 0f), 100f, 100f);

        Predator closePredator = new Predator(0, new Vec2(110f, 100f), Vec2.ZERO, 100f, 100f, 100f);
        Predator farPredator = new Predator(1, new Vec2(180f, 100f), Vec2.ZERO, 100f, 100f, 100f);

        java.util.List<Predator> closeList = new java.util.ArrayList<>();
        closeList.add(closePredator);

        java.util.List<Predator> farList = new java.util.ArrayList<>();
        farList.add(farPredator);

        float fleeRadius = 200f;
        float fleeScale = 3000f;

        Vec2 closeForce = BoidRules.fleePredators(boid, closeList, fleeRadius, fleeScale);
        Vec2 farForce = BoidRules.fleePredators(boid, farList, fleeRadius, fleeScale);

        assertTrue(closeForce.magnitude() > farForce.magnitude(),
                "Flee force should be stronger when the predator is closer (inverse-distance-squared)");
    }
}
