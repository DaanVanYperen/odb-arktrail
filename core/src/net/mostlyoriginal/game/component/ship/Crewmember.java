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

    public static enum Ability {
        EAT, PILOT, BUILD
    }


    public static enum Effect {

        /** Nothing wrong. */
        HEALTHY("Healthy", Color.valueOf("2C4142"), Ability.PILOT, Ability.EAT, Ability.BUILD),

        /** Nothing wrong. */
        HUNGRY("Hungry", Color.valueOf("46140B"), Ability.PILOT, Ability.EAT, Ability.BUILD),

        /** Nothing wrong. */
        STARVING("Starving", Color.valueOf("A9301B"), Ability.PILOT, Ability.EAT),

        /** NOOOOOOOOO TIMMYYYYYYYYY (crewmember dead) */
        DEAD("Dead", Color.valueOf("4C3448"));

        public final String label;
        public final Color color;
        private final Ability[] abilities;

        /** Does the crewmember have specified ability? */
        public boolean can( Ability ability )
        {
            for (Ability ability1 : abilities) {
                if ( ability1 == ability )
                     return true;
            }
            return false;
        }

        Effect(String label, Color color, Ability ... abilities ) {
            this.label = label;
            this.color = color;
            this.abilities = abilities;
        }


    }
}
