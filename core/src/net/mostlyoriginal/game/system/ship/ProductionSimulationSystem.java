package net.mostlyoriginal.game.system.ship;

import com.artemis.Aspect;
import com.artemis.ComponentMapper;
import com.artemis.Entity;
import com.artemis.annotations.Wire;
import com.artemis.managers.TagManager;
import com.artemis.systems.EntityProcessingSystem;
import com.artemis.utils.EntityBuilder;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.math.MathUtils;
import net.mostlyoriginal.api.component.basic.Bounds;
import net.mostlyoriginal.api.component.basic.Pos;
import net.mostlyoriginal.api.component.graphics.Anim;
import net.mostlyoriginal.api.component.graphics.ColorAnimation;
import net.mostlyoriginal.api.component.graphics.Renderable;
import net.mostlyoriginal.api.component.physics.Clamped;
import net.mostlyoriginal.api.component.physics.Homing;
import net.mostlyoriginal.api.component.physics.Physics;
import net.mostlyoriginal.api.utils.GdxUtil;
import net.mostlyoriginal.api.utils.reference.SafeEntityReference;
import net.mostlyoriginal.game.G;
import net.mostlyoriginal.game.component.ship.CrewMember;
import net.mostlyoriginal.game.component.ship.ShipComponent;
import net.mostlyoriginal.game.component.ui.Button;
import net.mostlyoriginal.game.component.ui.ButtonListener;
import net.mostlyoriginal.game.component.ui.Clickable;
import net.mostlyoriginal.game.component.ui.Label;
import net.mostlyoriginal.game.manager.AssetSystem;
import net.mostlyoriginal.game.system.ui.ConstructionSystem;

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
    public static final float BUILDERS_BONUS_FACTOR = 1f;
    protected ComponentMapper<ShipComponent> mShipComponent;
    protected ComponentMapper<Pos> mPos;
    public InventorySystem inventorySystem;
    public CrewSystem crewSystem;
    public float buildSpeed;
    private ShipComponentSystem shipComponentSystem;
    private TagManager tagManager;
    public Entity labelEntity;
    private AssetSystem assetSystem;
    private boolean hullBuilt;
    private HullSystem hullSystem;
    private ConstructionSystem constructionSystem;

    public ProductionSimulationSystem() {
        super(Aspect.getAspectForAll(ShipComponent.class));
    }

    protected ComponentMapper<Label> mLabel;

    @Override
    protected void initialize() {
        super.initialize();


        labelEntity = new EntityBuilder(world).with(new Label(""), new Renderable(), new Pos(4, G.SCREEN_HEIGHT - 42)).build();
    }

    @Override
    protected void begin() {
        super.begin();

        hullBuilt = false;

        int builders = (int) (crewSystem.countOf(CrewMember.Ability.BUILD) * BUILDERS_BONUS_FACTOR);

        Label buildSpeedLabel = mLabel.get(labelEntity);

        if (builders > 10) {
            buildSpeed = 5;
            buildSpeedLabel.text = "buildspeed x5";
        } else if (builders >= 8) {
            buildSpeed = 4;
            buildSpeedLabel.text = "buildspeed x4";
        } else if (builders >= 5) {
            buildSpeed = 3;
            buildSpeedLabel.text = "buildspeed x3";
        } else if (builders >= 3) {
            buildSpeed = 2;
            buildSpeedLabel.text = "buildspeed x2";
        } else {
            buildSpeed = 1;
            buildSpeedLabel.text = "buildspeed x1";
        }
    }

    @Override
    protected void end() {
        super.end();
        if (hullBuilt) {
            // make sure the hull updates the sprites.
            hullSystem.dirty();
        }
    }

    /**
     * Finish the whole ship at once.
     */
    public void finishAllConstruction() {
        for (Entity e : getActives()) {
            if (e != null) {
                ShipComponent shipComponent = mShipComponent.get(e);
                if (shipComponent != null && shipComponent.state == ShipComponent.State.UNDER_CONSTRUCTION) {
                    constructionSystem.complete(e);
                }
            }
        }
        hullSystem.dirty();
    }

    @Override
    protected void process(Entity e) {
        ShipComponent shipComponent = mShipComponent.get(e);

        if (shipComponent.state == ShipComponent.State.UNDER_CONSTRUCTION) {

            if (shipComponent.type != ShipComponent.Type.HULL) {
                if (buildSpeed > 0) {

                    // attempt to assign as many builders as we have.
                    float cost = MathUtils.clamp(shipComponent.constructionManyearsRemaining, 0, buildSpeed);
                    shipComponent.constructionManyearsRemaining -= cost;
                    buildSpeed -= cost;
                    if (shipComponent.constructionManyearsRemaining <= 0) {
                        constructionSystem.complete(e);
                    }
                }
            } else {
                // hull autobuilds.
                constructionSystem.complete(e);
                hullBuilt = true;
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
        final Entity cursor = tagManager.getEntity("cursor");

        assetSystem.playSfx("snd-squish");
        CollectCollectible listener = new CollectCollectible(resource);
        Button button = new Button(listener);
        button.autoclick = true;
        button.autoclickCooldown = 0.8f;
        Physics physics = new Physics();
        physics.vx = MathUtils.random(-15f, 15f);
        physics.vy = MathUtils.random(-15f, 15f);
        Homing homing = new Homing(new SafeEntityReference(cursor));
        homing.maxDistance = 40;
        homing.speedFactor = 0.5f;
        listener.entity =
                new EntityBuilder(world).
                        with(
                                new Pos(x, y),
                                physics,
                                new Clickable(),
                                new ColorAnimation(GdxUtil.convert(Color.CLEAR), GdxUtil.convert(Color.WHITE), GdxUtil.convert(Interpolation.linear), 1f, 1f),
                                homing,
                                new Clamped(0, 0, G.SCREEN_WIDTH, G.SCREEN_HEIGHT),
                                new Anim(resource.pickupAnimId),
                                new Renderable(10000),
                                new Bounds(0 - 4, 0 - 4, 8 + 4, 6 + 4), button).build();


    }

    public void spawnCollectibleRandomlyOnShip(InventorySystem.Resource resource) {
        Entity location = shipComponentSystem.getRandomPart();

        // revert to cursor location if none available.
        if (location == null) {
            location = tagManager.getEntity("cursor");
        }

        if (location != null) {
            Pos pos = mPos.get(location);
            if (pos != null) {
                spawnCollectible(pos.x + MathUtils.random(-4, 4), pos.y + MathUtils.random(-4, 4), resource);
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
            assetSystem.playSfx("snd-slurp");
            inventorySystem.alter(resource, 1);
            entity.deleteFromWorld();
        }
    }
}
