package net.mostlyoriginal.game.component.ui;

import com.artemis.Component;

/**
 * @author Daan van Yperen
 */
public class Label extends Component {

    public String text;
    public Align align = Align.LEFT;

    /** target layer, higher is in front, lower is behind. */
    public float scale = 1f;

    public Label(String text) {
        this.text = text;
    }

    public enum Align {
        LEFT, RIGHT;
    }
}
