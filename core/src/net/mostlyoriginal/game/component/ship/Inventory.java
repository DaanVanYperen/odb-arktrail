package net.mostlyoriginal.game.component.ship;

import com.artemis.Component;

/**
 * @author Daan van Yperen
 */
public class Inventory extends Component {
    public static final int DEFAULT_FUEL = 5;
    public static final int DEFAULT_FOOD = 5;
    public int fuel = DEFAULT_FUEL;
    public int food = DEFAULT_FOOD;
}
