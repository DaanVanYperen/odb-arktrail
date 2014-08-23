package net.mostlyoriginal.game.manager;

import com.artemis.Manager;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.BitmapFont;

/**
 * @author Daan van Yperen
 */
public class FontManager extends Manager {

    public BitmapFont font;

    @Override
    protected void initialize() {
        super.initialize();
        font = new BitmapFont(Gdx.files.internal("5x5.fnt"), false);
        font.setColor(1f, 1f, 1f, 1f);
    }
}
