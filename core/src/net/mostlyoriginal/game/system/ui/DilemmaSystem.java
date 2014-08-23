package net.mostlyoriginal.game.system.ui;

import com.artemis.Aspect;
import com.artemis.Entity;
import com.artemis.annotations.Wire;
import com.artemis.systems.EntityProcessingSystem;
import com.artemis.utils.EntityBuilder;
import com.badlogic.gdx.graphics.Color;
import net.mostlyoriginal.api.component.basic.Pos;
import net.mostlyoriginal.game.component.ui.DilemmaChoice;
import net.mostlyoriginal.game.component.ui.Label;
import net.mostlyoriginal.game.manager.EntityFactorySystem;

/**
 * Responsible for serving and processing dilemmas.
 *
 * @author Daan van Yperen
 */
@Wire
public class DilemmaSystem extends EntityProcessingSystem {

    public static final String DILEMMA_GROUP = "dilemma";
    public static final int ROW_HEIGHT = 8;
    EntityFactorySystem efs;

    public static final Color COLOR_DILEMMA = Color.valueOf("6AD7ED");
    public static final Color COLOR_OPTION_ODD  = Color.valueOf("E7E045");
    public static final Color COLOR_OPTION_EVEN = Color.valueOf("FDF1AA");


    public DilemmaSystem() {
        super(Aspect.getAspectForAll(Pos.class, DilemmaChoice.class));
    }

    public Entity createLabel(int x, int y, Color color, String text) {
        return new EntityBuilder(world).with(
                new Pos(x, y),
                new Label(text, color)
        ).group(DILEMMA_GROUP).build();
    }

    @Override
    protected void initialize() {
        super.initialize();

        createLabel(10, 10 + ROW_HEIGHT, COLOR_DILEMMA, "Captain, ensign Jovoc");
        createLabel(10, 10, COLOR_DILEMMA, "contracted a brainslug!");
    }

    @Override
    protected void process(Entity e) {

    }
}
