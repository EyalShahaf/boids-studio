package com.flocklab.render;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Matrix4;

/**
 * Renders a gradient background for the simulation world.
 */
public class BackgroundRenderer {

    public Color topColor = new Color(0.04f, 0.04f, 0.08f, 1f);
    public Color bottomColor = new Color(0.12f, 0.15f, 0.22f, 1f);

    public void render(ShapeRenderer shapeRenderer, float width, float height, Matrix4 projectionMatrix) {
        shapeRenderer.setProjectionMatrix(projectionMatrix);
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);

        // Draw a full-screen quad with a vertical gradient
        shapeRenderer.rect(0, 0, width, height,
                bottomColor, bottomColor,
                topColor, topColor);

        shapeRenderer.end();
    }
}
