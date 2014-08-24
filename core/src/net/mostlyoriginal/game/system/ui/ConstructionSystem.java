package net.mostlyoriginal.game.system.ui;

import com.artemis.annotations.Wire;
import com.artemis.systems.VoidEntitySystem;
import com.artemis.utils.EntityBuilder;
import net.mostlyoriginal.api.component.basic.Pos;
import net.mostlyoriginal.api.component.graphics.Anim;
import net.mostlyoriginal.game.G;
import net.mostlyoriginal.game.component.ship.ShipComponent;
import net.mostlyoriginal.game.component.ui.ButtonListener;
import net.mostlyoriginal.game.manager.EntityFactorySystem;
import net.mostlyoriginal.game.system.ship.TravelSimulationSystem;

/**
 * @author Daan van Yperen
 */
@Wire
public class ConstructionSystem extends VoidEntitySystem {

    public static final int MARGIN_RIGHT = -140;
    EntityFactorySystem efs;
    private TravelSimulationSystem travelSimulationSystem;
    private DilemmaSystem dilemmaSystem;
    private ShipComponent.Type selected;

    @Override
    protected void initialize() {
        super.initialize();

        // engage button.
        int index=0;
        for (ShipComponent.Type structure : ShipComponent.Type.values()) {
            int x = G.SCREEN_WIDTH + MARGIN_RIGHT - (index + 1) * 18;
            int y = 7;
            efs.createButton(x, y, 15, 15, "btn-construct", new ToolSelectButton(structure));
            // add icon over button. @todo merge with button logic.
            new EntityBuilder(world).with(new Pos(x + 4,y + 5 ), new Anim(structure.animId, 4000)).build();
            index++;
        }
    }


    @Override
    protected void processSystem() {

    }

    private void startPlacing(ShipComponent.Type selected) {
        this.selected = selected;
    }

    private class ToolSelectButton extends ButtonListener {

        private final ShipComponent.Type structure;

        private ToolSelectButton(ShipComponent.Type structure) {
            this.structure = structure;
        }

        @Override
        public void run() {
            startPlacing(this.structure);
        }

        @Override
        public boolean enabled() {
            // we don't want to allow building while busy!.
            return !travelSimulationSystem.isTraveling() && !dilemmaSystem.isDilemmaActive();
        }
    }

}
