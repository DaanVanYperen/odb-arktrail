package net.mostlyoriginal.game.component.environment;


import com.artemis.Component;

/**
 * Node in the route.
 *
 * @author Daan van Yperen
 */
public class RouteNode extends Component {
    public int order; // order in the route. (0=first).
    public Action action = Action.SKIP; // action to take when reaching this node.
    public boolean visited = false;

    public RouteNode(Action action, int order) {
        this.action = action;
        this.order = order;
    }

    public enum Action {
        SKIP,
        EVENT
    }
}
