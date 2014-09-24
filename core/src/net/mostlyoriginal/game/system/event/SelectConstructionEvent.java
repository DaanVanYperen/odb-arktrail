package net.mostlyoriginal.game.system.event;

import net.mostlyoriginal.api.event.common.Event;
import net.mostlyoriginal.game.component.ship.ShipComponent;

/**
 * Construction on component starting.
 *
 * Created by Daan on 14-9-2014.
 */
public class SelectConstructionEvent implements Event {

	public final ShipComponent.Type type;

	public SelectConstructionEvent(ShipComponent.Type type) {
		this.type = type;
	}
}
