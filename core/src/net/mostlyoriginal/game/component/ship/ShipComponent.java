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
        HULL(null, null, false, 500, 0, true),
        BUNKS("c-bunks", "Bunks: may produce birth each jump 'I'll be in my bunk!'.", true, 500, 0, false),
        MEDBAY("c-medbay", "Auto-medic: May produce medigel each jump.", true, 500, 0, false),
        HYDROPONICS("c-hydroponics", "Hydroponics: may produce food each jump.", true, 500, 0, false),
        STORAGEPOD("c-storagepod", "Storage Pod: increases ship capacity for fuel, food and medigel.", true, 500, 0, false),
        ENGINE("c-engine", "Engine: Faster travel, +fuel usage, -births, -food usage, +sanity.", true, 550, -8, true),
        RAMSCOOP("c-ramscoop", "Ramscoop: May produce food each jump.", true, 550, 0, true);

        public final String animId;
        public final boolean buildable;
        public final String placedAnimId;
        public final String label;
        public final int layer;
        public final int xOffset;
        public final boolean countsAsHull;

        /**
         * @param animId
         * @param buildable
         * @param layer
         * @param xOffset offset when placed on the ship (for engines).
         * @param countsAsHull
         */
        Type(String animId, String label, boolean buildable, int layer, int xOffset, boolean countsAsHull) {
            this.animId = animId;
            this.label = label;
            this.layer = layer;
            this.xOffset = xOffset;
            this.countsAsHull = countsAsHull;
            this.placedAnimId = animId + "-placed";
            this.buildable = buildable;
        }
    }



}
