package net.mostlyoriginal.game.system.ui;

import com.artemis.Aspect;
import com.artemis.Entity;
import com.artemis.annotations.Wire;
import com.artemis.systems.EntityProcessingSystem;
import com.artemis.utils.EntityBuilder;
import com.badlogic.gdx.graphics.Color;
import net.mostlyoriginal.api.component.basic.Bounds;
import net.mostlyoriginal.api.component.basic.Pos;
import net.mostlyoriginal.game.component.ui.Button;
import net.mostlyoriginal.game.component.ui.Clickable;
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
    public static final String COLOR_RAW_BRIGHT = "E7E045";
    public static final String COLOR_RAW_DIMMED = "FDF1AA";


    public DilemmaSystem() {
        super(Aspect.getAspectForAll(Pos.class, DilemmaChoice.class));
    }

    public Entity createLabel(int x, int y, Color color, String text) {
        return new EntityBuilder(world).with(
                new Pos(x, y),
                new Label(text, color)
        ).group(DILEMMA_GROUP).build();
    }

    private Entity createOption(int x, int y, String text) {
        return new EntityBuilder(world).with(
                new Pos(x, y),
                new Label(text),
                new Bounds(0, -8, text.length() * 8, 0),
                new Clickable( ),
                new Button( COLOR_RAW_DIMMED, COLOR_RAW_BRIGHT, "FFFFFF", new Runnable(){
                    @Override
                    public void run() {

                    }
                })
        ).group(DILEMMA_GROUP).build();
    }

    @Override
    protected void initialize() {
        super.initialize();

        createLabel(10, 10 + ROW_HEIGHT * 4, COLOR_DILEMMA, "Captain, ensign Jovoc");
        createLabel(10, 10 + ROW_HEIGHT * 3, COLOR_DILEMMA, "contracted a brainslug!");
        createOption(10, 10 + ROW_HEIGHT * 2, "[DUMP HIM OUT OF AIRLOCK]");
        createOption(10, 10 + ROW_HEIGHT * 1, "[DO NOTHING]");
    }


    @Override
    protected void process(Entity e) {

    }
}
