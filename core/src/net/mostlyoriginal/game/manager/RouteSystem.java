package net.mostlyoriginal.game.manager;

import com.artemis.Aspect;
import com.artemis.ComponentMapper;
import com.artemis.Entity;
import com.artemis.annotations.Wire;
import com.artemis.managers.GroupManager;
import com.artemis.managers.TagManager;
import com.artemis.systems.EntityProcessingSystem;
import com.badlogic.gdx.math.MathUtils;
import net.mostlyoriginal.api.component.basic.Pos;
import net.mostlyoriginal.api.component.graphics.Anim;
import net.mostlyoriginal.game.G;
import net.mostlyoriginal.game.component.environment.RouteNode;

/**
 * Plot system.
 *
 * @author Daan van Yperen
 */
@Wire
public class RouteSystem extends EntityProcessingSystem {

    public static final int CHANCE_OF_ROUTE_BEING_NODE = 50;
    public static final int DEFAULT_ROUTE_LENGTH = 16;

    protected GroupManager groupManager;
    protected EntityFactorySystem efs;

    protected ComponentMapper<RouteNode> mRouteNode;
    protected ComponentMapper<Anim> mAnim;
    protected ComponentMapper<Pos> mPos;
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
            RouteNode.Action action = ( (i==0) || (i==length-1) || (MathUtils.random(0, 99) < CHANCE_OF_ROUTE_BEING_NODE) ) ? RouteNode.Action.EVENT : RouteNode.Action.SKIP;

            efs.createRouteNode( startX + i * 8, G.SCREEN_HEIGHT - 16, action, i);
         }

        markVisitedUpTo(0);
    }

    /** Signal everything visited up to given step. */
    public void markVisitedUpTo(int upToStep)
    {

        for ( Entity node : groupManager.getEntities("route")) {
            if( mRouteNode.has(node) )
            {
                RouteNode routeNode = (RouteNode) mRouteNode.get(node);
                routeNode.visited = ( routeNode.order <= upToStep );

                // move indicator to active step.
                if ( routeNode.order == upToStep )
                {
                    placeIndicatorAboveNode(node);
                }
            }
        }
    }

    private void placeIndicatorAboveNode(Entity node) {
        Entity routeIndicator = tagManager.getEntity("routeindicator");
        if ( routeIndicator != null && mPos.has(routeIndicator)) {
            Pos indicatorPos = mPos.get(routeIndicator);
            Pos nodePos = mPos.get(node);
            indicatorPos.x = nodePos.x;
            indicatorPos.y = nodePos.y + 5;
        }
    }

    private void deleteRoute() {

        for ( Entity e : groupManager.getEntities("route")) {
            e.deleteFromWorld();
        }
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
                anim.id = routeNode.visited ? "progress-bubble-1" : "progress-bubble-0";
                break;
        }
    }
}
