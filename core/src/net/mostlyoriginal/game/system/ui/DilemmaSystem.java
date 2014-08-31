package net.mostlyoriginal.game.system.ui;

import com.artemis.Aspect;
import com.artemis.ComponentMapper;
import com.artemis.Entity;
import com.artemis.annotations.Wire;
import com.artemis.managers.GroupManager;
import com.artemis.systems.EntityProcessingSystem;
import com.artemis.utils.EntityBuilder;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.MathUtils;
import net.mostlyoriginal.api.component.basic.Bounds;
import net.mostlyoriginal.api.component.basic.Pos;
import net.mostlyoriginal.api.component.graphics.Renderable;
import net.mostlyoriginal.api.utils.EntityUtil;
import net.mostlyoriginal.api.utils.GdxUtil;
import net.mostlyoriginal.game.G;
import net.mostlyoriginal.game.MyGame;
import net.mostlyoriginal.game.component.ship.CrewMember;
import net.mostlyoriginal.game.component.ui.*;
import net.mostlyoriginal.game.manager.EntityFactorySystem;
import net.mostlyoriginal.game.system.ship.*;
import net.mostlyoriginal.game.system.tutorial.TutorialSystem;

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

    protected ShipComponentSystem shipComponentSystem;

    protected ComponentMapper<CrewMember> mCrewMember;
    private CrewSystem crewSystem;
    private LifesupportSimulationSystem lifesupportSimulationSystem;
    private TutorialSystem tutorialSystem;


    public DilemmaSystem() {
        super(Aspect.getAspectForAll(Pos.class, DilemmaChoice.class));
    }

    public Entity createLabel(int x, int y, Color color, String text) {
        return new EntityBuilder(world).with(
                new Pos(x, y),
                new Renderable(),
                new Label(text),
                GdxUtil.convert(color)
        ).group(DILEMMA_GROUP).build();
    }

    private Entity createOption(int x, int y, String text, ButtonListener listener) {
        return new EntityBuilder(world).with(
                new Pos(x, y),
                new Label(text),
                new Renderable(),
                new Bounds(0, -8, text.length() * 8, 0),
                new Clickable(),
                new net.mostlyoriginal.api.component.graphics.Color(),
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
        if ( dilemma == null ) return;
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

        startDilemma(new Dilemma("Preparing in space dock, ", "you are ready for your biggest adventure yet!", "[Embark my very own Ark!]",
                new ChainDilemma(new Dilemma("you have been tasked with transporting a gate,", "and activating it at a resource heavy planet!", "[Stop talking and hand me the keys!]",
                new CloseDilemmaListener() {
                    @Override
                    public void run() {
                        super.run();
                        tutorialSystem.activateNextStep();
                    }
                }))));
    }

    public void afterTutorialDilemma() {
        startDilemma(
                new Dilemma("The space dock rushes ark construction.","Complete further construction in transit!","[What, that thing is a deathtrap!]",
                new ChainDilemma(new Dilemma("Place a couple of planned upgrades now.", "Scoop up your crew and resources, and engage!",
                        "[ Normal - Full crew, full resources. ]", new PayoutListener(
                                    InventorySystem.Resource.FUEL,
                                    InventorySystem.Resource.FUEL,
                                    InventorySystem.Resource.FUEL,
                                    InventorySystem.Resource.FOOD,
                                    InventorySystem.Resource.FOOD,
                                    InventorySystem.Resource.FOOD,
                                    InventorySystem.Resource.CREWMEMBER,
                                    InventorySystem.Resource.CREWMEMBER,
                                    InventorySystem.Resource.CREWMEMBER) {
            @Override
            public void run() {
                super.run();
                efs.createEngageButton();
                efs.createScanButton();
            }
        },
                        "[ Hard - Small crew, less resources. ]", new PayoutListener(
                                           InventorySystem.Resource.FUEL,
                                           InventorySystem.Resource.FUEL,
                                           InventorySystem.Resource.FOOD,
                                           InventorySystem.Resource.CREWMEMBER,
                                           InventorySystem.Resource.CREWMEMBER) {
                   @Override
                   public void run() {
                       super.run();
                       efs.createEngageButton();
                       efs.createScanButton();
                   }
               }
                ))));
    }

    /** Spawn am even weighted random dilemma. */
    public void randomDilemma() {
            if ( MathUtils.random(0, 99) < 60 ) {
                randomPositiveDilemma();
            } else {
                randomNegativeDilemma();
            }
    }

    /** player is look for a fight, the odds are against him! */
    public void scanDilemma() {
            if ( MathUtils.random(0, 99) < 40 ) {
                randomPositiveDilemma();
            } else {
                randomNegativeDilemma();
            }
    }

    /** Victory! :D */
    public void victoryDilemma() {
        displayScore();
        startDilemma(new Dilemma("You have successfully reached your destination!", "This gate will bring an wealth and prosperity to your world!", "[YAY! Play again.]", new RestartListener()));
    }

    private void displayScore() {
        Label score = new Label("Scored "+shipComponentSystem.shipValue()+" points");
        score.scale=2;
        new EntityBuilder(world).with(new Renderable(10000), new Pos(G.SCREEN_WIDTH/2 - 4*score.text.length(), G.SCREEN_HEIGHT/2), score).build();
    }

    /** Out of gas. :( */
    public void outOfGasDilemma() {
        startDilemma(new Dilemma("You have run out of gas.", DONT_GIVE_UP, new CloseDilemmaListener(),GIVE_UP, new RestartListener() ));
    }

    public void brainslugTakeoverDilemma() {
        startDilemma(new Dilemma("Brainslugs have taken over!", "None of your crew remains.", GIVE_UP, new RestartListener()));
    }



    /** No pilots remain. :( */
    public void noPilotsDilemma() {
        startDilemma(new Dilemma("Nobody left to pilot the ship!", DONT_GIVE_UP, new CloseDilemmaListener(), GIVE_UP, new RestartListener()));
    }

    public void randomPositiveDilemma()
    {
        Dilemma dilemma = null;
        while ( dilemma  == null ) {

            switch (MathUtils.random(0, 4)) {
                case 0: {
                    dilemma = birthInElevator();
                    break;
                }
                case 1: {
                    dilemma = brainslugOnPlanet();
                    break;
                }
                case 2: {
                    dilemma = abandonedFuelPlant();
                    break;
                }
                case 3: {
                    dilemma = gasGiant();
                    break;
                }
                case 4: {
                    dilemma = foodPlanet();
                    break;
                }
                default:
                    // nothing happens.
                    dilemma = new Dilemma("Another year, another mile.", null, "[I wish something exploded]", new CloseDilemmaListener());
                    break;
            }

        }

        startDilemma(dilemma);

        /*
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
        })); */

    }

    private Dilemma abandonedFuelPlant() {
        CrewMember worker = crewSystem.randomWithAsCrew(CrewMember.Ability.BUILD);
        if ( worker != null ) {
            return createRewardDilemma("You come across an abandoned refueling station.", "There appears to be some fuel remaining.", "[Recover the fuel]", InventorySystem.Resource.FUEL);
        }
        return null;
    }

    private Dilemma gasGiant() {
        CrewMember worker = crewSystem.randomWithAsCrew(CrewMember.Ability.BUILD);
        if ( worker != null ) {
            return createRewardDilemma("You discover a rogue gas giant.", "The gas can be converted to fuel.", "[Scoop it up!]", InventorySystem.Resource.FUEL, InventorySystem.Resource.FUEL, InventorySystem.Resource.FUEL);
        }
        return null;
    }

    private Dilemma foodPlanet() {
        CrewMember worker = crewSystem.randomWithAsCrew(CrewMember.Ability.BUILD);
        if ( worker != null ) {
            return createRewardDilemma("A probe has discovered a planet lush with coconuts.", "Sentient coconuts...", "[Gather]", InventorySystem.Resource.FOOD, InventorySystem.Resource.FOOD, InventorySystem.Resource.FOOD);
        }
        return null;
    }

    private Dilemma birthInElevator() {
        CrewMember birther = crewSystem.randomWithAsCrew(CrewMember.Ability.GIVE_BIRTH);
        if ( birther != null ) {
            return createRewardDilemma("Stuck with " + birther.name + " in the turbolift,", "water starts dripping down their leg! so typical.", "[Deliver baby]", InventorySystem.Resource.CREWMEMBER);
        }
        return null;
    }

    private Dilemma createRewardDilemma(String text1, String text2, String option1, InventorySystem.Resource ... resources ) {
        return new Dilemma(text1,text2,option1, new PayoutListener(resources));
    }

    private Dilemma createPenaltyDilemma(String text1, String text2, String option1, InventorySystem.Resource ... resources ) {
        return new Dilemma(text1,text2,option1, new PenaltyListener(resources));
    }

    public void randomNegativeDilemma()
    {
        Dilemma dilemma = null;
        while ( dilemma  == null ) {

            switch (MathUtils.random(0, 3)) {
                case 0: {
                    dilemma = plasmaAccident();
                    break;
                }
                case 1: {
                    dilemma = brainslugOnPlanet();
                    break;
                }
                case 2: {
                    dilemma = foodSpoilage();
                    break;
                }
                default:
                    // nothing happens.
                    dilemma = new Dilemma("Another year, another mile.", null, "[I wish something exploded]", new CloseDilemmaListener());
                    break;
            }

        }

        startDilemma(dilemma);

        /*
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
        })); */

    }

    private Dilemma foodSpoilage() {
        return createPenaltyDilemma("The away team brought back a pest,", "infested the food stores.", "[My spaceburgers!]", InventorySystem.Resource.FOOD, InventorySystem.Resource.FOOD);
    }

    private Dilemma plasmaAccident() {
        final Entity e = crewSystem.randomWith(CrewMember.Ability.BUILD);
        if ( e != null ) {
            final CrewMember member = mCrewMember.get(e);
            if ( member != null ) {
                return new Dilemma(member.name + " stuck his fingers", "in an active plasma conduit.", "[Scrape him off the walls]", new KillCrewmemberDilemma(e));
            }
        }
        return null;
    }

    private Dilemma brainslugOnPlanet() {
        final Entity e = crewSystem.randomWith(CrewMember.Ability.INFECTABLE);
        if ( e != null ) {
            final CrewMember member = mCrewMember.get(e);
            if ( member != null ) {
                return new Dilemma("During a spacewalk, " + member.name,  "suit was breached.",
                        "[I wonder if that slug on his head is dangerous!]", new InfectCrewmemberDilemma(e),
                        "[Throw him out the airlock]", new KillCrewmemberDilemma(e));
            }
        }
        return null;
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
                productionSimulationSystem.spawnCollectibleRandomlyOnShip(resource);
            }
        }
    }

    /** Spawn specified resources at mouse cursor when picking this option. */
    private class PenaltyListener extends CloseDilemmaListener {
        private final InventorySystem.Resource[] resources;

        public PenaltyListener(InventorySystem.Resource ... resources )
        {
            this.resources = resources;
        }

        @Override
        public void run() {
            super.run();
            for (InventorySystem.Resource resource : resources) {
                inventorySystem.alter(resource,-1);
            }
        }
    }

    private class KillCrewmemberDilemma extends CloseDilemmaListener {

        private final Entity e;

        private KillCrewmemberDilemma( Entity e ) {
            this.e = e;
        }

        @Override
        public void run() {
            super.run();
            lifesupportSimulationSystem.changeState(e, CrewMember.Effect.DEAD);
        }
    }

    private class InfectCrewmemberDilemma extends CloseDilemmaListener {

        private final Entity e;

        private InfectCrewmemberDilemma( Entity e ) {
            this.e = e;
        }

        @Override
        public void run() {
            super.run();
            lifesupportSimulationSystem.changeState(e, CrewMember.Effect.BRAINSLUG);
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
