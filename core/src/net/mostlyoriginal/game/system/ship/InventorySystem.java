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
        FOOD("pickup-food");
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
                    inventory.fuel = MathUtils.clamp(inventory.fuel + amount, 0, 64);
                    break;
                case FOOD:
                    inventory.food = MathUtils.clamp(inventory.food + amount, 0, 64);
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
            }
        }
        return 0;
    }

    @Override
    protected void initialize() {
        super.initialize();

        fuelIndicator = efs.createBar(LEFT_BAR_MARGIN, G.SCREEN_HEIGHT - 16, "fuel", "bar-fuel", 5);
        foodIndicator = efs.createBar(LEFT_BAR_MARGIN, G.SCREEN_HEIGHT - 16 - BAR_OFFSET_Y, "food", "bar-food", 5);
    }

    @Override
    protected void process(Entity e) {

        Inventory inventory = mInventory.get(e);
        updateBar(fuelIndicator, inventory.fuel);
        updateBar(foodIndicator, inventory.food);
    }

    private void updateBar(Entity indicator, int value) {
        mBar.get(indicator).value = value;
    }
}
