package net.mostlyoriginal.api.utils;

import com.badlogic.gdx.graphics.g2d.TextureRegion;
import net.mostlyoriginal.api.component.basic.Bounds;

/**
 * @author Daan van Yperen
 */
public class TextureRegionUtils {

    /** Return bounds of TextureRegion */
    public static Bounds boundsOf(TextureRegion frame) {
        return new Bounds(0,0, frame.getRegionWidth(), frame.getRegionHeight());
    }
}
