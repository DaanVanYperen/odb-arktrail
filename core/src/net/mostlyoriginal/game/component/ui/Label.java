package net.mostlyoriginal.game.component.ui;

import com.artemis.Component;

/**
 * @author Daan van Yperen
 */
public class Label extends Component {

    public String text;
    public Align align = Align.LEFT;

    /** target layer, higher is in front, lower is behind. */
    public int layer = 0;

    public Label(String text) {
        this.text = text;
    }

    public Label(String text, Align align) {
        this.text = text;
        this.align = align;
    }

    public enum Align {
        LEFT;
    }
}
