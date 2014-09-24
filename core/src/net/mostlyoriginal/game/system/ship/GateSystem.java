package net.mostlyoriginal.game.system.ship;

import com.artemis.Aspect;
import com.artemis.ComponentMapper;
import com.artemis.Entity;
import com.artemis.annotations.Wire;
import com.artemis.systems.EntityProcessingSystem;
import com.artemis.utils.EntityBuilder;
import com.badlogic.gdx.math.MathUtils;
import net.mostlyoriginal.api.component.basic.Pos;
import net.mostlyoriginal.api.component.graphics.Anim;
import net.mostlyoriginal.api.component.graphics.Renderable;
import net.mostlyoriginal.game.component.ship.Gate;

/**
 * @author Daan van Yperen
 */
@Wire
public class GateSystem extends EntityProcessingSystem {

    protected ComponentMapper<Anim> mAnim;
    protected ComponentMapper<Pos> mPos;

    float bobTimer=0;

    protected AccelerationEffectSystem accelerationEffectSystem;

    public GateSystem() {
        super(Aspect.getAspectForAll(Gate.class, Pos.class, Anim.class));
    }


    @Override
    protected void initialize() {
        super.initialize();

        new EntityBuilder(world).with(new Gate(), new Pos(), new Anim("gate"), new Renderable()).build();
    }

    @Override
    protected void begin() {
        super.begin();
        bobTimer+= world.delta;
    }

    @Override
    protected void process(Entity e) {
        Anim anim = mAnim.get(e);
        Pos pos = mPos.get(e);
        pos.x = 30 + 9f * accelerationEffectSystem.speedFactor + 2;
        pos.y = 80 - 8 + MathUtils.sin(bobTimer * 0.5f) * 1f;
    }
}
