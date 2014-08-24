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

    public ShipComponent(Type type, int gridX, int gridY, State state) {
        this.type = type;
        this.gridX = gridX;
        this.gridY = gridY;
        this.state = state;
    }

    public enum State {
        UNDER_CONSTRUCTION,
        CONSTRUCTED,
    }

    public enum Type {
        EXPANSION_SLOT(null, false, 500, 0),
        BUNKS("c-bunks", true, 500, 0),
        MEDBAY("c-medbay", true, 500, 0),
        HYDROPONICS("c-hydroponics", true, 500, 0),
        STORAGEPOD("c-storagepod", true, 500, 0),
        ENGINE("c-engine", true, 550, -8),
        RAMSCOOP("c-ramscoop", true, 550, 0);

        public final String animId;
        public final boolean buildable;
        public final String placedAnimId;
        public final int layer;
        public final int xOffset;

        /**
         *
         * @param animId
         * @param buildable
         * @param layer
         * @param xOffset offset when placed on the ship (for engines).
         */
        Type(String animId, boolean buildable, int layer, int xOffset) {
            this.animId = animId;
            this.layer = layer;
            this.xOffset = xOffset;
            this.placedAnimId = animId + "-placed";
            this.buildable = buildable;
        }
    }



}
