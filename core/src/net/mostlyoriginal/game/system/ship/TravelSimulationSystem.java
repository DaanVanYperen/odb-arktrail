package net.mostlyoriginal.game.system.ship;

import com.artemis.Aspect;
import com.artemis.ComponentMapper;
import com.artemis.Entity;
import com.artemis.annotations.Wire;
import com.artemis.managers.TagManager;
import com.artemis.systems.EntityProcessingSystem;
import com.badlogic.gdx.math.MathUtils;
import net.mostlyoriginal.game.component.environment.RouteNode;
import net.mostlyoriginal.game.component.ship.CrewMember;
import net.mostlyoriginal.game.component.ship.Travels;
import net.mostlyoriginal.game.system.ui.DilemmaSystem;
import net.mostlyoriginal.game.system.ui.RouteSystem;

/**
 * @author Daan van Yperen
 */
@Wire
public class TravelSimulationSystem extends EntityProcessingSystem {

    public static final int WARP_COOLDOWN = 5;
    protected RouteSystem routeSystem;
    protected ComponentMapper<Travels> mTravels;
    protected ComponentMapper<RouteNode> mRouteNode;
    private TagManager tagManager;
    private boolean traveling;
    private DilemmaSystem dilemmaSystem;
    private InventorySystem inventorySystem;
    private CrewSystem crewSystem;
    private LifesupportSimulationSystem lifesupportSimulationSystem;
    private ProductionSimulationSystem productionSimulationSystem;

    public TravelSimulationSystem() {
        super(Aspect.getAspectForAll(Travels.class));
    }

    /** immediately warp, and continue warping if required */
    public void warp(int speed)
    {
        // cost to travel to next warp point.
        if (handleNoPilotsLeft()) return;

        if ( inventorySystem.get(InventorySystem.Resource.FUEL) < 1 )
        {
            dilemmaSystem.outOfFuelDilemma();
            return;
        }

        final Entity entity = routeSystem.gotoNext();
        if ( entity != null && mRouteNode.has(entity) ) {

            RouteNode.Action action = mRouteNode.get(entity).action;

            // only tick when we are not skipping a step in route.
            if ( action != RouteNode.Action.SKIP || speed == 0 ) {
                // step the lifesupport system forward.
                productionSimulationSystem.process();
                lifesupportSimulationSystem.process();
            }

            switch (action)
            {
                case SKIP:
                    if ( speed > 0 )
                    {
                        warp(speed-1);
                    } else {
                        planWarp();
                    }
                    break;
                case EVENT:
                    dilemmaSystem.randomDilemma();
                    break;
                case FINISH:
                    dilemmaSystem.victoryDilemma();
                    break;
            }
        }
    }

    private boolean handleNoPilotsLeft() {
        if ( crewSystem.countOf(CrewMember.Ability.PILOT) <= 0 )
        {
            dilemmaSystem.noPilotsDilemma();
            return true;
        }
        return false;
    }

    /** schedule a warp! */
    public void planWarp() {

        if (handleNoPilotsLeft()) return;

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
                    warp(MathUtils.random(1, MathUtils.clamp(inventorySystem.get(InventorySystem.Resource.THRUST),1,99)));
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
