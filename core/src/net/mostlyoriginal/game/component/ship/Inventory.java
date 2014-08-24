package net.mostlyoriginal.game.component.ship;

import com.artemis.Component;

/**
 * @author Daan van Yperen
 */
public class Inventory extends Component {
    public static final int DEFAULT_FUEL = 3;
    public static final int DEFAULT_FOOD = 3;
    public static final int DEFAULT_BIOGEL = 1;
    public int fuel = DEFAULT_FUEL;
    public int food = DEFAULT_FOOD;
    public int biogel = DEFAULT_BIOGEL;

    public int maxFuel = 3;
    public int maxFood = 3;
    public int maxBiogel = 1;
}
