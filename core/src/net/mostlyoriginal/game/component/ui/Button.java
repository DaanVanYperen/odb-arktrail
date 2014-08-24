package net.mostlyoriginal.game.component.ui;

import com.artemis.Component;

/**
 * @author Daan van Yperen
 */
public class Button extends Component {

    // not the best design but hey, ludum haste!
    public ButtonListener listener;

    private String animPrefix;
    public String animDefault;
    private ButtonListener buttonListener;
    public String animClicked;
    public String animHover;
    public float cooldown=0;
    public String hint=null;

    public Button(String animDefault, String animHover, String animClicked, ButtonListener listener) {
        this.animHover = animHover;
        this.animClicked = animClicked;
        this.animDefault = animDefault;
        this.listener = listener;
    }

    public Button(String animPrefix, ButtonListener listener, String hint) {
        this.animPrefix = animPrefix;
        this.listener = listener;
        this.hint = hint;
        this.animDefault = animPrefix + "-up";
        this.animHover = animPrefix + "-hover";
        this.animClicked = animPrefix + "-down";
    }
}
