package net.mostlyoriginal.game.system.ship;

import com.artemis.Aspect;
import com.artemis.ComponentMapper;
import com.artemis.Entity;
import com.artemis.annotations.Wire;
import com.artemis.systems.EntityProcessingSystem;
import com.artemis.utils.EntityBuilder;
import com.artemis.utils.ImmutableBag;
import com.badlogic.gdx.math.MathUtils;
import net.mostlyoriginal.api.component.basic.Bounds;
import net.mostlyoriginal.api.component.basic.Pos;
import net.mostlyoriginal.api.component.graphics.Anim;
import net.mostlyoriginal.game.component.ship.EngineFlame;
import net.mostlyoriginal.game.component.ship.ShipComponent;
import net.mostlyoriginal.game.component.ui.Clickable;

/**
 * Holds a spatial map of the ship.
 *
 * @author Daan van Yperen
 */
@Wire
public class ShipComponentSystem extends EntityProcessingSystem {

    public static final int MARGIN_TOP = 32;
    public static final int MARGIN_LEFT = 16;
    protected ComponentMapper<Pos> mPos;
    protected ComponentMapper<Anim> mAnim;
    protected ComponentMapper<ShipComponent> mShipComponent;
    private HullSystem hullSystem;
    private AccelerationEffectSystem accelerationEffectSystem;
    private InventorySystem inventorySystem;

    public ShipComponentSystem() {
        super(Aspect.getAspectForAll(ShipComponent.class, Pos.class, Anim.class));
    }

    protected ComponentMapper<ShipComponent> mc;

    public static final int MAX_X = 32;
    public static final int MAX_Y = 16;

    Entity emap[][] = new Entity[MAX_Y][MAX_X];

    @Override
    protected void initialize() {
        super.initialize();

        int shipCenterX = 8;
        int shipCenterY = 8;

        // initialize basic ship.
        // create test expansion slot.
        final ShipComponent.State constructed = ShipComponent.State.UNDER_CONSTRUCTION;
        //createComponent(shipCenterX -1, shipCenterY+1, ShipComponent.Type.ENGINE, constructed);

        for (int x = -1; x <= 1; x++) {
            for (int y = -2; y <= 2; y++) {
                createComponent(shipCenterX+x, shipCenterY+y, ShipComponent.Type.HULL, constructed);
            }
        }

        createComponent(shipCenterX - 2, shipCenterY, ShipComponent.Type.CHAIN, constructed);
        createComponent(shipCenterX - 3, shipCenterY, ShipComponent.Type.CHAIN, constructed);
        createComponent(shipCenterX - 4, shipCenterY, ShipComponent.Type.CHAIN, constructed);

        hullSystem.dirty();
    }

    /**
     * attempts to create a component at coordinates. will fail if out of bounds or already one there.
     */
    public Entity createComponent(int gridX, int gridY, ShipComponent.Type type, ShipComponent.State state) {
        if (gridY < 0 || gridX < 0 || gridX >= MAX_X || gridY >= MAX_Y) return null;
        if (get(gridX, gridY) == null) {
            Entity entity = new EntityBuilder(world).with(new Pos(), new Anim(), new ShipComponent(type, gridX, gridY, ShipComponent.State.UNDER_CONSTRUCTION), new Bounds(0, 0, 8, 8), new Clickable()).build();
            set(gridX, gridY, entity);

            if (state == ShipComponent.State.CONSTRUCTED) {
                completeConstructionOf(entity);
            }
            return entity;
        }
        return null;
    }

    public void completeConstructionOf(Entity entity) {
        final ShipComponent c = mShipComponent.get(entity);
        if (c.state == ShipComponent.State.UNDER_CONSTRUCTION) {
            c.state = ShipComponent.State.CONSTRUCTED;
            switch (c.type) {
                case HULL:
                    break;
                case BUNKS:
                    break;
                case MEDBAY:
                    inventorySystem.alter(InventorySystem.Resource.BIOGEL_STORAGE, 1);
                    break;
                case HYDROPONICS:
                    break;
                case STORAGEPOD:
                    inventorySystem.alter(InventorySystem.Resource.STORAGE, 1);
                    break;
                case ENGINE:
                    createEngineFlame(c.gridX - 3, c.gridY);
                    inventorySystem.alter(InventorySystem.Resource.THRUST, 1);
                    break;
                case RAMSCOOP:
                    break;
            }
        }
    }

    private void createEngineFlame(int gridX, int gridY) {
        Entity entity = new EntityBuilder(world).with(new Pos(), new Anim(600), new EngineFlame(gridX, gridY)).build();
    }

    @Override
    protected void inserted(Entity e) {
        if (mc.has(e)) {
            ShipComponent shipComponent = mc.get(e);
            if (get(shipComponent.gridX, shipComponent.gridY) == null) {
                set(shipComponent.gridX, shipComponent.gridY, e);
            }
        }
        super.inserted(e);
    }

    public Entity get(int x, int y) {
        if (x < 0 || y < 0 || x >= MAX_X || y >= MAX_Y) return null;
        return emap[y][x];
    }

    public void set(int x, int y, Entity e) {
        if (x < 0 || y < 0 || x >= MAX_X || y >= MAX_Y) return;
        emap[y][x] = e;
    }

    @Override
    protected void removed(Entity e) {
        if (mc.has(e)) {
            ShipComponent shipComponent = mc.get(e);
            if (get(shipComponent.gridX, shipComponent.gridY) == e) {
                set(shipComponent.gridX, shipComponent.gridY, null);
            }
        }
        super.removed(e);
    }

    @Override
    protected void begin() {
        super.begin();
    }

    @Override
    protected void process(Entity e) {

        Pos pos = mPos.get(e);
        ShipComponent shipComponent = mc.get(e);
        pos.x = shipComponent.gridX * 8 + MARGIN_LEFT + shipComponent.type.xOffset + 10f * accelerationEffectSystem.speedFactor;
        pos.y = shipComponent.gridY * 8 + MARGIN_TOP;

        Anim anim = mAnim.get(e);
        anim.layer = shipComponent.type.layer;
        if (shipComponent.type.animId != null) {
            anim.id = shipComponent.state == ShipComponent.State.UNDER_CONSTRUCTION ? shipComponent.type.buildingAnimId : shipComponent.type.placedAnimId;
            anim.id2 = null;
        }
    }

    public int shipValue() {

        int count = 0;
        for (Entity entity : getActives()) {
            if (mc.has(entity)) {
                ShipComponent shipComponent = mc.get(entity);
                count += shipComponent.type.pointValue;
            }
        }

        return count;
    }

    /**
     * Fetch random ship part.
     */
    public Entity getRandomPart() {
        final ImmutableBag<Entity> actives = getActives();
        return actives.isEmpty() ? null : actives.get(MathUtils.random(0, actives.size() - 1));
    }
}
