package net.mostlyoriginal.game.system.ship;

import com.artemis.Aspect;
import com.artemis.ComponentMapper;
import com.artemis.Entity;
import com.artemis.annotations.Wire;
import com.artemis.managers.TagManager;
import com.artemis.systems.EntityProcessingSystem;
import com.badlogic.gdx.math.MathUtils;
import net.mostlyoriginal.game.G;
import net.mostlyoriginal.game.component.ship.Inventory;
import net.mostlyoriginal.game.component.ui.Bar;
import net.mostlyoriginal.game.manager.EntityFactorySystem;

/**
 * Sync UI with inventory.
 *
 * @author Daan van Yperen
 */
@Wire
public class InventorySystem extends EntityProcessingSystem {

    public static final int BAR_OFFSET_Y = 8;
    public static final int LEFT_BAR_MARGIN = 4;
    public EntityFactorySystem efs;
    public Entity fuelIndicator;
    public Entity foodIndicator;
    protected ComponentMapper<Inventory> mInventory;
    protected ComponentMapper<Bar> mBar;
    private Inventory inventory;
    private TagManager tagManager;
    private CrewSystem crewSystem;
    private Entity biogelIndicator;

    public Inventory getInventory() {
        final Entity entity = tagManager.getEntity("travels");
        if ( entity != null ) {
            return mInventory.get(entity);
        }
        return null;
    }

    public enum Resource {
        FUEL("pickup-fuel"),
        BIOGEL("pickup-biogel"),
        CREWMEMBER("pickup-crew"),
        FOOD("pickup-food"),
        STORAGE(""), BIOGEL_STORAGE(""), THRUST("");
        public final String pickupAnimId;

        Resource(String pickupAnimId) {
            this.pickupAnimId = pickupAnimId;
        }
    }

    public InventorySystem() {
        super(Aspect.getAspectForAll(Inventory.class));
    }

    /** inc/dec resource by amount. */
    public void alter(Resource resource, int amount) {
        final Inventory inventory = getInventory();
        if ( inventory != null ) {
            switch (resource) {
                case FUEL:
                    inventory.fuel = MathUtils.clamp(inventory.fuel + amount, 0, inventory.maxFuel);
                    break;
                case FOOD:
                    inventory.food = MathUtils.clamp(inventory.food + amount, 0, inventory.maxFood);
                    break;
                case BIOGEL:
                    inventory.biogel = MathUtils.clamp(inventory.biogel + amount, 0, inventory.maxBiogel);
                    break;
                case STORAGE:
                    inventory.maxFuel = inventory.maxFood = inventory.maxFood + amount;
                    break;
                case BIOGEL_STORAGE:
                    inventory.maxBiogel = (int)MathUtils.clamp(inventory.maxBiogel + amount, 1f, 3f);
                    break;
                case THRUST:
                    inventory.thrust = inventory.thrust + amount;
                    break;
                case CREWMEMBER:
                    if ( amount > 0 ){
                        for ( int i=0; i<amount;i++) {
                            crewSystem.createRandomCrewmember();
                        }
                    }
                    break;
            }
        }
    }

    /** get resource amount. */
    public int get(Resource resource) {
        final Inventory inventory = getInventory();
        if ( inventory != null ) {
            switch (resource) {
                case FUEL:
                    return inventory.fuel;
                case FOOD:
                    return inventory.food;
                case BIOGEL:
                    return inventory.biogel;
                case STORAGE:
                    return inventory.maxBiogel;
                case THRUST:
                    return inventory.thrust;
            }
        }
        return 0;
    }

    @Override
    protected void initialize() {
        super.initialize();

        fuelIndicator = efs.createBar(LEFT_BAR_MARGIN, G.SCREEN_HEIGHT - 18, "fuel", "bar-fuel", "bar-fuel-open", 0 , 0 );
        foodIndicator = efs.createBar(LEFT_BAR_MARGIN, G.SCREEN_HEIGHT - 18 - BAR_OFFSET_Y, "food", "bar-food", "bar-food-open",0, 0);
        biogelIndicator = efs.createBar(LEFT_BAR_MARGIN, G.SCREEN_HEIGHT - 18 - BAR_OFFSET_Y*2, "biogel", "bar-biogel", "bar-biogel-open", 0, 0);
    }

    @Override
    protected void process(Entity e) {

        Inventory inventory = mInventory.get(e);
        updateBar(fuelIndicator, inventory.fuel, inventory.maxFuel - inventory.fuel);
        updateBar(foodIndicator, inventory.food, inventory.maxFood- inventory.food);
        updateBar(biogelIndicator, inventory.biogel, inventory.maxBiogel- inventory.biogel);
    }

    private void updateBar(Entity indicator, int value, int emptyValue) {
        Bar bar = mBar.get(indicator);
        bar.value = value;
        bar.valueEmpty = emptyValue;

    }
}
