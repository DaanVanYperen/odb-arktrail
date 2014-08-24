package net.mostlyoriginal.game.component.ship;

import com.artemis.Component;

/**
 * @author Daan van Yperen
 */
public class ShipComponent extends Component {

    public Type type;
    public State state = State.UNDER_CONSTRUCTION;
    public int gridY;
    public int gridX;

    public ShipComponent(Type type, int gridX, int gridY) {
        this.type = type;
        this.gridX = gridX;
        this.gridY = gridY;
    }

    public enum State {
        UNDER_CONSTRUCTION,
        CONSTRUCTED,
    }

    public enum Type {
        EXPANSION_SLOT(null, false),
        BUNKS("c-bunks", true),
        MEDBAY("c-medbay", true),
        HYDROPONICS("c-hydroponics", true),
        STORAGEPOD("c-storagepod", true),
        ENGINE("c-engine", true),
        RAMSCOOP("c-ramscoop", true);

        public final String animId;
        public final boolean buildable;

        Type(String animId, boolean buildable) {
            this.animId = animId;
            this.buildable = buildable;
        }
    }



}
