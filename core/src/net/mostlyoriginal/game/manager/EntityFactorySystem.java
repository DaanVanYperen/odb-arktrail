package net.mostlyoriginal.game.manager;

import com.artemis.Entity;
import com.artemis.annotations.Wire;
import com.artemis.managers.TagManager;
import com.artemis.utils.EntityBuilder;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.maps.MapProperties;
import net.mostlyoriginal.api.component.basic.Angle;
import net.mostlyoriginal.api.component.basic.Bounds;
import net.mostlyoriginal.api.component.basic.Pos;
import net.mostlyoriginal.api.component.camera.Camera;
import net.mostlyoriginal.api.component.graphics.Anim;
import net.mostlyoriginal.api.component.graphics.Color;
import net.mostlyoriginal.api.component.graphics.Renderable;
import net.mostlyoriginal.api.component.map.MapWallSensor;
import net.mostlyoriginal.api.component.mouse.MouseCursor;
import net.mostlyoriginal.api.component.physics.Clamped;
import net.mostlyoriginal.api.component.physics.Homing;
import net.mostlyoriginal.api.component.physics.Physics;
import net.mostlyoriginal.api.manager.AbstractAssetSystem;
import net.mostlyoriginal.api.manager.AbstractEntityFactorySystem;
import net.mostlyoriginal.api.utils.reference.SafeEntityReference;
import net.mostlyoriginal.game.G;
import net.mostlyoriginal.game.MainScreen;
import net.mostlyoriginal.game.component.agent.PlayerControlled;
import net.mostlyoriginal.game.component.environment.RouteIndicator;
import net.mostlyoriginal.game.component.environment.RouteNode;
import net.mostlyoriginal.game.component.ship.CrewMember;
import net.mostlyoriginal.game.component.ship.EngineFlame;
import net.mostlyoriginal.game.component.ship.Inventory;
import net.mostlyoriginal.game.component.ship.Travels;
import net.mostlyoriginal.game.component.ui.Bar;
import net.mostlyoriginal.game.component.ui.Button;
import net.mostlyoriginal.game.component.ui.ButtonListener;
import net.mostlyoriginal.game.component.ui.Clickable;
import net.mostlyoriginal.game.system.ship.CrewSystem;
import net.mostlyoriginal.game.system.ship.LifesupportSimulationSystem;
import net.mostlyoriginal.game.system.ship.TravelSimulationSystem;
import net.mostlyoriginal.game.system.ui.DilemmaSystem;

/**
 * Game specific entity factory.
 *
 * @todo transform this into a manager.
 * @author Daan van Yperen
 */
@Wire
public class EntityFactorySystem extends AbstractEntityFactorySystem {

    public static final int MOUSE_CURSOR_LAYER = 9999;
    private TagManager tagManager;
    private AbstractAssetSystem abstractAssetSystem;
    private TravelSimulationSystem travelSimulationSystem;
    private DilemmaSystem dilemmaSystem;
    private CrewSystem crewSystem;
    private LifesupportSimulationSystem lifesupportSimulationSystem;

    @Override
    protected void initialize() {
        super.initialize();

        createSpaceshipMetadata();


        createCamera(G.CANVAS_WIDTH / 8, G.CANVAS_HEIGHT / 8);

        createMousecursor();
    }

    public void createScanButton() {
        // engage button.
        createButton(G.SCREEN_WIDTH - 56 - 4 - 35, 7, 31, 15, "btn-scan", new ButtonListener() {
            @Override
            public void run() {
                dilemmaSystem.scanDilemma();
                lifesupportSimulationSystem.process();
            }

            @Override
            public boolean enabled() {
                // we don't want to allow engaging while busy!.
                return !travelSimulationSystem.isTraveling() && !dilemmaSystem.isDilemmaActive() && crewSystem.countOf(CrewMember.Ability.BUILD) > 0;
            }
        }, "Stick around and look for trouble!");
    }

    public void createEngageButton() {
        // engage button.
        createButton(G.SCREEN_WIDTH - 56 - 4, 4, 56, 15, "btn-engage", new ButtonListener() {
            @Override
            public void run() {
                travelSimulationSystem.planWarp();
            }

            @Override
            public boolean enabled() {
                // we don't want to allow engaging while busy!.
                return !travelSimulationSystem.isTraveling() && !dilemmaSystem.isDilemmaActive();
            }
        }, "Warp to next landmark. Plan ahead and build!");
    }

    public Entity createBar(int x, int y, String label, String icon, String iconEmpty, int value, int valueEmpty) {
        return new EntityBuilder(world).with(new Pos(x,y), new Renderable(), new Bar(label, icon, value, iconEmpty, valueEmpty)).build();
    }

    private void createSpaceshipMetadata() {
        new EntityBuilder(world).with(
                new Travels(),
                new Inventory()).tag("travels").build();
    }

    @Override
    public Entity createEntity(String entity, int cx, int cy, MapProperties properties) {
        return null;
    }


    public void createCamera(int cx, int cy)
    {
        // now create a drone that will swerve towards the player which contains the camera. this will create a smooth moving camera.
        world.createEntity().edit().addComponent(new Pos(cx, cy))
                .addComponent(createCameraBounds())
                .addComponent(new Camera());
    }

    private Bounds createCameraBounds() {
        // convert viewport into bounds.
        return new Bounds(
                (-Gdx.graphics.getWidth() / 2) / MainScreen.CAMERA_ZOOM_FACTOR,
                (-Gdx.graphics.getHeight() / 2) / MainScreen.CAMERA_ZOOM_FACTOR,
                (Gdx.graphics.getWidth() / 2) / MainScreen.CAMERA_ZOOM_FACTOR,
                (Gdx.graphics.getHeight() / 2) / MainScreen.CAMERA_ZOOM_FACTOR
        );
    }

    public Entity createButton(int x, int y, int width, int height, String animPrefix, ButtonListener listener, String hint)
    {
        return new EntityBuilder(world)
                .with(new Pos(x, y),
                        new Bounds(0, 0, width, height),
                        new Anim(),
                        new Color(),
                        new Renderable(1100),
                        new Button(animPrefix, listener, hint),
                        new Clickable()).build();
    }

    public Entity createRouteNode(int x, int y, RouteNode.Action action, int order) {

        return new EntityBuilder(world)
                .with(new Pos(x,y),
                      new Bounds(0,0,8,8),
                      new Anim(),
                      new Renderable(900),
                      new RouteNode(action, order))
                .group("route").build();


    }

    public Entity createRouteIndicator() {
        return new EntityBuilder(world)
                .with(new Pos(5,5),
                      new Bounds(0,0,8,8),
                      new Anim("progress-indicator"),
                      new Renderable(1000),
                      new RouteIndicator())
                .tag("routeindicator").build();
    }

    private Entity createMousecursor() {
        return new EntityBuilder(world).with(
                new MouseCursor(),
                new Pos(),
                new Bounds(0,0,0,0),
                new Anim("cursor"),
                new Renderable(MOUSE_CURSOR_LAYER)).tag("cursor").build();

    }

    public void createEngineFlame(int gridX, int gridY) {
        Entity entity = new EntityBuilder(world).with(new Pos(),
                new Anim(),
                new Renderable(600),
        new EngineFlame(gridX, gridY)).build();
    }
}
