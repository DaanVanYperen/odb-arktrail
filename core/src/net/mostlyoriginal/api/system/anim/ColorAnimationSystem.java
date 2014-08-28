package net.mostlyoriginal.api.system.anim;

import com.artemis.Aspect;
import com.artemis.ComponentMapper;
import com.artemis.Entity;
import com.artemis.annotations.Wire;
import com.artemis.systems.EntityProcessingSystem;
import net.mostlyoriginal.api.component.graphics.Anim;
import net.mostlyoriginal.api.component.graphics.ColorAnimation;
import net.mostlyoriginal.game.component.ui.Label;

/**
 * Tween Entity animation between two colors.
 *
 * @author Daan van Yperen
 * @see net.mostlyoriginal.api.component.graphics.ColorAnimation
 */
@Wire
public class ColorAnimationSystem extends EntityProcessingSystem {

    ComponentMapper<Anim> am;
    ComponentMapper<Label> lm;
    ComponentMapper<ColorAnimation> cm;

    public ColorAnimationSystem() {
        super(Aspect.getAspectForAll( ColorAnimation.class).one(Anim.class,Label.class));
    }

    @Override
    protected void process(final Entity entity) {
        final ColorAnimation colorAnimation = cm.get(entity);

        // age colors individually.
        colorAnimation.age.r += colorAnimation.speed.r * world.delta;
        colorAnimation.age.g += colorAnimation.speed.g * world.delta;
        colorAnimation.age.b += colorAnimation.speed.b * world.delta;
        colorAnimation.age.a += colorAnimation.speed.a * world.delta;

        // tween colors individually.
        if ( am.has(entity)) {
            final Anim animation = am.get(entity);
            animation.color.r = colorAnimation.tween.apply(colorAnimation.minColor.r, colorAnimation.maxColor.r, 1 - Math.abs(colorAnimation.age.r % 2f - 1));
            animation.color.g = colorAnimation.tween.apply(colorAnimation.minColor.g, colorAnimation.maxColor.g, 1 - Math.abs(colorAnimation.age.g % 2f - 1));
            animation.color.b = colorAnimation.tween.apply(colorAnimation.minColor.b, colorAnimation.maxColor.b, 1 - Math.abs(colorAnimation.age.b % 2f - 1));
            animation.color.a = colorAnimation.tween.apply(colorAnimation.minColor.a, colorAnimation.maxColor.a, 1 - Math.abs(colorAnimation.age.a % 2f - 1));
        }

        if ( lm.has(entity)) {
            final Label lm = this.lm.get(entity);
            lm.color.r = colorAnimation.tween.apply(colorAnimation.minColor.r, colorAnimation.maxColor.r, 1 - Math.abs(colorAnimation.age.r % 2f - 1));
            lm.color.g = colorAnimation.tween.apply(colorAnimation.minColor.g, colorAnimation.maxColor.g, 1 - Math.abs(colorAnimation.age.g % 2f - 1));
            lm.color.b = colorAnimation.tween.apply(colorAnimation.minColor.b, colorAnimation.maxColor.b, 1 - Math.abs(colorAnimation.age.b % 2f - 1));
            lm.color.a = colorAnimation.tween.apply(colorAnimation.minColor.a, colorAnimation.maxColor.a, 1 - Math.abs(colorAnimation.age.a % 2f - 1));
        }

        if ( colorAnimation.duration != -1 )
        {
            colorAnimation.duration -= world.delta;
            if ( colorAnimation.duration <= 0 )
            {
                if ( am.has(entity)) {
                    final Anim animation = am.get(entity);
                    animation.color.set(1f, 1f, 1f, 1f);
                }
                entity.removeComponent(ColorAnimation.class).changedInWorld();
            }

        }
    }
}
