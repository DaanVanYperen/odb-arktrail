package net.mostlyoriginal.game.system.ui;

import com.artemis.Aspect;
import com.artemis.ComponentMapper;
import com.artemis.Entity;
import com.artemis.annotations.Wire;
import com.artemis.systems.EntityProcessingSystem;
import com.badlogic.gdx.graphics.Color;
import net.mostlyoriginal.api.component.basic.Bounds;
import net.mostlyoriginal.api.component.graphics.Anim;
import net.mostlyoriginal.game.component.ui.Button;
import net.mostlyoriginal.game.component.ui.Clickable;
import net.mostlyoriginal.game.component.ui.Label;

/**
 * @author Daan van Yperen
 */
@Wire
public class ButtonSystem extends EntityProcessingSystem {

    public static final float COOLDOWN_AFTER_BUTTON_CLICK = 0.15f;
    protected ComponentMapper<Clickable> mClickable;

    protected ComponentMapper<Label> mLabel;
    protected ComponentMapper<Button> mButton;
    protected ComponentMapper<Anim> mAnim;

    public ButtonSystem() {
        super(Aspect.getAspectForAll(Button.class, Clickable.class, Bounds.class).one(Anim.class, Label.class));
    }

    @Override
    protected void process(Entity e) {
        updateAnim(e);
    }

    private void updateAnim(Entity e) {
        final String id = getNewAnimId(e);

        if (mAnim.has(e)) {
            mAnim.get(e).id = id;
        } else if (mLabel.has(e)) {
            mLabel.get(e).color = Color.valueOf(id);
        }
    }

    private String getNewAnimId(Entity e) {
        final Clickable clickable = mClickable.get(e);
        final Button button = mButton.get(e);

        // disable the button temporarily after use to avoid trouble.
        if (button.cooldown >= 0) {
            button.cooldown -= world.delta;
            return button.animClicked;
        }

        // gray out disabled items. @todo separate.
        boolean active = button.listener.enabled();
        if (mAnim.has(e)) {
            Anim anim = mAnim.get(e);
            anim.color.r = active ? 1f : 0.5f;
            anim.color.g = active ? 1f : 0.5f;
            anim.color.b = active ? 1f : 0.5f;
            if ( !active ) {
                return button.animDefault;
            }
        }

        switch (clickable.state) {
            case HOVER:
                return button.animHover;
            case CLICKED:
                button.cooldown = COOLDOWN_AFTER_BUTTON_CLICK;
                triggerButton(button);
                return button.animClicked;
            default:
                return button.animDefault;
        }
    }

    private void triggerButton(Button button) {
        if (button.listener.enabled()) {
            button.listener.run();
        }
    }
}
