package com.flocklab.ui;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.ui.Label.LabelStyle;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Slider.SliderStyle;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton.TextButtonStyle;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;

/**
 * Creates a simple programmatic skin for Scene2D UI without needing external
 * JSON/image assets.
 */
public class SkinFactory {

    public static Skin createSkin() {
        Skin skin = new Skin();

        // Basic font
        BitmapFont font = new BitmapFont();
        skin.add("default", font);

        // Colors
        Color panelColor = new Color(0.1f, 0.1f, 0.15f, 0.8f);
        Color buttonColor = new Color(0.2f, 0.25f, 0.35f, 1f);
        Color buttonDownColor = new Color(0.15f, 0.2f, 0.3f, 1f);
        Color sliderBgColor = new Color(0.3f, 0.3f, 0.4f, 1f);
        Color sliderKnobColor = new Color(0.5f, 0.7f, 0.9f, 1f);

        // Textures mapped to drawables
        skin.add("panel_bg", createColorDrawable(panelColor));
        skin.add("button_up", createColorDrawable(buttonColor));
        skin.add("button_down", createColorDrawable(buttonDownColor));
        skin.add("slider_bg", createColorDrawable(sliderBgColor, 100, 4));
        skin.add("slider_knob", createColorDrawable(sliderKnobColor, 12, 12));

        // Styles
        LabelStyle labelStyle = new LabelStyle();
        labelStyle.font = font;
        labelStyle.fontColor = Color.WHITE;
        skin.add("default", labelStyle);

        TextButtonStyle textButtonStyle = new TextButtonStyle();
        textButtonStyle.up = skin.getDrawable("button_up");
        textButtonStyle.down = skin.getDrawable("button_down");
        textButtonStyle.font = font;
        textButtonStyle.fontColor = Color.WHITE;
        skin.add("default", textButtonStyle);

        SliderStyle sliderStyle = new SliderStyle();
        sliderStyle.background = skin.getDrawable("slider_bg");
        sliderStyle.knob = skin.getDrawable("slider_knob");
        skin.add("default-horizontal", sliderStyle);

        return skin;
    }

    private static TextureRegionDrawable createColorDrawable(Color color) {
        return createColorDrawable(color, 1, 1);
    }

    private static TextureRegionDrawable createColorDrawable(Color color, int width, int height) {
        Pixmap pixmap = new Pixmap(width, height, Pixmap.Format.RGBA8888);
        pixmap.setColor(color);
        pixmap.fill();
        Texture texture = new Texture(pixmap);
        pixmap.dispose();
        return new TextureRegionDrawable(new TextureRegion(texture));
    }
}
