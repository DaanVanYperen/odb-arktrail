package net.mostlyoriginal.game.manager;

import com.artemis.Aspect;
import com.artemis.ComponentMapper;
import com.artemis.Entity;
import com.artemis.annotations.Wire;
import com.artemis.managers.TagManager;
import com.artemis.systems.EntityProcessingSystem;
import net.mostlyoriginal.game.component.environment.RouteNode;
import net.mostlyoriginal.game.component.ship.Travels;
import net.mostlyoriginal.game.system.ship.InventorySystem;
import net.mostlyoriginal.game.system.ui.DilemmaSystem;
import net.mostlyoriginal.game.system.ui.RouteSystem;

/**
 * @author Daan van Yperen
 */
@Wire
public class TravelSimulationSystem extends EntityProcessingSystem {

    public static final int WARP_COOLDOWN = 1;
    protected RouteSystem routeSystem;
    protected ComponentMapper<Travels> mTravels;
    protected ComponentMapper<RouteNode> mRouteNode;
    private TagManager tagManager;
    private boolean traveling;
    private DilemmaSystem dilemmaSystem;
    private InventorySystem inventorySystem;

    public TravelSimulationSystem() {
        super(Aspect.getAspectForAll(Travels.class));
    }

    /** immediately warp, and continue warping if required */
    public void warp()
    {
        // cost to travel to next warp point.
        final int fuelcost = 1;

        if ( inventorySystem.get(InventorySystem.Resource.FUEL) < fuelcost )
        {
            dilemmaSystem.outOfGasDilemma();
            return;
        }

        final Entity entity = routeSystem.gotoNext();
        if ( entity != null && mRouteNode.has(entity) ) {
            inventorySystem.alter(InventorySystem.Resource.FOOD, -1);
            inventorySystem.alter(InventorySystem.Resource.FUEL, -fuelcost);

            switch ( mRouteNode.get(entity).action )
            {
                case SKIP:
                    planWarp();
                    break;
                case EVENT:
                    dilemmaSystem.randomDilemma();
                    break;
                case FINISH:
                    dilemmaSystem.finishDilemma();
                    break;
            }
        }
    }

    /** schedule a warp! */
    public void planWarp() {
        Entity shipMetadata = getShipMetadata();
        if ( shipMetadata != null )
        {
            Travels travels = mTravels.get(shipMetadata);
            if ( travels != null )
            {
                travels.nextJumpAfterCooldown = WARP_COOLDOWN;
            }
        }
    }

    private Entity getShipMetadata() {
        return tagManager.getEntity("travels");
    }

    @Override
    protected void process(Entity e) {
        Travels travels = mTravels.get(e);
        if ( travels != null )
        {
            if ( travels.nextJumpAfterCooldown != 0 ) {
                travels.nextJumpAfterCooldown -= world.delta;
                if (travels.nextJumpAfterCooldown <= 0) {
                    travels.nextJumpAfterCooldown=0;
                    warp();
                }
            }
        }
    }

    public boolean isTraveling() {
        Entity shipMetadata = getShipMetadata();
        if ( shipMetadata != null )
        {
            Travels travels = mTravels.get(shipMetadata);
            if ( travels != null )
            {
                return travels.nextJumpAfterCooldown > 0;
            }
        }
        return false;
    }
}
