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
    public int age = 0;

    public transient EntityReference icon;
    public transient EntityReference labelName;
    public transient EntityReference labelStatus;

    public CrewMember(String name, String animId, Effect effect) {
        this.name = name;
        this.animId = animId;
        this.effect = effect;
    }

    public static enum Ability {
        EAT, PILOT, GIVE_BIRTH, BUILD, INFECTABLE, AGE, INFECT
    }


    public static enum Effect {

        /** Nothing wrong. */
        HEALTHY("Healthy", null, Color.valueOf("2C4142"), Ability.PILOT, Ability.EAT, Ability.BUILD, Ability.GIVE_BIRTH, Ability.INFECTABLE, Ability.AGE),

        /** Nothing wrong. */
        HUNGRY("Hungry",  null,Color.valueOf("46140B"), Ability.PILOT, Ability.EAT, Ability.BUILD, Ability.GIVE_BIRTH, Ability.INFECTABLE, Ability.AGE),

        /** Nothing wrong. */
        STARVING("Starving", null, Color.valueOf("A9301B"), Ability.PILOT, Ability.EAT, Ability.GIVE_BIRTH, Ability.INFECTABLE, Ability.AGE),

        /** Nothing wrong. */
        BRAINSLUG("Brainslug", "state-slug", Color.valueOf("42FA29"), Ability.PILOT, Ability.BUILD, Ability.INFECT, Ability.AGE),

        /** Elderly */
        ELDERLY("Elderly", null, Color.valueOf("2C4142"), Ability.INFECTABLE),

        /** NOOOOOOOOO TIMMYYYYYYYYY (crewmember dead) */
        DEAD("Dead", "state-dead", Color.valueOf("4C3448"));

        public final String label;
        public final String animStatusId;
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

        Effect(String label, String animStatusId, Color color, Ability ... abilities ) {
            this.label = label;
            this.animStatusId = animStatusId;
            this.color = color;
            this.abilities = abilities;
        }


    }
}
