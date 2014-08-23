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
    public float cooldown=0;

    public Button(String animDefault, String animHover, String animClicked, Runnable runnable) {
        this.animHover = animHover;
        this.animClicked = animClicked;
        this.animDefault = animDefault;
        this.runnable = runnable;
    }

    public Button(String animPrefix, Runnable runnable) {
        this.runnable = runnable;
        this.animDefault = animPrefix + "-up";
        this.animHover = animPrefix + "-hover";
        this.animClicked = animPrefix + "-down";
    }
}
