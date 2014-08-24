package net.mostlyoriginal.game.system.ship;

import com.artemis.Aspect;
import com.artemis.ComponentMapper;
import com.artemis.Entity;
import com.artemis.annotations.Wire;
import com.artemis.systems.EntityProcessingSystem;
import net.mostlyoriginal.api.component.graphics.Anim;
import net.mostlyoriginal.game.component.ship.ShipComponent;

/**
 * @author Daan van Yperen
 */
@Wire
public class HullSystem extends EntityProcessingSystem {

    private boolean dirty = true;

    ShipComponentSystem shipComponentSystem;
    protected ComponentMapper<Anim> mAnim;
    protected ComponentMapper<ShipComponent> mc;

    public HullSystem() {
        // @todo manual qualifiers.
        super(Aspect.getAspectForAll(ShipComponent.class, Anim.class));
    }

    @Override
    protected void begin() {
        super.begin();

        if (dirty) {
            dirty = false;
            generateMissingHullPieces();
            connectHullPieces();
        }
    }

    private static class Pattern {
        private final int[] layout;
        public final String animId;

        private Pattern( String animId, int ... layout ) {
            this.layout = layout;
            this.animId = animId;
        }

        public boolean matches(int[] pat) {
            for (int i = 0; i < 9; i++)
            {
                if ( layout[i] == 2 ) continue; // ignore!
                if ( pat[i] != layout[i] ) return false;
            }
            return true;
        }
    }

    // 0: empty
    // 1: wall (or engine, solar panel, scoop)
    // 2: ignore
    private static final Pattern[] patterns = {
            // top left
            new Pattern("hull-0",
                    2, 0, 2,
                    0, 1, 1,
                    2, 1, 1),
            new Pattern("hull-1",
                    2, 0, 2,
                    1, 1, 1,
                    2, 1, 2),
            // top right,
            new Pattern("hull-2",
                    2, 0, 2,
                    1, 1, 0,
                    1, 1, 2),
            // left
            new Pattern("hull-3",
                    2, 1, 2,
                    0, 1, 1,
                    2, 1, 2),
            // right
            new Pattern("hull-4",
                    2, 1, 2,
                    1, 1, 0,
                    2, 1, 2),
            // b-left
            new Pattern("hull-5",
                    2, 1, 1,
                    0, 1, 1,
                    0, 0, 2),
            // b-middle
            new Pattern("hull-6",
                    2, 1, 2,
                    1, 1, 1,
                    2, 0, 2),
            // b-right
            new Pattern("hull-7",
                    1, 1, 2,
                    1, 1, 0,
                    2, 0, 0),
            // b-right
            new Pattern("hull-inny-0",
                    1, 1, 2,
                    1, 1, 1,
                    2, 1, 0),
            // b-left
            new Pattern("hull-inny-1",
                    2, 1, 1,
                    1, 1, 1,
                    0, 1, 2),
            // t-right
            new Pattern("hull-inny-2",
                    2, 1, 0,
                    1, 1, 1,
                    1, 1, 1),
            // t-left
            new Pattern("hull-inny-3",
                    0, 1, 2,
                    1, 1, 1,
                    2, 1, 1)
    };

    private static final int[] pat = new int[9];

    private void connectHullPieces() {
        for (int gridY = 0; gridY < ShipComponentSystem.MAX_Y; gridY++) {
            for (int gridX = 0; gridX < ShipComponentSystem.MAX_X; gridX++) {
                final Entity entity = shipComponentSystem.get(gridX, gridY);
                if ( entity != null )
                {
                    // try to find non hull pieces.
                    ShipComponent shipComponent = mc.get(entity);
                    if ( shipComponent != null && shipComponent.type == ShipComponent.Type.HULL )
                    {
                        // attempt to place hull in all directions.
                        // @todo what am i doing @_@ IN A HURRY DON'T CARE.
                        pat[0] = structureAt(-1 + gridX, 1 + gridY);
                        pat[1] = structureAt(0 + gridX, 1 + gridY);
                        pat[2] = structureAt(1 + gridX, 1 + gridY);
                        pat[3] = structureAt(-1 + gridX, 0 + gridY);
                        pat[4] = 1;
                        pat[5] = structureAt(1 + gridX, 0 + gridY);
                        pat[6] = structureAt(-1 + gridX, -1 + gridY);
                        pat[7] = structureAt(0 + gridX, -1 + gridY);
                        pat[8] = structureAt(1 + gridX, -1 + gridY);

                        final Anim anim = mAnim.get(entity);
                        anim.id = "hull-missing";
                        for (Pattern pattern : patterns) {
                            if ( pattern.matches(pat))
                            {
                                anim.id = pattern.animId;
                                break;
                            }
                        }
                    }

                }
            }
        }
    }

    private int structureAt(int gridX, int gridY) {

        final Entity entity = shipComponentSystem.get(gridX, gridY);
        if ( entity != null ) {
            return 1;
        }

        // nothing here.
        return 0;
    }

    private void generateMissingHullPieces() {
        for (int gridY = 1; gridY < ShipComponentSystem.MAX_Y-1; gridY++) {
            for (int gridX = 1; gridX < ShipComponentSystem.MAX_X-1; gridX++) {
                final Entity entity = shipComponentSystem.get(gridX, gridY);
                if ( entity != null )
                {
                    // try to find non hull pieces.
                    ShipComponent shipComponent = mc.get(entity);
                    if ( shipComponent != null && !shipComponent.type.countsAsHull )
                    {
                        // attempt to place hull in all directions.
                        placeHull(-1 + gridX,-1 + gridY);
                        placeHull( 0 + gridX,-1 + gridY);
                        placeHull( 1 + gridX,-1 + gridY);
                        placeHull(-1 + gridX, 0 + gridY);
                        placeHull( 1 + gridX, 0 + gridY);
                        placeHull(-1 + gridX, 1 + gridY);
                        placeHull( 0 + gridX, 1 + gridY);
                        placeHull( 1 + gridX, 1 + gridY);
                    }

                }
            }
        }
    }

    // attempt to place hull at coordinates, if slot is empty.
    private void placeHull(int gridX, int gridY) {
        
        final Entity entity = shipComponentSystem.get(gridX, gridY);
        if ( entity == null ) {
               shipComponentSystem.createComponent(gridX, gridY, ShipComponent.Type.HULL, ShipComponent.State.CONSTRUCTED);
        }
    }

    // regenerate walls.
    public void dirty() {
        dirty = true;
    }

    @Override
    protected void process(Entity e) {
    }
}
