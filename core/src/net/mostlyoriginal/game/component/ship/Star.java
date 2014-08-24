package net.mostlyoriginal.game.component.ship;

import com.artemis.Component;
import com.badlogic.gdx.math.MathUtils;

/**
 * @author Daan van Yperen
 */
public class Star extends Component {
    public final String animId[] = new String[3];
    public final float speedFactor = 1f + MathUtils.random(-0.2f,0.5f);

    public Star(int kind) {
        animId[0] = "star-" + kind + "-0";
        animId[1] = "star-" + kind + "-1";
        animId[2] = "star-" + kind + "-2";
    }
}
