package net.mostlyoriginal.game.component.ship;

import com.artemis.Component;

/**
 * @author Daan van Yperen
 */
public class Inventory extends Component {
    public static final int DEFAULT_FUEL = 0;
    public static final int DEFAULT_FOOD = 0;
    public static final int DEFAULT_BIOGEL = 0;
    public int fuel = DEFAULT_FUEL;
    public int food = DEFAULT_FOOD;
    public int biogel = DEFAULT_BIOGEL;

    public int maxFuel = 0;
    public int maxFood = 0;
    public int maxBiogel = 0;
    public int thrust = 0;
}
