package net.mostlyoriginal.game.system.ui;

import com.artemis.Aspect;
import com.artemis.ComponentMapper;
import com.artemis.Entity;
import com.artemis.annotations.Wire;
import com.artemis.systems.EntityProcessingSystem;
import com.artemis.utils.EntityBuilder;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import net.mostlyoriginal.api.component.basic.Pos;
import net.mostlyoriginal.api.component.graphics.Anim;
import net.mostlyoriginal.api.utils.SafeEntityReference;
import net.mostlyoriginal.game.G;
import net.mostlyoriginal.game.component.ship.ShipComponent;
import net.mostlyoriginal.game.component.ui.Button;
import net.mostlyoriginal.game.component.ui.ButtonListener;
import net.mostlyoriginal.game.component.ui.Clickable;
import net.mostlyoriginal.game.manager.EntityFactorySystem;
import net.mostlyoriginal.game.system.ship.HullSystem;
import net.mostlyoriginal.game.system.ship.ShipComponentSystem;
import net.mostlyoriginal.game.system.ship.TravelSimulationSystem;

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
    private HullSystem hullSystem;

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
        final Anim anim = mAnim.get(e);
        final ShipComponent shipComponent = mShipComponent.get(e);

        if ( shipComponent.type == ShipComponent.Type.HULL)
        {
            // show building indicator while placing.
            boolean validLocale = getLocaleValidity(anim, shipComponent);

            anim.id2 = validLocale ? "c-indicator" : null;
            anim.color.a = 1;

            // start construction when clicked.
            Clickable clickable = mClickable.get(e);
            if ( clickable.state == Clickable.ClickState.CLICKED && validLocale )
            {
                startConstruction(e, selected);
                if ( !Gdx.input.isKeyPressed(Input.Keys.CONTROL_LEFT ) && !Gdx.input.isKeyPressed(Input.Keys.CONTROL_RIGHT ) ) stopConstructionmode();
            }

        } else {
            anim.color.a = 1f;
        }

    }

    /** Return if locale is valid. */
    private boolean getLocaleValidity(Anim anim, ShipComponent shipComponent) {
        boolean validLocale = selected != null;

        // don't allow expanding at the borders so the hull doesn't break.
        if ( shipComponent.gridX <= 0 ||shipComponent.gridY <= 0 || shipComponent.gridX >= ShipComponentSystem.MAX_X-1 || shipComponent.gridY >= ShipComponentSystem.MAX_Y -1 )
            return false;

        // only allow engine on left facing hull.
        if ( selected == ShipComponent.Type.ENGINE && !"hull-3".equals(anim.id) ) validLocale = false;
        // only allow ramscoop on right facing hull.
        if ( selected == ShipComponent.Type.RAMSCOOP && !"hull-4".equals(anim.id) ) validLocale = false;
        return validLocale;
    }

    private void stopConstructionmode() {
        selected=null;
    }

    /** Activate shipcomponent! */
    private void startConstruction(Entity e, ShipComponent.Type selected) {
        if ( selected != null && e != null && mAnim.has(e) && mShipComponent.has(e) ) {

            //e.addComponent(new ColorAnimation( Color.CLEAR, new Color(1f,1f,1f,0.5f), Interpolation.linear, 2f, 0.4f )).changedInWorld();

            final ShipComponent shipComponent = mShipComponent.get(e);
            shipComponent.type = selected;
            shipComponent.state = ShipComponent.State.UNDER_CONSTRUCTION;
            shipComponent.constructionManyearsRemaining = selected.buildManYears;
            mAnim.get(e).id = selected.animId;

            hullSystem.dirty();
        }
    }

    private void createConstructionButtons() {
        int index = 0;
        for (ShipComponent.Type structure : ShipComponent.Type.values()) {
            if (structure.buildable) {
                int x = G.SCREEN_WIDTH + MARGIN_RIGHT - (index + 1) * 18;
                int y = 7;
                Entity button = efs.createButton(x, y, 15, 15, "btn-construct", new ToolSelectButton(structure), null);
                Button button1 = mButton.get(button);
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
