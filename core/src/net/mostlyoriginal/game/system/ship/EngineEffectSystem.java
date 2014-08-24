package net.mostlyoriginal.game.system.ship;

import com.artemis.Aspect;
import com.artemis.ComponentMapper;
import com.artemis.Entity;
import com.artemis.annotations.Wire;
import com.artemis.systems.EntityProcessingSystem;
import net.mostlyoriginal.api.component.basic.Pos;
import net.mostlyoriginal.api.component.graphics.Anim;
import net.mostlyoriginal.game.component.ship.EngineFlame;

/**
 * Flame effect for engines.
 *
 * @author Daan van Yperen
 */
@Wire
public class EngineEffectSystem extends EntityProcessingSystem {


    protected ComponentMapper<Anim> mAnim;
    protected ComponentMapper<Pos> mPos;
    protected ComponentMapper<EngineFlame> mEngineFlame;

    protected AccelerationEffectSystem accelerationEffectSystem;

    public EngineEffectSystem() {
        super(Aspect.getAspectForAll(Anim.class, Pos.class, EngineFlame.class));
    }

    @Override
    protected void process(Entity e) {
        Anim anim = mAnim.get(e);
        EngineFlame flame = mEngineFlame.get(e);
        flame.timer += world.delta;
        Pos pos = mPos.get(e);

        pos.x = flame.gridX * 8 + ShipComponentSystem.MARGIN_LEFT + 10f * accelerationEffectSystem.speedFactor + 2;
        pos.y = flame.gridY * 8 + ShipComponentSystem.MARGIN_TOP;

        // no flame when stationary
        if ( accelerationEffectSystem.speedFactor <= 0 ) anim.id = null;
        // flame when moving!
        else if ( accelerationEffectSystem.speedFactor <= 0.1f ) anim.id = "engine-0";
        // flicker the engine.
        else anim.id = (flame.timer * 2f) % 0.5f < 0.25f  ? "engine-1" : "engine-2";

    }
}
