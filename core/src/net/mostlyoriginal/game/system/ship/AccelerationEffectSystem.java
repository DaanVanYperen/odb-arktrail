package net.mostlyoriginal.game.system.ship;

import com.artemis.Aspect;
import com.artemis.Entity;
import com.artemis.annotations.Wire;
import com.artemis.systems.EntityProcessingSystem;
import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.math.MathUtils;
import net.mostlyoriginal.game.component.ship.AccelerationEffect;

/**
 * @author Daan van Yperen
 */
@Wire
public class AccelerationEffectSystem extends EntityProcessingSystem {

    protected TravelSimulationSystem travelSimulationSystem;

    /** speed factor of ship. */
    private float timer;
    public float speedFactor;

    public AccelerationEffectSystem() {
        super(Aspect.getAspectForAll(AccelerationEffect.class));
    }


    private void trustEffect() {

        // work towards full thrust.
        if ( travelSimulationSystem.isTraveling() )
        {
            timer += world.delta * 0.25f;
            timer = MathUtils.clamp(timer, 0f, 1f);
            speedFactor = Interpolation.exp5.apply(timer);
        } else {
            timer -= world.delta * 0.25f;
            timer = MathUtils.clamp(timer, 0f, 1f);
            speedFactor = Interpolation.exp5.apply(timer);
        }

    }

    @Override
    protected void begin() {
        super.begin();
        trustEffect();
    }

    @Override
    protected void process(Entity e) {
    }
}
