package net.mostlyoriginal.game.manager;

import com.artemis.Entity;
import com.artemis.annotations.Wire;
import com.artemis.managers.TagManager;
import com.artemis.utils.EntityBuilder;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.maps.MapProperties;
import com.badlogic.gdx.math.MathUtils;
import net.mostlyoriginal.api.component.basic.Angle;
import net.mostlyoriginal.api.component.basic.Bounds;
import net.mostlyoriginal.api.component.basic.Pos;
import net.mostlyoriginal.api.component.camera.Camera;
import net.mostlyoriginal.api.component.graphics.Anim;
import net.mostlyoriginal.api.component.map.MapWallSensor;
import net.mostlyoriginal.api.component.mouse.MouseCursor;
import net.mostlyoriginal.api.component.physics.*;
import net.mostlyoriginal.api.component.script.Schedule;
import net.mostlyoriginal.api.manager.AbstractAssetSystem;
import net.mostlyoriginal.api.manager.AbstractEntityFactorySystem;
import net.mostlyoriginal.api.utils.SafeEntityReference;
import net.mostlyoriginal.api.utils.TagEntityReference;
import net.mostlyoriginal.game.G;
import net.mostlyoriginal.game.MainScreen;
import net.mostlyoriginal.game.component.agent.PlayerControlled;
import net.mostlyoriginal.game.component.agent.Slumberer;
import net.mostlyoriginal.game.component.environment.RouteIndicator;
import net.mostlyoriginal.game.component.environment.RouteNode;
import net.mostlyoriginal.game.component.ship.CrewMember;
import net.mostlyoriginal.game.component.ship.Inventory;
import net.mostlyoriginal.game.component.ship.Travels;
import net.mostlyoriginal.game.component.ui.Bar;
import net.mostlyoriginal.game.component.ui.Button;
import net.mostlyoriginal.game.component.ui.ButtonListener;
import net.mostlyoriginal.game.component.ui.Clickable;
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

    @Override
    protected void initialize() {
        super.initialize();

        createSpaceshipMetadata();

        //createCrew("Daan", "crew-0", CrewMember.Effect.HEALTHY);
        createCrew("Rellik", "crew-1", CrewMember.Effect.DEAD);
//        createCrew("Flaterectomy", "crew-2", CrewMember.Effect.HEALTHY);
//        createCrew("Troop", "crew-1", CrewMember.Effect.HEALTHY);


        createCamera(G.CANVAS_WIDTH / 8, G.CANVAS_HEIGHT / 8);

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
        });

        createMousecursor();
    }

    private void createCrew(String name, String animId, CrewMember.Effect effect) {
        new EntityBuilder(world).with(new CrewMember(name, animId, effect)).build();
    }

    public Entity createBar(int x, int y, String label, String icon, int value) {
        return new EntityBuilder(world).with(new Pos(x,y), new Bar(label, icon, value)).build();
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

    private Entity createSlumberer(int cx, int cy) {
        Entity slumberer =
                defaultEntity(cx, cy, "slumberer-idle").addComponent(new Slumberer());
        slumberer.getComponent(Anim.class).layer = -2;

        Anim eyeAnim     = new Anim("slumberer-eye", -3);
        eyeAnim.loop     = false;
        Inbetween inbetween = new Inbetween(new SafeEntityReference(slumberer), new TagEntityReference(tagManager, "player"), 0.05f);
        inbetween.ax = 10;
        inbetween.ay = 26;
        inbetween.bx = 10;
        inbetween.by = 10;
        inbetween.maxDistance = 2f;
        Entity eye = world.createEntity()
                .addComponent(new Pos())
                .addComponent(eyeAnim)
                .addComponent(inbetween);
        eye.addToWorld();
        tagManager.register("slumberer-eye", eye);

        Anim eyelidAnim = new Anim("slumberer-eyelid", -1);
        eyelidAnim.loop = false;
        Entity eyelid = world.createEntity()
                .addComponent(new Pos())
                .addComponent(eyelidAnim)
                .addComponent(new Attached(new SafeEntityReference(slumberer), 12, 28));
        eyelid.addToWorld();
        tagManager.register("slumberer-eyelid", eyelid);

        return slumberer;
    }

    public Entity createSweat(int x, int y, String animId) {

        final Physics physics = new Physics();
        physics.vx = MathUtils.random(-90, 90)*1.5f;
        physics.vy = MathUtils.random(50, 110)*1.5f;
        physics.friction = 0.1f;

        final TextureRegion frame = abstractAssetSystem.get(animId).getKeyFrame(0);

        return basicCenteredParticle(x, y, animId, 1, 1)
                .addComponent(new Schedule().wait(1f).deleteFromWorld())
                .addComponent(physics)
                .addComponent(new Bounds(frame))
                .addComponent(new Gravity());
    }

    /**
     * Spawns a particle, animation centered on x,y.
     *
     * @param x
     * @param y
     * @param animId
     * @return
     */
    private Entity basicCenteredParticle(int x, int y, String animId, float scale, float speed) {
        Anim anim = new Anim(animId);
        anim.scale=scale;
        anim.speed=speed;
        anim.color.a= 0.9f;

        TextureRegion frame = abstractAssetSystem.get(animId).getKeyFrame(0);

        return world.createEntity()
                .addComponent(new Pos(x - ((frame.getRegionWidth() * anim.scale) / 2), y - (frame.getRegionHeight() * anim.scale) / 2))
                .addComponent(anim);
    }

    public void createCamera(int cx, int cy)
    {
        // now create a drone that will swerve towards the player which contains the camera. this will create a smooth moving camera.
        world.createEntity()
                .addComponent(new Pos(cx, cy))
                .addComponent(createCameraBounds())
                .addComponent(new Camera())
                .addToWorld();
    }

    private Entity createPlayer(int cx, int cy) {
        Entity player =
                defaultEntity(cx, cy, "player-idle")
                        .addComponent(new PlayerControlled())
                        .addComponent(new MapWallSensor());

        tagManager.register("player", player);

        // now create a drone that will swerve towards the player which contains the camera. this will create a smooth moving camera.
        world.createEntity()
                .addComponent(new Pos(0, 0))
                .addComponent(createCameraBounds())
                .addComponent(new Physics())
                .addComponent(new Homing(new SafeEntityReference(player)))
                .addComponent(new Camera())
                .addComponent(new Clamped(0, 0, 20 * 16, 15 * 16))
                .addToWorld();

        return player;
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

    private Entity defaultEntity(int cx, int cy, String startingAnim) {
        return world.createEntity()
                .addComponent(new Pos(cx, cy))
                .addComponent(new Angle())
                .addComponent(new Bounds(0, 0, 25, 16))
                .addComponent(new Anim(startingAnim))
                .addComponent(new Physics());
    }

    public Entity createButton(int x, int y, int width, int height, String animPrefix, ButtonListener listener)
    {
        return new EntityBuilder(world)
                .with(new Pos(x, y),
                        new Bounds(0, 0, width, height),
                        new Anim(1100),
                        new Button(animPrefix, listener),
                        new Clickable()).build();
    }

    public Entity createRouteNode(int x, int y, RouteNode.Action action, int order) {

        return new EntityBuilder(world)
                .with(new Pos(x,y),
                      new Bounds(0,0,8,8),
                      new Anim(900),
                      new RouteNode(action, order))
                .group("route").build();


    }

    public Entity createRouteIndicator() {
        return new EntityBuilder(world)
                .with(new Pos(5,5),
                      new Bounds(0,0,8,8),
                      new Anim("progress-indicator", 1000),
                      new RouteIndicator())
                .tag("routeindicator").build();
    }

    private Entity createMousecursor() {
        return new EntityBuilder(world).with(new MouseCursor(), new Pos(), new Bounds(-5, -5, -5, -5), new Anim("progress-indicator", MOUSE_CURSOR_LAYER)).tag("cursor").build();

    }
}
