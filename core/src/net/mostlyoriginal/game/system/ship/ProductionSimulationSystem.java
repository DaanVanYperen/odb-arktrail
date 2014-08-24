package net.mostlyoriginal.game.system.ship;

import com.artemis.Aspect;
import com.artemis.ComponentMapper;
import com.artemis.Entity;
import com.artemis.annotations.Wire;
import com.artemis.managers.TagManager;
import com.artemis.systems.EntityProcessingSystem;
import com.artemis.utils.EntityBuilder;
import com.badlogic.gdx.math.MathUtils;
import net.mostlyoriginal.api.component.basic.Bounds;
import net.mostlyoriginal.api.component.basic.Pos;
import net.mostlyoriginal.api.component.graphics.Anim;
import net.mostlyoriginal.api.component.physics.Clamped;
import net.mostlyoriginal.api.component.physics.Physics;
import net.mostlyoriginal.game.G;
import net.mostlyoriginal.game.component.ship.CrewMember;
import net.mostlyoriginal.game.component.ship.ShipComponent;
import net.mostlyoriginal.game.component.ui.Button;
import net.mostlyoriginal.game.component.ui.ButtonListener;
import net.mostlyoriginal.game.component.ui.Clickable;

/**
 * Production simulation of the ship modules.
 * <p/>
 * Should be called manually by the travel simulation.
 *
 * @author Daan van Yperen
 * @todo this is really a subsystem of the travel simulation system.
 */
@Wire
public class ProductionSimulationSystem extends EntityProcessingSystem {

    public static final float CREW_FED_PER_FOOD = 1.25f;
    protected ComponentMapper<ShipComponent> mShipComponent;
    protected ComponentMapper<Pos> mPos;
    public InventorySystem inventorySystem;
    public CrewSystem crewSystem;
    public int builders;
    private ShipComponentSystem shipComponentSystem;
    private TagManager tagManager;

    public ProductionSimulationSystem() {
        super(Aspect.getAspectForAll(ShipComponent.class));
    }

    @Override
    protected void begin() {
        super.begin();

        builders = crewSystem.countOf(CrewMember.Ability.BUILD);
    }

    @Override
    protected void end() {
        super.end();
    }

    @Override
    protected void process(Entity e) {
        ShipComponent shipComponent = mShipComponent.get(e);

        if (shipComponent.state == ShipComponent.State.UNDER_CONSTRUCTION) {
            if ( builders > 0 ) {
                // attempt to assign as many builders as we have.
                float cost = MathUtils.clamp(shipComponent.constructionManyearsRemaining, 0, builders);
                shipComponent.constructionManyearsRemaining -= cost;
                builders -= cost;
                if (shipComponent.constructionManyearsRemaining <= 0) {
                    shipComponentSystem.completeConstructionOf(e);
                }
            }
        } else
            switch (shipComponent.type) {
                case HULL:
                    break;
                case BUNKS:
                    randomlyProduce(e, InventorySystem.Resource.CREWMEMBER, 0.25f);
                    break;
                case MEDBAY:
                    randomlyProduce(e, InventorySystem.Resource.BIOGEL, 0.25f);
                    break;
                case HYDROPONICS:
                    randomlyProduce(e, InventorySystem.Resource.FOOD, 0.25f);
                    break;
                case STORAGEPOD:
                    break;
                case ENGINE:
                    inventorySystem.alter(InventorySystem.Resource.FUEL, -1);
                    break;
                case RAMSCOOP:
                    randomlyProduce(e, InventorySystem.Resource.FUEL, 0.25f);
                    break;
            }
    }

    private void randomlyProduce(Entity e, InventorySystem.Resource resource, float chance) {
        if (MathUtils.random(0f, 1f) < chance) {
            Pos pos = mPos.get(e);
            spawnCollectible(pos.x, pos.y, resource);
        }
    }

    public void spawnCollectible(float x, float y, InventorySystem.Resource resource) {

        CollectCollectible listener = new CollectCollectible(resource);
        Button button = new Button(listener);
        button.autoclick = true;
        button.autoclickCooldown = 1.5f;
        Physics physics = new Physics();
        physics.vx = MathUtils.random(-10f, 10f);
        physics.vy = MathUtils.random(-10f, 10f);
        listener.entity =
                new EntityBuilder(world).
                        with(
                                new Pos(x, y),
                                physics,
                                new Clickable(),
                                new Clamped(0,0, G.SCREEN_WIDTH, G.SCREEN_HEIGHT),
                                new Anim(resource.pickupAnimId, 10000), new Bounds(0, 0, 8, 6), button).build();


    }

    public void spawnCollectibleNearMouse(InventorySystem.Resource resource) {
        final Entity cursor = tagManager.getEntity("cursor");
        if ( cursor != null ) {
            Pos pos = mPos.get(cursor);
            if ( pos != null ) {
                spawnCollectible(pos.x + MathUtils.random(-10, 10),pos.y + MathUtils.random(-10, 10), resource);
            }
        }

    }

    private class CollectCollectible extends ButtonListener {
        private final InventorySystem.Resource resource;
        public Entity entity;

        public CollectCollectible(InventorySystem.Resource resource) {
            this.resource = resource;
        }

        @Override
        public void run() {
            super.run();
            inventorySystem.alter(resource, 1);
            entity.deleteFromWorld();
        }
    }
}
