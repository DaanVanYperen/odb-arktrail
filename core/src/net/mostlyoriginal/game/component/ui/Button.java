package net.mostlyoriginal.game.component.ui;

import com.artemis.Component;
import com.badlogic.gdx.graphics.Color;
import net.mostlyoriginal.api.utils.EntityReference;

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
    public boolean autoclick = false;
    public float autoclickCooldown;
    public boolean hideIfDisabled = false;
    public EntityReference transientIcon;
    public Color color = new Color(Color.WHITE);

    /** create a button event handler without effects. */
    public Button(ButtonListener listener) {
        this.listener = listener;
    }

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
