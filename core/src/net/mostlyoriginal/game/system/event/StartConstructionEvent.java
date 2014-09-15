package net.mostlyoriginal.game.system.event;

import net.mostlyoriginal.api.event.common.Event;
import net.mostlyoriginal.game.component.ship.ShipComponent;

/**
 * Constructable part selected in UI.
 *
 * Created by Daan on 14-9-2014.
 */
public class StartConstructionEvent implements Event {

	public final ShipComponent.Type type;

	public StartConstructionEvent(ShipComponent.Type type) {
		this.type = type;
	}
}
