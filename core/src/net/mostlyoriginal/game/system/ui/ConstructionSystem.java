package net.mostlyoriginal.game.system.ui;

import com.artemis.Aspect;
import com.artemis.ComponentMapper;
import com.artemis.Entity;
import com.artemis.annotations.Wire;
import com.artemis.systems.EntityProcessingSystem;
import com.artemis.utils.EntityBuilder;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Color;
import net.mostlyoriginal.api.component.basic.Pos;
import net.mostlyoriginal.api.component.graphics.Anim;
import net.mostlyoriginal.api.utils.SafeEntityReference;
import net.mostlyoriginal.game.G;
import net.mostlyoriginal.game.component.ship.ShipComponent;
import net.mostlyoriginal.game.component.ui.Button;
import net.mostlyoriginal.game.component.ui.ButtonListener;
import net.mostlyoriginal.game.component.ui.Clickable;
import net.mostlyoriginal.game.manager.AssetSystem;
import net.mostlyoriginal.game.manager.EntityFactorySystem;
import net.mostlyoriginal.game.system.ship.HullSystem;
import net.mostlyoriginal.game.system.ship.InventorySystem;
import net.mostlyoriginal.game.system.ship.ShipComponentSystem;
import net.mostlyoriginal.game.system.ship.TravelSimulationSystem;
import net.mostlyoriginal.game.system.tutorial.TutorialSystem;

import java.util.HashMap;

/**
 * @author Daan van Yperen
 */
@Wire
public class ConstructionSystem extends EntityProcessingSystem {

    public static final int MARGIN_RIGHT = -140;
    EntityFactorySystem efs;
    private TravelSimulationSystem travelSimulationSystem;
    private DilemmaSystem dilemmaSystem;
    private ShipComponent.Type selected;

    protected ComponentMapper<Anim> mAnim;
    protected ComponentMapper<ShipComponent> mShipComponent;
    protected ComponentMapper<Clickable> mClickable;
    protected ComponentMapper<Button> mButton;

    private InventorySystem inventorySystem;
    private HullSystem hullSystem;
    private AssetSystem assetSystem;
    private TutorialSystem tutorialSystem;
    public final HashMap<ShipComponent.Type, Entity> constructionButton = new HashMap<>();

    public ConstructionSystem() {
        super(Aspect.getAspectForAll(ShipComponent.class, Clickable.class, Anim.class));
    }

    @Override
    protected void initialize() {
        super.initialize();

        // list all buildable structures.
        createConstructionButtons();
    }

    @Override
    protected void process(Entity e) {
        // Building is restricted to hull parts.
        final ShipComponent shipComponent = mShipComponent.get(e);
        if ( shipComponent.type == ShipComponent.Type.HULL)
        {
            // show building indicator while placing.
            final boolean validDestination = canAttachTo(selected, e);

            final Anim anim = mAnim.get(e);
            anim.id2 = validDestination ? "c-indicator" : null;

            // start construction when clicked.
            final Clickable clickable = mClickable.get(e);
            if ( clickable.state == Clickable.ClickState.CLICKED && validDestination )
            {
                startConstruction(e, selected);

                // continue placement as long as control is pressed.
                if ( !Gdx.input.isKeyPressed(Input.Keys.CONTROL_LEFT ) && !Gdx.input.isKeyPressed(Input.Keys.CONTROL_RIGHT ) ) stopConstructionmode();
            }
        }
    }

    /** Finish construction of passed ship component. */
    public void complete(Entity entity) {
        if (entity != null) {
            final ShipComponent c = mShipComponent.get(entity);
            if (c.state == ShipComponent.State.UNDER_CONSTRUCTION) {
                c.state = ShipComponent.State.CONSTRUCTED;
                switch (c.type) {
                    case HULL:
                        break;
                    case BUNKS:
                        break;
                    case MEDBAY:
                        inventorySystem.alter(InventorySystem.Resource.BIOGEL_STORAGE, 1);
                        break;
                    case HYDROPONICS:
                        break;
                    case STORAGEPOD:
                        inventorySystem.alter(InventorySystem.Resource.STORAGE, 1);
                        break;
                    case ENGINE:
                        efs.createEngineFlame(c.gridX - 3, c.gridY);
                        inventorySystem.alter(InventorySystem.Resource.THRUST, 1);
                        break;
                    case RAMSCOOP:
                        break;
                }
            }
        }
    }


