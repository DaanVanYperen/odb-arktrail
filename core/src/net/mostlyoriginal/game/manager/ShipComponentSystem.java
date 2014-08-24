package net.mostlyoriginal.game.manager;

import com.artemis.Aspect;
import com.artemis.ComponentMapper;
import com.artemis.Entity;
import com.artemis.annotations.Wire;
import com.artemis.systems.EntityProcessingSystem;
import net.mostlyoriginal.api.component.basic.Pos;
import net.mostlyoriginal.game.component.ship.ShipComponent;

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

    public ShipComponentSystem() {
        super(Aspect.getAspectForAll(ShipComponent.class, Pos.class));
    }

    protected ComponentMapper<ShipComponent> mc;

    public static final int MAX_X = 50;
    public static final int MAX_Y = 50;

    Entity emap[][] = new Entity[MAX_Y][MAX_X];

    @Override
    protected void initialize() {
        super.initialize();
    }

    @Override
    protected void inserted(Entity e) {
        if ( mc.has(e))
        {
            ShipComponent shipComponent = mc.get(e);
            if ( emap[shipComponent.gridY][shipComponent.gridX] == null ) {
                set(shipComponent.gridX, shipComponent.gridY, e);
            }
        }
        super.inserted(e);
    }

    public Entity get(int x, int y)
    {
        if ( x < 0 || y < 0 || x >= MAX_X || y >= MAX_Y ) return null;
        return emap[y][x];
    }

    public void set(int x, int y, Entity e) {
        if ( x < 0 || y < 0 || x >= MAX_X || y >= MAX_Y ) return;
        emap[y][x] = e;
    }

    @Override
    protected void removed(Entity e) {
        if ( mc.has(e))
        {
            ShipComponent shipComponent = mc.get(e);
            if ( emap[shipComponent.gridY][shipComponent.gridX] == e )
            {
                set(shipComponent.gridX, shipComponent.gridY, null);
            }
        }
        super.removed(e);
    }

    @Override
    protected void process(Entity e) {
        Pos pos = mPos.get(e);
        ShipComponent shipComponent = mc.get(e);
        pos.x = shipComponent.gridX * 8 + MARGIN_TOP;
        pos.y = shipComponent.gridY * 8 + MARGIN_LEFT;
    }
}
