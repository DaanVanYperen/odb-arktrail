package net.mostlyoriginal.game.system.ui;

import com.artemis.Aspect;
import com.artemis.Entity;
import com.artemis.annotations.Wire;
import com.artemis.managers.GroupManager;
import com.artemis.systems.EntityProcessingSystem;
import com.artemis.utils.EntityBuilder;
import com.badlogic.gdx.graphics.Color;
import net.mostlyoriginal.api.component.basic.Bounds;
import net.mostlyoriginal.api.component.basic.Pos;
import net.mostlyoriginal.api.utils.EntityUtil;
import net.mostlyoriginal.game.component.ui.*;
import net.mostlyoriginal.game.manager.EntityFactorySystem;

/**
 * Responsible for serving and processing dilemmas.
 *
 * @author Daan van Yperen
 */
@Wire
public class DilemmaSystem extends EntityProcessingSystem {

    public static final String DILEMMA_GROUP = "dilemma";
    public static final int ROW_HEIGHT = 9;
    EntityFactorySystem efs;

    public static final Color COLOR_DILEMMA = Color.valueOf("6AD7ED");
    public static final String COLOR_RAW_BRIGHT = "E7E045";
    public static final String COLOR_RAW_DIMMED = "FDF1AA";
    private boolean dilemmaActive;
    private GroupManager groupManager;


    public DilemmaSystem() {
        super(Aspect.getAspectForAll(Pos.class, DilemmaChoice.class));
    }

    public Entity createLabel(int x, int y, Color color, String text) {
        return new EntityBuilder(world).with(
                new Pos(x, y),
                new Label(text, color)
        ).group(DILEMMA_GROUP).build();
    }

    private Entity createOption(int x, int y, String text, ButtonListener listener) {
        return new EntityBuilder(world).with(
                new Pos(x, y),
                new Label(text),
                new Bounds(0, -8, text.length() * 8, 0),
                new Clickable(),
                new Button(COLOR_RAW_DIMMED, COLOR_RAW_BRIGHT, "FFFFFF", listener)
        )
                .group(DILEMMA_GROUP).build();
    }

    public boolean isDilemmaActive() {
        return dilemmaActive;
    }

    @Override
    protected void initialize() {
        super.initialize();
    }

    /** Spawn a random dilemma. */
    public void randomDilemma() {
        if (!dilemmaActive) {
            createLabel(10, 10 + ROW_HEIGHT * 4, COLOR_DILEMMA, "Captain, ensign Jovoc");
            createLabel(10, 10 + ROW_HEIGHT * 3, COLOR_DILEMMA, "contracted a brainslug!");
            createOption(10, 10 + ROW_HEIGHT * 2, "[DUMP HIM OUT OF AIRLOCK]", new ButtonListener() {
                @Override
                public void run() {
                    stopDilemma();
                }
            });
            createOption(10, 10 + ROW_HEIGHT, "[DO NOTHING]", new ButtonListener() {
                @Override
                public void run() {
                    stopDilemma();
                }
            });
            dilemmaActive = true;
        }
    }

    /** Remove active dilemma from screen. */
    private void stopDilemma() {
        EntityUtil.safeDeleteAll(groupManager.getEntities(DILEMMA_GROUP));
        dilemmaActive = false;
    }


    @Override
    protected void process(Entity e) {

    }

    public void finishDilemma() {
        createLabel(10, 10 + ROW_HEIGHT * 4, COLOR_DILEMMA, "[VICTORY CONDITION REACHED. YAY.]");
        dilemmaActive=true;
    }
}
