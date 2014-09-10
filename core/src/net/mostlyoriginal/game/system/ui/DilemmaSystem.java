package net.mostlyoriginal.game.system.ui;

import com.artemis.Aspect;
import com.artemis.ComponentMapper;
import com.artemis.Entity;
import com.artemis.annotations.Wire;
import com.artemis.managers.GroupManager;
import com.artemis.systems.EntityProcessingSystem;
import com.artemis.utils.EntityBuilder;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.utils.Json;
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

import java.util.*;

/**
 * Responsible for serving and processing dilemmas.
 *
 * @author Daan van Yperen
 */
@Wire
public class DilemmaSystem extends EntityProcessingSystem {

    private DilemmaLibrary dilemmaLibrary;

    public static class DilemmaLibrary {
        public Dilemma[] dilemmas;
        private DilemmaLibrary() {
        }

        Map<String, List<Dilemma>> grouped = new HashMap<>();

        /** Return dilemma, or <code>null</code> if empty. */
        public Dilemma getById( String id )
        {
            for (Dilemma dilemma : dilemmas) {
                if ( dilemma.id != null && dilemma.id.equals(id)) return dilemma;
            }
            return null;
        }

        /** Map dilemma to groups */
        public void assignToGroups() {
            for (Dilemma dilemma : dilemmas) {
                if (dilemma.groups != null) {
                    for (String group : dilemma.groups) {
                        addToGroup(dilemma, group);
                    }
                }
            }
        }

        private void addToGroup(Dilemma dilemma, String group) {
            getGroup(group).add(dilemma);
        }

        public List<Dilemma> getGroup(String group) {
            List<Dilemma> list = grouped.get(group);
            if ( list == null )
            {
                list = new ArrayList<>();
                grouped.put(group, list);
            }
            return list;
        }
    }

    public static final String DILEMMA_GROUP = "dilemma";
    public static final int ROW_HEIGHT = 9;
    EntityFactorySystem efs;

    public static final Color COLOR_DILEMMA = Color.valueOf("6AD7ED");
    public static final String COLOR_RAW_BRIGHT = "E7E045";
    public static final String COLOR_RAW_DIMMED = "FDF1AA";
    private boolean dilemma2Active;
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

    public boolean isDilemma2Active() {
        return dilemma2Active;
    }

    @Override
    protected void initialize() {
        super.initialize();
        loadDilemmas();
        startDilemma("START_TUTORIAL");
    }

    private void loadDilemmas() {

        final Json json = new Json();
        dilemmaLibrary = json.fromJson(DilemmaLibrary.class, Gdx.files.internal("dilemma.json"));
        dilemmaLibrary.assignToGroups();
    }

    private void startDilemma(String dilemmaId )
    {
        Dilemma dilemma = dilemmaLibrary.getById(dilemmaId);
        if ( dilemma == null ) {
            throw new RuntimeException("Missing dilemma " + dilemmaId);
        }
        startDilemma(dilemma);
    }

    private Dilemma startDilemma(Dilemma dilemma) {
        if (!dilemma2Active)
        {

            int row = Math.max(4,dilemma.choices.length + dilemma.text.length);

            final Entity globalCrewMember = dilemma.crew != null ? getCrewWithAbility(dilemma.crew) : null;

            // abort if we lack required crew.
            if ( dilemma.crew != null && globalCrewMember == null )
                return null;

            dilemma2Active = true;
            for (String text : dilemma.text) {
                createLabel(10, 10 + ROW_HEIGHT * row, COLOR_DILEMMA, replaceKeywords(text, globalCrewMember));
                row--;
            }

            for (Dilemma.Choice choice : dilemma.choices) {

                final Entity crewMember = choice.crew != null ? getCrewWithAbility(choice.crew) : globalCrewMember;

                // random chance of succes, if no failure options defined, always failure.
                final String[] choices = (choice.failure == null) || (MathUtils.random(0, 100) < 100-choice.risk) ? choice.success : choice.failure;

                if ( (choice.crew == null) || crewMember != null  ) {
                    createOption(10, 10 + ROW_HEIGHT * row, replaceKeywords("[" + choice.label[MathUtils.random(0, choice.label.length - 1)] + "]", crewMember), new DilemmaListener(choices, crewMember));
                }
                row--;
            }
        }

        return dilemma;
    }

