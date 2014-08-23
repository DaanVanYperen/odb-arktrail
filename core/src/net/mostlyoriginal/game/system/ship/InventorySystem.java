package net.mostlyoriginal.game.system.ship;

import com.artemis.Aspect;
import com.artemis.ComponentMapper;
import com.artemis.Entity;
import com.artemis.annotations.Wire;
import com.artemis.systems.EntityProcessingSystem;
import com.artemis.utils.Bag;
import com.artemis.utils.ImmutableBag;
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

    public enum Resource {
        FUEL,
        FOOD
    }

    public InventorySystem() {
        super(Aspect.getAspectForAll(Inventory.class));
    }

    public void alter(Resource resource, int amount) {
        final ImmutableBag<Entity> actives = getActives();
        final Object[] array = ((Bag<Entity>) actives).getData();
        for (int i = 0, s = actives.size(); s > i; i++) {
            final Entity e = (Entity)array[i];
            if ( e != null ) {
                final Inventory inventory = mInventory.get(e);
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
