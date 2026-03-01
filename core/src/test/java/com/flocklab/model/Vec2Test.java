package com.flocklab.model;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class Vec2Test {

    @Test
    void testAdd() {
        Vec2 v1 = new Vec2(2, 3);
        Vec2 v2 = new Vec2(-1, 5);
        Vec2 result = v1.add(v2);

        assertEquals(1, result.x(), 0.001);
        assertEquals(8, result.y(), 0.001);
    }

    @Test
    void testSub() {
        Vec2 v1 = new Vec2(5, 5);
        Vec2 v2 = new Vec2(2, 3);
        Vec2 result = v1.sub(v2);

        assertEquals(3, result.x(), 0.001);
        assertEquals(2, result.y(), 0.001);
    }

    @Test
    void testMagnitude() {
        Vec2 v = new Vec2(3, 4);
        assertEquals(5, v.magnitude(), 0.001);
    }

    @Test
    void testNormalize() {
        Vec2 v = new Vec2(0, 10);
        Vec2 normal = v.normalize();

        assertEquals(0, normal.x(), 0.001);
        assertEquals(1, normal.y(), 0.001);
        assertEquals(1, normal.magnitude(), 0.001);
    }

    @Test
    void testLimit() {
        Vec2 v = new Vec2(0, 10);
        Vec2 limited = v.limit(5);

        assertEquals(0, limited.x(), 0.001);
        assertEquals(5, limited.y(), 0.001);

        // Limiting below magnitude changes nothing
        Vec2 v2 = new Vec2(0, 3);
        Vec2 limited2 = v2.limit(5);
        assertEquals(3, limited2.magnitude(), 0.001);
    }
}
