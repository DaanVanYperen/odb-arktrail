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
import net.mostlyoriginal.game.MyGame;
import net.mostlyoriginal.game.component.ui.*;
import net.mostlyoriginal.game.manager.EntityFactorySystem;
import net.mostlyoriginal.game.system.ship.InventorySystem;
import net.mostlyoriginal.game.system.ship.ProductionSimulationSystem;

/**
 * Responsible for serving and processing dilemmas.
 *
 * @author Daan van Yperen
 */
@Wire
public class DilemmaSystem extends EntityProcessingSystem {

    public static final String DILEMMA_GROUP = "dilemma";
    public static final int ROW_HEIGHT = 9;
    public static final String GIVE_UP = "[Give up, Tow back to earth]";
    public static final String DONT_GIVE_UP = "[Never give up! never surrender!]";
    EntityFactorySystem efs;

    public static final Color COLOR_DILEMMA = Color.valueOf("6AD7ED");
    public static final String COLOR_RAW_BRIGHT = "E7E045";
    public static final String COLOR_RAW_DIMMED = "FDF1AA";
    private boolean dilemmaActive;
    private GroupManager groupManager;
    private InventorySystem inventorySystem;
    private ProductionSimulationSystem productionSimulationSystem;


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
        tutorialDilemma();
    }


    private void startDilemma(Dilemma dilemma) {
        if ( !dilemmaActive ) {
            dilemmaActive = true;
            if (dilemma.getText1() != null) {
                createLabel(10, 10 + ROW_HEIGHT * 4, COLOR_DILEMMA, dilemma.getText1());
            }
            if (dilemma.getText2() != null) {
                createLabel(10, 10 + ROW_HEIGHT * 3, COLOR_DILEMMA, dilemma.getText2());
            }
            if (dilemma.getOption1() != null) {
                createOption(10, 10 + ROW_HEIGHT * 2, dilemma.getOption1(), dilemma.getListener1());
            }
            if (dilemma.getOption2() != null) {
                createOption(10, 10 + ROW_HEIGHT, dilemma.getOption2(), dilemma.getListener2());
            }
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

    public void tutorialDilemma() {
        startDilemma(new Dilemma("Fresh out of space dock, ", "your ark is ready to embark.", "[Pedal to the medal!]",
                new ChainDilemma(
                        new Dilemma("You will have to finish it in transit.", "Here are some resources to bridge the gap.", "[Thanks!!]", new PayoutListener(
                                InventorySystem.Resource.FUEL,
                                InventorySystem.Resource.FUEL,
                                InventorySystem.Resource.FUEL,
                                InventorySystem.Resource.FOOD,
                                InventorySystem.Resource.FOOD,
                                InventorySystem.Resource.FOOD,
                                InventorySystem.Resource.CREWMEMBER,
                                InventorySystem.Resource.CREWMEMBER,
                                InventorySystem.Resource.CREWMEMBER,
                                InventorySystem.Resource.CREWMEMBER) )
                ) ));
    }

    /** Spawn a random dilemma. */
    public void randomDilemma() {
            startDilemma(new Dilemma("Captain, ensign Jovoc", "contracted a brainslug!", "[DUMP HIM OUT OF AIRLOCK]", new ButtonListener() {
                @Override
                public void run() {
                    stopDilemma();
                }
            }, "[DO NOTHING]", new ButtonListener() {
                @Override
                public void run() {
                    stopDilemma();
                }
            })) ;
    }

    /** Victory! :D */
    public void victoryDilemma() {
        startDilemma(new Dilemma("You have succesfully reached your destination.", "[YAY! Play again.]", new RestartListener() ));
    }

    /** Out of gas. :( */
    public void outOfGasDilemma() {
        startDilemma(new Dilemma("OUT OF GAS. BOO.", DONT_GIVE_UP, new CloseDilemmaListener(),GIVE_UP, new RestartListener() ));
    }

    /** No pilots remain. :( */
    public void noPilotsDilemma() {
        startDilemma(new Dilemma("Nobody left to pilot the ship!", DONT_GIVE_UP, new CloseDilemmaListener(), GIVE_UP, new RestartListener() ));
    }

    /** Just closes dilemma, no action */
    private class CloseDilemmaListener extends ButtonListener {
        @Override
        public void run() {
            stopDilemma();
        }
    }

    private static class RestartListener extends ButtonListener {
        @Override
        public void run() {
            MyGame.getInstance().restart();
        }
    }

    private static class Dilemma {
        private String text1;
        private String text2;
        private final String option1;
        private final ButtonListener listener1;
        private String option2;
        private ButtonListener listener2;

        public Dilemma(String text1, String text2, String option1, ButtonListener listener1, String option2, ButtonListener listener2) {
            this.text1 = text1;
            this.text2 = text2;
            this.option1 = option1;
            this.listener1 = listener1;
            this.option2 = option2;
            this.listener2 = listener2;
        }

        public Dilemma(String text2, String option1, ButtonListener listener1) {
            this.text2 = text2;
            this.option1 = option1;
            this.listener1 = listener1;
        }

        public Dilemma(String text2, String option1, ButtonListener listener1, String option2, ButtonListener listener2) {
            this.text2 = text2;
            this.option1 = option1;
            this.listener1 = listener1;
            this.option2 = option2;
            this.listener2 = listener2;
        }

        public Dilemma(String text1, String text2, String option1, ButtonListener listener1) {
            this.text1 = text1;
            this.text2 = text2;
            this.option1 = option1;
            this.listener1 = listener1;
        }

        public String getText1() {
            return text1;
        }

        public String getText2() {
            return text2;
        }

        public String getOption1() {
            return option1;
        }

        public ButtonListener getListener1() {
            return listener1;
        }

        public String getOption2() {
            return option2;
        }

        public ButtonListener getListener2() {
            return listener2;
        }
    }

    /** Spawn specified resources at mouse cursor when picking this option. */
    private class PayoutListener extends CloseDilemmaListener {
        private final InventorySystem.Resource[] resources;

        public PayoutListener(InventorySystem.Resource ... resources )
        {
            this.resources = resources;
        }

        @Override
        public void run() {
            super.run();
            for (InventorySystem.Resource resource : resources) {
                productionSimulationSystem.spawnCollectibleNearMouse(resource);
            }
        }
    }

    /** Run another dilemma after this one. */
    private class ChainDilemma extends ButtonListener {
        private final Dilemma dilemma;

        public ChainDilemma(Dilemma dilemma) {
            super();
            this.dilemma = dilemma;
        }

        @Override
        public void run() {
            super.run();
            stopDilemma();
            startDilemma(dilemma);
        }
    }
}