    /** Return if locale is valid. */
    private boolean canAttachTo(ShipComponent.Type type, Entity destination) {

        final Anim anim = mAnim.get(destination);
        final ShipComponent shipComponent = mShipComponent.get(destination);
        if ( anim == null || shipComponent == null ) return false;

        // don't allow expanding at the borders so the hull doesn't break.
        if ( shipComponent.gridX <= 0 ||shipComponent.gridY <= 0 || shipComponent.gridX >= ShipComponentSystem.MAX_X-1 || shipComponent.gridY >= ShipComponentSystem.MAX_Y -1 )
            return false;

        // only allow engine on left facing hull.
        if ( type == ShipComponent.Type.ENGINE && (!"hull-3".equals(anim.id) && !"hull-3-building".equals(anim.id)) ) return false;
        // only allow ramscoop on right facing hull.
        if ( type == ShipComponent.Type.RAMSCOOP && (!"hull-4".equals(anim.id) && !"hull-4-building".equals(anim.id)) ) return false;

        return type != null;
    }

    private void stopConstructionmode() {
        selected=null;
    }

    /** Activate shipcomponent! */
    private void startConstruction(Entity e, ShipComponent.Type selected) {

        if ( selected != null && e != null && mAnim.has(e) && mShipComponent.has(e) ) {

            assetSystem.playSfx("snd-click");

            final ShipComponent shipComponent = mShipComponent.get(e);
            shipComponent.type = selected;
            shipComponent.state = ShipComponent.State.UNDER_CONSTRUCTION;
            shipComponent.constructionManyearsRemaining = selected.buildManYears;
            mAnim.get(e).id = selected.animId;

            hullSystem.dirty();

            if ( selected == ShipComponent.Type.ENGINE ) tutorialSystem.complete(TutorialSystem.Step.PLACE_ENGINE);
            if ( selected == ShipComponent.Type.STORAGEPOD ) tutorialSystem.complete(TutorialSystem.Step.PLACE_STORAGEPOD);
        }
    }

    private void createConstructionButtons() {
        int index = 0;
        for (ShipComponent.Type structure : ShipComponent.Type.values()) {
            if (structure.buildable) {
                int x = G.SCREEN_WIDTH + MARGIN_RIGHT - (index + 1) * 18;
                int y = 7;
                final Entity button = efs.createButton(x, y, 15, 15, "btn-construct", new ToolSelectButton(structure), null);
                constructionButton.put(structure, button);
                Button button1 = mButton.get(button);
                button1.color = Color.WHITE;
                button1.hint = structure.label;
                button1.hideIfDisabled  =true;
                button1.transientIcon = new SafeEntityReference(new EntityBuilder(world).with(new Pos(x + 4, y + 5), new Anim(structure.animId, 4000)).build());
                // add icon over button. @todo merge with button logic.

                index++;
            }
        }
    }

    private void startPlacing(ShipComponent.Type selected) {
        this.selected = selected;

        if ( selected == ShipComponent.Type.ENGINE ) tutorialSystem.complete(TutorialSystem.Step.SELECT_ENGINE);
        if ( selected == ShipComponent.Type.STORAGEPOD ) tutorialSystem.complete(TutorialSystem.Step.SELECT_STORAGEPOD);
    }

    private class ToolSelectButton extends ButtonListener {

        private final ShipComponent.Type structure;

        private ToolSelectButton(ShipComponent.Type structure) {
            this.structure = structure;
        }

        @Override
        public void run() {
            startPlacing(this.structure);
        }

        @Override
        public boolean enabled() {
            // we don't want to allow building while busy!.
            return !travelSimulationSystem.isTraveling() && !dilemmaSystem.isDilemmaActive();
        }
    }

}
