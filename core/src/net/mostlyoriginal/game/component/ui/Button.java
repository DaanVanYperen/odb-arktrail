package net.mostlyoriginal.game.component.ui;

import com.artemis.Component;

/**
 * @author Daan van Yperen
 */
public class Button extends Component {

    public final Runnable runnable;
    public String animDefault;
    public String animClicked;
    public String animHover;
    public float cooldown;

    public Button(String animPrefix, Runnable runnable) {
        this.runnable = runnable;
        this.animDefault = animPrefix + "-up";
        this.animHover = animPrefix + "-hover";
        this.animClicked = animPrefix + "-down";
    }
}
