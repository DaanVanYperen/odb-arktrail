package net.mostlyoriginal.game.system.ui;

import com.artemis.Aspect;
import com.artemis.ComponentMapper;
import com.artemis.Entity;
import com.artemis.annotations.Wire;
import com.artemis.systems.EntityProcessingSystem;
import net.mostlyoriginal.api.component.graphics.Anim;
import net.mostlyoriginal.game.component.ui.Button;
import net.mostlyoriginal.game.component.ui.Clickable;

/**
 * @author Daan van Yperen
 */
@Wire
public class ButtonSystem extends EntityProcessingSystem {

    protected ComponentMapper<Clickable> mClickable;

    protected ComponentMapper<Button> mButton;
    protected ComponentMapper<Anim> mAnim;

    public ButtonSystem() {
        super(Aspect.getAspectForAll(Button.class, Clickable.class, Anim.class));
    }

    @Override
    protected void process(Entity e) {
            updateAnim(e);
    }

    private void updateAnim(Entity e) {
        mAnim.get(e).id = getNewAnimId(e);
    }

    private String getNewAnimId(Entity e ) {
        final Clickable clickable = mClickable.get(e);
        final Button button = mButton.get(e);

        switch (clickable.state)
        {
            case HOVER:
                return button.animHover;
            case CLICKED:
                return button.animClicked;
            default:
                return button.animDefault;
        }
    }
}
