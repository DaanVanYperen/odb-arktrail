package net.mostlyoriginal.game.component.ship;

import com.artemis.Component;

/**
 * @author Daan van Yperen
 */
public class ShipComponent extends Component {

    public Type type;

    public enum Type {
        BUNKS("c-bunks"),
        MEDBAY("c-medbay"),
        HYDROPONICS("c-hydroponics"),
        STORAGEPOD("c-storagepod"),
        ENGINE("c-engine"),
        RAMSCOOP("c-ramscoop"),;

        public final String animId;

        Type(String animId) {
            this.animId = animId;
        }
    }



}
