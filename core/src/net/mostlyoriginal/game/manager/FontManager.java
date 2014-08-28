package net.mostlyoriginal.game.manager;

import com.artemis.Manager;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.TextureRegion;

/**
 * @author Daan van Yperen
 */
public class FontManager extends Manager {

    public BitmapFont font;
    public BitmapFont bigFont;

    @Override
    protected void initialize() {
        super.initialize();
        font = new BitmapFont(Gdx.files.internal("5x5.fnt"), (TextureRegion)null, false);
        font.setColor(1f, 1f, 1f, 1f);
        bigFont = new BitmapFont(Gdx.files.internal("5x5.fnt"), false);
        bigFont.setColor(1f, 1f, 1f, 1f);
        bigFont.setScale(2);
    }
}
