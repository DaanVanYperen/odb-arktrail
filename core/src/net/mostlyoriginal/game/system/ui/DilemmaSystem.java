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

    /** Repository for all dilemma. */
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
    private boolean dilemmaActive;

    private GroupManager groupManager;
    private InventorySystem inventorySystem;
    private ProductionSimulationSystem productionSimulationSystem;
    private ConstructionSystem constructionSystem;

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
        if (!dilemmaActive)
        {
            constructionSystem.stopConstructionmode();

            int row = Math.max(4,dilemma.choices.length + dilemma.text.length);

            final Entity globalCrewMember = dilemma.crew != null ? getCrewWithAbility(dilemma.crew) : null;

            // abort if we lack required crew.
            if ( dilemma.crew != null && globalCrewMember == null )
                return null;

            dilemmaActive = true;
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

    /** Remove active dilemma from screen. */
    private void stopDilemma() {
        EntityUtil.safeDeleteAll(groupManager.getEntities(DILEMMA_GROUP));
        dilemmaActive = false;
    }


    @Override
    protected void process(Entity e) {
    }

    /** Spawn a dilemma, with a bias towards positive dilemmas. */
    public void randomDilemma() {
        randomDilemma(60);
    }

    private void randomDilemma(int positiveChance) {
        if ( MathUtils.random(0, 99) < positiveChance) {
            startRandomDilemmaFromGroup("positive");
        } else {
            startRandomDilemmaFromGroup("negative");
        }
    }

    /** player is look for a fight, the odds are against him! */
    public void scanDilemma() {
        randomDilemma(40);
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

    /** Just closes dilemma, no action */
    private class CloseDilemmaListener extends ButtonListener {
        @Override
        public void run() {
            stopDilemma();
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
            case "INFECT":
                lifesupportSimulationSystem.changeState(crewMember, CrewMember.Effect.BRAINSLUG);
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
            case "-FUEL":
                // subtract fuel
                inventorySystem.alter(InventorySystem.Resource.FUEL,-1);
                break;
            case "-FOOD":
                // subtract food
                inventorySystem.alter(InventorySystem.Resource.FOOD,-1);
                break;
            case "-CREW":
                // subtract crew
                inventorySystem.alter(InventorySystem.Resource.CREWMEMBER,-1);
                break;
            case "-BIOGEL":
                // subtract biogel
                inventorySystem.alter(InventorySystem.Resource.BIOGEL,-1);
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
