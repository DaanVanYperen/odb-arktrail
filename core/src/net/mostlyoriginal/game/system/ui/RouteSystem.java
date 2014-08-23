package net.mostlyoriginal.game.system.ui;

import com.artemis.Aspect;
import com.artemis.ComponentMapper;
import com.artemis.Entity;
import com.artemis.annotations.Wire;
import com.artemis.managers.GroupManager;
import com.artemis.managers.TagManager;
import com.artemis.systems.EntityProcessingSystem;
import com.artemis.utils.ImmutableBag;
import com.badlogic.gdx.math.MathUtils;
import net.mostlyoriginal.api.component.basic.Pos;
import net.mostlyoriginal.api.component.graphics.Anim;
import net.mostlyoriginal.api.utils.EntityUtil;
import net.mostlyoriginal.game.G;
import net.mostlyoriginal.game.component.environment.RouteIndicator;
import net.mostlyoriginal.game.component.environment.RouteNode;
import net.mostlyoriginal.game.manager.EntityFactorySystem;

/**
 * Plot system.
 *
 * @author Daan van Yperen
 */
@Wire
public class RouteSystem extends EntityProcessingSystem {

    public static final int CHANCE_OF_ROUTE_BEING_NODE = 38;
    public static final int DEFAULT_ROUTE_LENGTH = 16;

    protected GroupManager groupManager;
    protected EntityFactorySystem efs;

    protected ComponentMapper<RouteNode> mRouteNode;
    protected ComponentMapper<Anim> mAnim;
    protected ComponentMapper<Pos> mPos;
    protected ComponentMapper<RouteIndicator> mRouteIndicator;
    private TagManager tagManager;

    public RouteSystem() {
        super(Aspect.getAspectForAll(RouteNode.class));
    }

    @Override
    protected void initialize() {
        super.initialize();

        efs.createRouteIndicator();
        createRoute(DEFAULT_ROUTE_LENGTH);
    }


    private void createRoute( int length ) {

        deleteRoute();

        // center our route.
        int startX = (G.SCREEN_WIDTH / 2) - (length * 4);

        for ( int i=0; i<length; i++ )
        {
            RouteNode.Action action =
                    ( i==length-1 ) ? RouteNode.Action.FINISH :
                    ( (i==0) || (MathUtils.random(0, 99) < CHANCE_OF_ROUTE_BEING_NODE) ) ? RouteNode.Action.EVENT : RouteNode.Action.SKIP;

            efs.createRouteNode( startX + i * 8, G.SCREEN_HEIGHT - 16, action, i);
         }

        markVisitedUpTo(0);
    }

    /** Signal everything visited up to given step. */
    public Entity markVisitedUpTo(int upToStep)
    {
        Entity atNode = null;

        ImmutableBag<Entity> entities = groupManager.getEntities("route");
        for (int i=0,s=entities.size();i<s; i++)
        {
            Entity node = entities.get(i);
            if( mRouteNode.has(node) )
            {
                RouteNode routeNode = (RouteNode) mRouteNode.get(node);
                routeNode.visited = ( routeNode.order <= upToStep );

                // move indicator to active step.
                if ( routeNode.order == upToStep )
                {
                    atNode = node;
                    placeIndicatorAboveNode(node);
                }
            }
        }

        return atNode;
    }

    private void placeIndicatorAboveNode(Entity node) {
        final Entity routeIndicator = getIndicator();
        if ( routeIndicator != null && mPos.has(routeIndicator)) {
            Pos indicatorPos = mPos.get(routeIndicator);
            Pos nodePos = mPos.get(node);
            indicatorPos.x = nodePos.x;
            indicatorPos.y = nodePos.y + 5;

            // keep track of our current location.
            RouteIndicator indicator = mRouteIndicator.get(routeIndicator);
            indicator.at = mRouteNode.get(node).order;
        }
    }

    private Entity getIndicator() {
        return tagManager.getEntity("routeindicator");
    }

    private void deleteRoute() {
        EntityUtil.safeDeleteAll(groupManager.getEntities("route"));
    }

    @Override
    protected void process(Entity e) {
        RouteNode routeNode = mRouteNode.get(e);
        Anim anim = mAnim.get(e);

        // update nodes so they have the right appearance.
        switch ( routeNode.action ) {
            case SKIP:
                anim.id = routeNode.visited ? "progress-bar-1" : "progress-bar-0";
                break;
            case EVENT:
            case FINISH:
                anim.id = routeNode.visited ? "progress-bubble-1" : "progress-bubble-0";
                break;
        }
    }

    /** go to next step in route. */
    public Entity gotoNext() {
        final Entity routeIndicator = getIndicator();
        return markVisitedUpTo(mRouteIndicator.get(routeIndicator).at + 1);
    }
}
