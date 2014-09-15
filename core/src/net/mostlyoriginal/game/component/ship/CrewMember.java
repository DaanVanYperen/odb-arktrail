package net.mostlyoriginal.game.component.ship;

import com.artemis.Component;
import net.mostlyoriginal.api.component.graphics.Color;
import net.mostlyoriginal.api.utils.reference.EntityReference;
import net.mostlyoriginal.api.utils.GdxUtil;

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
    public transient EntityReference biogelButton;

    public CrewMember(String name, String animId, Effect effect) {
        this.name = name;
        this.animId = animId;
        this.effect = effect;
    }

    public static enum Ability {
        EAT, PILOT, GIVE_BIRTH, BUILD, INFECTABLE, AGE, INFECT,
        /** Offsets infected crewmembers. */
        COUNTER_INFECTED, BIOGELLABLE
    }


    public static enum Effect {

        /** Nothing wrong. */
        HEALTHY("Healthy", null, GdxUtil.asColor("2C4142"), Ability.PILOT, Ability.EAT, Ability.BUILD, Ability.GIVE_BIRTH, Ability.INFECTABLE, Ability.AGE, Ability.COUNTER_INFECTED),

        /** Nothing wrong. */
        HUNGRY("Hungry",  null,GdxUtil.asColor("46140B"), Ability.PILOT, Ability.EAT, Ability.BUILD, Ability.GIVE_BIRTH, Ability.INFECTABLE, Ability.AGE, Ability.COUNTER_INFECTED),

        /** Nothing wrong. */
        STARVING("Starving", null, GdxUtil.asColor("A9301B"), Ability.PILOT, Ability.EAT, Ability.GIVE_BIRTH, Ability.INFECTABLE, Ability.AGE, Ability.COUNTER_INFECTED),

        /** Nothing wrong. */
        BRAINSLUG("Brainslug", "state-slug", GdxUtil.asColor("42FA29"), Ability.PILOT, Ability.BUILD, Ability.INFECT, Ability.BIOGELLABLE),

        /** Elderly */
        ELDERLY("Elderly", null, GdxUtil.asColor("2C4142"), Ability.INFECTABLE, Ability.BIOGELLABLE, Ability.COUNTER_INFECTED),

        /** NOOOOOOOOO TIMMYYYYYYYYY (crewmember dead) */
        DEAD("Dead", "state-dead", GdxUtil.asColor("4C3448"));

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
