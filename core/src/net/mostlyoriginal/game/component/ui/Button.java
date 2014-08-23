package net.mostlyoriginal.game.component.ui;

import com.artemis.Component;

/**
 * @author Daan van Yperen
 */
public class Button extends Component {

    public String animDefault;
    public String animClicked;
    public String animHover;

    public Button(String animPrefix) {
        this.animDefault = animPrefix + "-up";
        this.animHover = animPrefix + "-hover";
        this.animClicked = animPrefix + "-down";
    }
}
