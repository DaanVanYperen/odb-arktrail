package net.mostlyoriginal.game.system.ship;

import com.artemis.Aspect;
import com.artemis.ComponentMapper;
import com.artemis.Entity;
import com.artemis.annotations.Wire;
import com.artemis.systems.EntityProcessingSystem;
import com.artemis.utils.EntityBuilder;
import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.math.MathUtils;
import net.mostlyoriginal.api.component.basic.Pos;
import net.mostlyoriginal.api.component.graphics.Anim;
import net.mostlyoriginal.api.component.graphics.Color;
import net.mostlyoriginal.api.component.graphics.Renderable;
import net.mostlyoriginal.game.G;
import net.mostlyoriginal.game.component.ship.Star;
import net.mostlyoriginal.game.manager.AssetSystem;

/**
 * @author Daan van Yperen
 */
@Wire
public class AccelerationEffectSystem extends EntityProcessingSystem {

    public static final int BIGGEST_STAR_WIDTH = 100;
    protected TravelSimulationSystem travelSimulationSystem;

    /**
     * speed factor of ship.
     */
    private float timer;
    public float speedFactor;

    protected ComponentMapper<Star> mStar;
    protected ComponentMapper<Anim> mAnim;
    protected ComponentMapper<Pos> mPos;
    private int animStage;
    private int playSoundState=0;
    private AssetSystem assetSystem;

    public AccelerationEffectSystem() {
        super(Aspect.getAspectForAll(Star.class, Pos.class, Anim.class));
    }

    @Override
    protected void initialize() {
        super.initialize();

        for (int i = 0; i < 60; i++) {
            spawnStar(MathUtils.random(0, G.SCREEN_WIDTH + BIGGEST_STAR_WIDTH), randomStarY(), MathUtils.random(100) < 20 ? 1 : MathUtils.random(100) < 20 ? 0 : 2);
        }

    }


    private int randomStarY() {
        return MathUtils.random(0, G.SCREEN_HEIGHT);
    }

    private void spawnStar(int x, int y, int kind) {
        new EntityBuilder(world).with(
                new Pos(x, y),
                new Star(kind),
                new Anim(),
                new Renderable(-50),
                new Color(MathUtils.random(0.6f,1f),MathUtils.random(0.6f,1f),MathUtils.random(0.6f,1f),MathUtils.random(kind == 0 ? 0.1f : 0.5f,0.9f))).build();
    }


    private void trustEffect() {

        // work towards full thrust.
        if (travelSimulationSystem.isTraveling()) {
            timer += world.delta * 0.25f;
            timer = MathUtils.clamp(timer, 0f, 1f);
            speedFactor = Interpolation.exp5.apply(timer * 0.95f);

            if ( playSoundState == 0 )
            {
                playSoundState = 1;
                assetSystem.playSfx("snd-speedup");
            }
        } else {
            timer -= world.delta * 0.25f;
            timer = MathUtils.clamp(timer, 0f, 1f);
            speedFactor = Interpolation.exp5.apply(timer * 0.95f);
            if ( playSoundState == 1 )
            {
                playSoundState = 0;
                assetSystem.playSfx("snd-slowdown");
            }
        }

    }

    @Override
    protected void begin() {
        super.begin();
        trustEffect();

        if (speedFactor > 0.5) {
            animStage = 3;
        } else if (speedFactor > 0.25) {
            animStage = 2;
        } else if (speedFactor > 0.05) {
            animStage = 1;
        } else animStage = 0;
    }

    @Override
    protected void process(Entity e) {

        // match animation to speed.
        Star star = mStar.get(e);
        Anim anim = mAnim.get(e);

        int id = 2 + animStage;
        if (animStage == 0) {
            // just blinking
            id = (int) (star.blinkTimer % 3f);
            star.blinkTimer += world.delta;
        }

        anim.id = star.animId[id];

        Pos pos = mPos.get(e);

        // move star to the left, and randomize location to give the appearance of more stars.
        pos.x -= ((8f + (speedFactor * 1000f)) * world.delta * star.speedFactor);
        if (pos.x < -BIGGEST_STAR_WIDTH) {
            pos.x = G.SCREEN_WIDTH;
            pos.y = randomStarY();
        }
    }
}
