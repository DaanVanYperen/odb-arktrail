package net.mostlyoriginal.game.manager;

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
    public static final int MARGIN_LEFT = 32;
    protected ComponentMapper<Pos> mPos;
    protected ComponentMapper<Anim> mAnim;

    public ShipComponentSystem() {
        super(Aspect.getAspectForAll(ShipComponent.class, Pos.class, Anim.class));
    }

    protected ComponentMapper<ShipComponent> mc;

    public static final int MAX_X = 50;
    public static final int MAX_Y = 50;

    Entity emap[][] = new Entity[MAX_Y][MAX_X];

    @Override
    protected void initialize() {
        super.initialize();

        // initialize basic ship.
        // create test expansion slot.
        createComponent(5, 5, ShipComponent.Type.ENGINE, ShipComponent.State.CONSTRUCTED);
        createComponent(6, 5, ShipComponent.Type.STORAGEPOD, ShipComponent.State.CONSTRUCTED);
        createComponent(6, 4, ShipComponent.Type.BUNKS, ShipComponent.State.CONSTRUCTED);
        createComponent(6, 6, ShipComponent.Type.BUNKS, ShipComponent.State.CONSTRUCTED);
    }

    /**
     * attempts to create a component at coordinates. will fail if out of bounds or already one there.
     */
    private Entity createComponent(int gridY, int gridX, ShipComponent.Type expansionSlot, ShipComponent.State state) {
        if (gridY < 0 || gridX < 0 || gridY >= MAX_X || gridX >= MAX_Y) return null;
        if (get(gridY, gridX) == null) {
            return new EntityBuilder(world).with(new Pos(), new Anim(), new ShipComponent(expansionSlot, gridY, gridX, state), new Bounds(0, 0, 8, 8), new Clickable()).build();
        }
        return null;
    }

    @Override
    protected void inserted(Entity e) {
        if (mc.has(e)) {
            ShipComponent shipComponent = mc.get(e);
            if (emap[shipComponent.gridY][shipComponent.gridX] == null) {
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
            if (emap[shipComponent.gridY][shipComponent.gridX] == e) {
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
        }
    }
}
