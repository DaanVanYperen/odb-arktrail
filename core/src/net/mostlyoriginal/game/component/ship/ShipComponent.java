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
    public float constructionManyearsRemaining;

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
        HULL(null, null, false, 500, 0, true, 1),
        BUNKS("c-bunks", "Bunks: may produce birth each jump 'I'll be in my bunk!'.", true, 500, 0, false, 1),
        MEDBAY("c-medbay", "Auto-medic: May produce medigel each jump.", true, 500, 0, false, 3),
        HYDROPONICS("c-hydroponics", "Hydroponics: may produce food each jump.", true, 500, 0, false, 2),
        STORAGEPOD("c-storagepod", "Storage Pod: +overal ship storage capacity.", true, 500, 0, false, 1),
        ENGINE("c-engine", "Engine: Travel further in same amount of time.", true, 550, -8, true, 1),
        RAMSCOOP("c-ramscoop", "Ramscoop: May produce fuel each jump.", true, 550, 0, true, 1),
        CHAIN("c-chain", null, false, 500, 0, true, 1);

        public final String animId;
        public final boolean buildable;
        public final String placedAnimId;
        public final String label;
        public final int layer;
        public final int xOffset;
        public final boolean countsAsHull;
        public final int buildManYears;
        public final String buildingAnimId;

        /**
         * @param animId
         * @param buildable
         * @param layer
         * @param xOffset offset when placed on the ship (for engines).
         * @param countsAsHull
         * @param buildManYears
         */
        Type(String animId, String label, boolean buildable, int layer, int xOffset, boolean countsAsHull, int buildManYears) {
            this.animId = animId;
            this.label = label;
            this.layer = layer;
            this.xOffset = xOffset;
            this.countsAsHull = countsAsHull;
            this.buildManYears = buildManYears;
            this.placedAnimId = animId + "-placed";
            this.buildingAnimId = animId + "-building";
            this.buildable = buildable;
        }
    }



}
