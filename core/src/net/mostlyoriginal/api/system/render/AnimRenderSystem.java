package net.mostlyoriginal.api.system.render;

/**
 * @author Daan van Yperen
 */

import com.artemis.Aspect;
import com.artemis.ComponentMapper;
import com.artemis.Entity;
import com.artemis.EntitySystem;
import com.artemis.annotations.Wire;
import com.artemis.utils.ImmutableBag;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import net.mostlyoriginal.api.component.basic.Angle;
import net.mostlyoriginal.api.component.basic.Pos;
import net.mostlyoriginal.api.component.graphics.Anim;
import net.mostlyoriginal.api.manager.AbstractAssetSystem;
import net.mostlyoriginal.api.system.camera.CameraSystem;
import net.mostlyoriginal.game.MainScreen;
import net.mostlyoriginal.game.manager.EntityFactorySystem;
import net.mostlyoriginal.game.system.render.BarRenderSystem;
import net.mostlyoriginal.game.system.render.LabelRenderSystem;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * Render and progress animations.
 *
 * @author Daan van Yperen
 * @see net.mostlyoriginal.api.component.graphics.Anim
 */
@Wire
public class AnimRenderSystem extends EntitySystem {

    private ComponentMapper<Pos> pm;
    private ComponentMapper<Anim> sm;
    private ComponentMapper<Angle> rm;
    private CameraSystem cameraSystem;
    private AbstractAssetSystem abstractAssetSystem;

    private LabelRenderSystem labelRenderSystem;

    private SpriteBatch batch;
    private final List<Entity> sortedEntities = new ArrayList<Entity>();
    public boolean sortedDirty = false;

    public Comparator<Entity> layerSortComperator = new Comparator<Entity>() {
        @Override
        public int compare(Entity e1, Entity e2) {
            return sm.get(e1).layer - sm.get(e2).layer;
        }
    };

    private float age;
    private boolean labelsRendered;
    private BarRenderSystem barRenderSystem;

    public AnimRenderSystem() {
        super(Aspect.getAspectForAll(Pos.class, Anim.class));
        batch  = new SpriteBatch(2000);
    }

    @Override
    protected void begin() {

        labelsRendered = false;

        age += world.delta;

        batch.setProjectionMatrix(cameraSystem.camera.combined);
        batch.begin();
        batch.setColor(1f, 1f, 1f, 1f);
    }

    @Override
    protected void end() {
        batch.end();
        if ( !labelsRendered ) {
            renderLabels();
        }
    }

    @Override
    protected void processEntities(ImmutableBag<Entity> entities) {

        if (sortedDirty) {
            sortedDirty = false;
            Collections.sort(sortedEntities, layerSortComperator);
        }

        for (Entity entity : sortedEntities) {
            process(entity);
        }
    }

    protected void process(final Entity entity) {

        final Anim anim = sm.get(entity);
        final Pos pos = pm.get(entity);
        final Angle angle = rm.has(entity) ? rm.get(entity) : Angle.NONE;

        anim.age += world.delta * anim.speed;

        // manually render labels before mouse cursor.
        if ( !labelsRendered && anim.layer >= EntityFactorySystem.MOUSE_CURSOR_LAYER )
        {
            batch.end();
            renderLabels();
            batch.begin();
        }

        batch.setColor( anim.color );
        if ( anim.id != null ) drawAnimation(anim, angle, pos, anim.id);
        if ( anim.id2 != null ) drawAnimation(anim, angle, pos, anim.id2);
    }

    private void renderLabels() {
        if ( !labelsRendered )
        {
            labelsRendered=true;
            labelRenderSystem.process();
            barRenderSystem.process();
        }
    }

    private void drawAnimation(final Anim animation, final Angle angle, final Pos position, String id) {

        // don't support backwards yet.
        if ( animation.age < 0 ) return;

        final com.badlogic.gdx.graphics.g2d.Animation gdxanim = abstractAssetSystem.get(id);
        if ( gdxanim == null) return;

        final TextureRegion frame = gdxanim.getKeyFrame(animation.age, animation.loop);

        if ( animation.flippedX)
        {
            // mirror
            batch.draw(frame.getTexture(),
                    roundToPixels(position.x),
                    roundToPixels(position.y),
                    angle.ox == Angle.ORIGIN_AUTO ? frame.getRegionWidth() * animation.scale * 0.5f : angle.ox,
                    angle.oy == Angle.ORIGIN_AUTO ? frame.getRegionHeight() * animation.scale * 0.5f : angle.oy,
                    frame.getRegionWidth() * animation.scale,
                    frame.getRegionHeight() * animation.scale,
                    1f,
                    1f,
                    angle.rotation,
                    frame.getRegionX(),
                    frame.getRegionY(),
                    frame.getRegionWidth(),
                    frame.getRegionHeight(),
                    true,
                    false);

        } else if ( angle.rotation != 0 )
        {
            batch.draw(frame,
                    roundToPixels(position.x),
                    roundToPixels(position.y),
                    angle.ox == Angle.ORIGIN_AUTO ? frame.getRegionWidth() * animation.scale * 0.5f : angle.ox,
                    angle.oy == Angle.ORIGIN_AUTO ? frame.getRegionHeight() * animation.scale * 0.5f : angle.oy,
                    frame.getRegionWidth() * animation.scale,
                    frame.getRegionHeight() * animation.scale, 1, 1,
                    angle.rotation);
        } else {
            batch.draw(frame,
                    roundToPixels(position.x),
                    roundToPixels(position.y),
                    frame.getRegionWidth() * animation.scale,
                    frame.getRegionHeight() * animation.scale);
        }
    }

    private float roundToPixels(final float val) {
        // since we use camera zoom rounding to integers doesn't work properly.
        return ((int)(val * MainScreen.CAMERA_ZOOM_FACTOR)) / (float)MainScreen.CAMERA_ZOOM_FACTOR;
    }

    @Override
    protected boolean checkProcessing() {
        return true;
    }

    @Override
    protected void inserted(Entity e) {
        sortedEntities.add(e);
        sortedDirty = true;
    }

    @Override
    protected void removed(Entity e) {
        sortedEntities.remove(e);
    }
}
