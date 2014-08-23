package net.mostlyoriginal.game.component.ship;

import com.artemis.Component;
import com.badlogic.gdx.graphics.Color;
import net.mostlyoriginal.api.utils.EntityReference;

/**
 * @author Daan van Yperen
 */
public class CrewMember extends Component {

    public String name;
    public String animId;
    public Effect effect = Effect.HEALTHY;

    public transient EntityReference icon;
    public transient EntityReference labelName;
    public transient EntityReference labelStatus;

    public CrewMember(String name, String animId, Effect effect) {
        this.name = name;
        this.animId = animId;
        this.effect = effect;
    }

    public enum Effect {

        /** Nothing wrong. */
        HEALTHY("Healthy", Color.valueOf("2C4142")),

        /** NOOOOOOOOO TIMMYYYYYYYYY (crewmember dead) */
        DEAD("Dead", Color.valueOf("4C3448"));

        public final String label;
        public final Color color;

        Effect(String label, Color color ) {
            this.label = label;
            this.color = color;
        }


    }
}
