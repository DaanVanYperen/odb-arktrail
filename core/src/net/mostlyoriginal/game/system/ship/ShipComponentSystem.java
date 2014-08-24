package net.mostlyoriginal.game.system.ship;

import com.artemis.Aspect;
import com.artemis.ComponentMapper;
import com.artemis.Entity;
import com.artemis.annotations.Wire;
import com.artemis.systems.EntityProcessingSystem;
import com.artemis.utils.EntityBuilder;
import net.mostlyoriginal.api.component.basic.Bounds;
import net.mostlyoriginal.api.component.basic.Pos;
import net.mostlyoriginal.api.component.graphics.Anim;
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
    private HullSystem hullSystem;

    public ShipComponentSystem() {
        super(Aspect.getAspectForAll(ShipComponent.class, Pos.class, Anim.class));
    }

    protected ComponentMapper<ShipComponent> mc;

    public static final int MAX_X = 32;
    public static final int MAX_Y = 13;

    Entity emap[][] = new Entity[MAX_Y][MAX_X];

    @Override
    protected void initialize() {
        super.initialize();

        int shipCenterX = 8;
        int shipCenterY = 8;

        // initialize basic ship.
        // create test expansion slot.
        createComponent(shipCenterX -1, shipCenterY, ShipComponent.Type.ENGINE, ShipComponent.State.CONSTRUCTED);
        createComponent(shipCenterX, shipCenterY, ShipComponent.Type.STORAGEPOD, ShipComponent.State.CONSTRUCTED);
        createComponent(shipCenterX, shipCenterY - 1, ShipComponent.Type.BUNKS, ShipComponent.State.CONSTRUCTED);
        createComponent(shipCenterX, shipCenterY + 1, ShipComponent.Type.BUNKS, ShipComponent.State.CONSTRUCTED);
        hullSystem.dirty();
    }

    /**
     * attempts to create a component at coordinates. will fail if out of bounds or already one there.
     */
    public Entity createComponent(int gridX, int gridY, ShipComponent.Type expansionSlot, ShipComponent.State state) {
        if (gridY < 0 || gridX < 0 || gridX >= MAX_X || gridY >= MAX_Y) return null;
        if (get(gridX, gridY) == null) {
            Entity entity = new EntityBuilder(world).with(new Pos(), new Anim(), new ShipComponent(expansionSlot, gridX, gridY, state), new Bounds(0, 0, 8, 8), new Clickable()).build();
            set(gridX,gridY, entity);
            return entity;
        }
        return null;
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
    protected void process(Entity e) {
        Pos pos = mPos.get(e);
        ShipComponent shipComponent = mc.get(e);
        pos.x = shipComponent.gridX * 8 + MARGIN_LEFT + shipComponent.type.xOffset;
        pos.y = shipComponent.gridY * 8 + MARGIN_TOP;

        Anim anim = mAnim.get(e);
        anim.layer = shipComponent.type.layer;
        if (shipComponent.type.animId != null) {
            anim.id = shipComponent.type.placedAnimId;
            anim.id2 = null;
        }
    }
}