    private String replaceKeywords(String text, Entity crew) {

        if ( crew != null ) {
            CrewMember member = mCrewMember.get(crew);
            if ( member != null ) {
                text = text.replaceAll("\\{NAME\\}", member.name)
                        .replaceAll("\\{HIS\\}", "his")
                        .replaceAll("\\{HIM\\}", "him");
            }
        }

        return text;
    }

    private Entity getCrewWithAbility(String ability) {
        return crewSystem.randomWith(CrewMember.Ability.valueOf(ability));
    }


    private void startDilemma(Dilemma2 dilemma) {
        if ( dilemma == null ) return;
        if ( !dilemma2Active) {
            dilemma2Active = true;
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
        dilemma2Active = false;
    }


    @Override
    protected void process(Entity e) {
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

    private void displayScore() {
        Label score = new Label("Scored "+shipComponentSystem.shipValue()+" points");
        score.scale=2;
        new EntityBuilder(world).with(new Renderable(10000), new Pos(G.SCREEN_WIDTH/2 - 4*score.text.length(), G.SCREEN_HEIGHT/2), score).build();
    }


    /** Victory! :D */
    public void victoryDilemma() {
        displayScore();
        startDilemma("VICTORY");
    }

    /** Out of gas. :( */
    public void outOfFuelDilemma() {
        startDilemma("OUT_OF_FUEL");
    }

    /** Drats. Those brainslugs. */
    public void brainslugTakeoverDilemma() {
        startDilemma("BRAINSLUGS_DOMINATE");
    }


    /** No pilots remain. :( */
    public void noPilotsDilemma() {
        startDilemma("NO_PILOTS_REMAIN");
    }

    /** Pick difficulty. */
    public void afterTutorialDilemma() {
        startDilemma("AFTER_TUTORIAL");
    }

    public void randomPositiveDilemma()
    {
        /*

            switch (MathUtils.random(0, 4)) {
                case 0: {
                    dilemma = birthInElevator();
                    break;
                }
                case 1: {
                    dilemma = brainslugOnPlanet();
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
                    dilemma = new Dilemma2("Another year, another mile.", null, "[I wish something exploded]", new CloseDilemmaListener());
                    break;
            }

        }*/

        startRandomDilemmaFromGroup("positive");
    }

    private void startRandomDilemmaFromGroup(String group) {
        List<Dilemma> dilemmas = dilemmaLibrary.getGroup(group);

        Dilemma dilemma = null;
        while ( dilemma == null ) {
            dilemma = dilemmas.get(MathUtils.random(0, dilemmas.size()-1));

            if ( dilemma != null ) {
                dilemma = startDilemma(dilemma);
                // if startdilemma fails, it returns NULL and we will search again.
            }
        }
    }

    private Dilemma2 gasGiant() {
        CrewMember worker = crewSystem.randomWithAsCrew(CrewMember.Ability.BUILD);
        if ( worker != null ) {
            return createRewardDilemma("You discover a rogue gas giant.", "The gas can be converted to fuel.", "[Scoop it up!]", InventorySystem.Resource.FUEL, InventorySystem.Resource.FUEL, InventorySystem.Resource.FUEL);
        }
        return null;
    }

    private Dilemma2 foodPlanet() {
        CrewMember worker = crewSystem.randomWithAsCrew(CrewMember.Ability.BUILD);
        if ( worker != null ) {
            return createRewardDilemma("A probe has discovered a planet lush with coconuts.", "Sentient coconuts...", "[Gather]", InventorySystem.Resource.FOOD, InventorySystem.Resource.FOOD, InventorySystem.Resource.FOOD);
        }
        return null;
    }

    private Dilemma2 createRewardDilemma(String text1, String text2, String option1, InventorySystem.Resource ... resources ) {
        return new Dilemma2(text1,text2,option1, new PayoutListener(resources));
    }

    private Dilemma2 createPenaltyDilemma(String text1, String text2, String option1, InventorySystem.Resource ... resources ) {
        return new Dilemma2(text1,text2,option1, new PenaltyListener(resources));
    }

    /** Trigger a mostly negative dilemma. */
    public void randomNegativeDilemma()
    {
        startRandomDilemmaFromGroup("negative");
        /*
        Dilemma2 dilemma = null;
        while ( dilemma  == null ) {

            switch (MathUtils.random(0, 3)) {
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
                    dilemma = new Dilemma2("Another year, another mile.", null, "[I wish something exploded]", new CloseDilemmaListener());
                    break;
            }

        }

        startDilemma(dilemma);*/
    }

    private Dilemma2 foodSpoilage() {
        return createPenaltyDilemma("The away team brought back a pest,", "infested the food stores.", "[My spaceburgers!]", InventorySystem.Resource.FOOD, InventorySystem.Resource.FOOD);
    }

    private Dilemma2 plasmaAccident() {
        final Entity e = crewSystem.randomWith(CrewMember.Ability.BUILD);
        if ( e != null ) {
            final CrewMember member = mCrewMember.get(e);
            if ( member != null ) {
                return new Dilemma2(member.name + " stuck his fingers", "in an active plasma conduit.", "[Scrape him off the walls]", new KillCrewmemberDilemma(e));
            }
        }
        return null;
    }

    private Dilemma2 brainslugOnPlanet() {
        final Entity e = crewSystem.randomWith(CrewMember.Ability.INFECTABLE);
        if ( e != null ) {
            final CrewMember member = mCrewMember.get(e);
            if ( member != null ) {
                return new Dilemma2("During a spacewalk, " + member.name,  "suit was breached.",
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
            restartGame();
        }
    }

    private static class Dilemma2 {
        private String text1;
        private String text2;
        private final String option1;
        private final ButtonListener listener1;
        private String option2;
        private ButtonListener listener2;

        public Dilemma2(String text1, String text2, String option1, ButtonListener listener1, String option2, ButtonListener listener2) {
            this.text1 = text1;
            this.text2 = text2;
            this.option1 = option1;
            this.listener1 = listener1;
            this.option2 = option2;
            this.listener2 = listener2;
        }

        public Dilemma2(String text2, String option1, ButtonListener listener1) {
            this.text2 = text2;
            this.option1 = option1;
            this.listener1 = listener1;
        }

        public Dilemma2(String text2, String option1, ButtonListener listener1, String option2, ButtonListener listener2) {
            this.text2 = text2;
            this.option1 = option1;
            this.listener1 = listener1;
            this.option2 = option2;
            this.listener2 = listener2;
        }

        public Dilemma2(String text1, String text2, String option1, ButtonListener listener1) {
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
        private final Dilemma2 dilemma;

        public ChainDilemma(Dilemma2 dilemma) {
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

    public final String[] DEFAULT_ACTION = new String[]{"CLOSE"};
    private class DilemmaListener extends ButtonListener {

        private String[] actions;
        private final Entity crewMember;

        public DilemmaListener(String[] actions, Entity crewMember) {
            super();
            this.actions = actions;
            this.crewMember = crewMember;
            this.actions = actions == null || actions.length == 0 ? DEFAULT_ACTION : actions;
        }

        @Override
        public void run() {
            super.run();

            // run all success.
            for (String action : actions) {
                triggerAction(action, crewMember);
            }
        }
    }

    /** Trigger hardcodede action indicated by string. If not exists, assume we are starting a dilemma. */
    private void triggerAction(String action, Entity crewMember) {
        stopDilemma();
        switch ( action )
        {
            case "CLOSE" :
                break;
            case "RESTART":
                restartGame();
                break;
            case "NEXT_TUTORIAL_STEP":
                if ( dilemmaLibrary.getById("TEST") != null ) {
                    startDilemma("TEST");
                } else
                    tutorialSystem.activateNextStep();
                break;
            case "KILL":
                lifesupportSimulationSystem.changeState(crewMember, CrewMember.Effect.DEAD);
                break;
            case "FUEL":
                // spawn fuel.
                productionSimulationSystem.spawnCollectibleRandomlyOnShip(InventorySystem.Resource.FUEL);
                break;
            case "FOOD":
                // spawn food
                productionSimulationSystem.spawnCollectibleRandomlyOnShip(InventorySystem.Resource.FOOD);
                break;
            case "CREW":
                // spawn crew.
                productionSimulationSystem.spawnCollectibleRandomlyOnShip(InventorySystem.Resource.CREWMEMBER);
                break;
            case "BIOGEL":
                // spawn biogel
                productionSimulationSystem.spawnCollectibleRandomlyOnShip(InventorySystem.Resource.BIOGEL);
                break;
            case "ENABLE_ENGAGE":
                efs.createEngageButton();
                break;
            case "ENABLE_SCAN":
                efs.createScanButton();
                break;
            default:
                startDilemma(action);
                break;
        }
    }

    private static void restartGame() {
        MyGame.getInstance().restart();
    }
}
