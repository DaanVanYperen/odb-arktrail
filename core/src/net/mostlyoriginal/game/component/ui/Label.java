package net.mostlyoriginal.game.component.ui;

import com.artemis.Component;
import com.badlogic.gdx.graphics.Color;

/**
 * @author Daan van Yperen
 */
public class Label extends Component {

    public String text;
    public Color color = new Color(Color.WHITE);
    public Align align = Align.LEFT;

    /** target layer, higher is in front, lower is behind. */
    public int layer = 0;
    public float scale = 1f;

    public Label(String text) {
        this.text = text;
    }

    public Label(String text, Color color) {
        this.text = text;
        this.color = color;
    }

    public enum Align {
        LEFT, RIGHT;
    }
}
