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
import net.mostlyoriginal.game.G;
import net.mostlyoriginal.game.component.ship.Star;

/**
 * @author Daan van Yperen
 */
@Wire
public class AccelerationEffectSystem extends EntityProcessingSystem {

    protected TravelSimulationSystem travelSimulationSystem;

    /** speed factor of ship. */
    private float timer;
    public float speedFactor;

    protected ComponentMapper<Star> mStar;
    protected ComponentMapper<Anim> mAnim;
    protected ComponentMapper<Pos> mPos;
    private int animStage;

    public AccelerationEffectSystem() {
        super(Aspect.getAspectForAll(Star.class, Pos.class, Anim.class));
    }

    @Override
    protected void initialize() {
        super.initialize();

        for ( int i = 0 ; i<20; i++) {
            spawnStar(MathUtils.random(0, G.SCREEN_WIDTH), randomStarY(), MathUtils.random(100) < 10 ? 0 : MathUtils.random(100) < 15 ? 1 : 2);
        }

    }


    private int randomStarY() {
        return MathUtils.random(0, G.SCREEN_HEIGHT);
    }

    private void spawnStar(int x, int y, int kind) {
        new EntityBuilder(world).with(new Pos(x,y), new Star(kind), new Anim(-50)).build();
    }


    private void trustEffect() {

        // work towards full thrust.
        if ( travelSimulationSystem.isTraveling() )
        {
            timer += world.delta * 0.25f;
            timer = MathUtils.clamp(timer, 0f, 1f);
            speedFactor = Interpolation.exp5.apply(timer * 0.95f);
        } else {
            timer -= world.delta * 0.25f;
            timer = MathUtils.clamp(timer, 0f, 1f);
            speedFactor = Interpolation.exp5.apply(timer * 0.95f);
        }

    }

    @Override
    protected void begin() {
        super.begin();
        trustEffect();

        animStage = 0;

        if ( speedFactor > 0.6 )
        {
            animStage = 2;
        } else if ( speedFactor > 0.2 )
        {
            animStage = 1;
        }
    }

    @Override
    protected void process(Entity e) {

        // match animation to speed.
        Star star = mStar.get(e);
        mAnim.get(e).id = star.animId[animStage];

        Pos pos = mPos.get(e);

        // move star to the left, and randomize location to give the appearance of more stars.
        pos.x -= ( (5f + (speedFactor * 1000f)) * world.delta * star.speedFactor );
        if ( pos.x < -100 )
        {
            pos.x = G.SCREEN_WIDTH;
            pos.y = randomStarY();
        }
    }
}
