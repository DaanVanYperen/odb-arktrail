package net.mostlyoriginal.game.system.render;

/**
 * @author Daan van Yperen
 */

import com.artemis.Aspect;
import com.artemis.ComponentMapper;
import com.artemis.Entity;
import com.artemis.EntitySystem;
import com.artemis.annotations.Wire;
import com.artemis.utils.ImmutableBag;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import net.mostlyoriginal.api.component.basic.Pos;
import net.mostlyoriginal.api.system.camera.CameraSystem;
import net.mostlyoriginal.game.component.ui.Label;
import net.mostlyoriginal.game.manager.FontManager;

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
public class LabelRenderSystem extends EntitySystem {

    private ComponentMapper<Pos> pm;
    private ComponentMapper<Label> mLabel;
    private CameraSystem cameraSystem;

    private SpriteBatch batch;
    private final List<Entity> sortedEntities = new ArrayList<Entity>();
    public boolean sortedDirty = false;

    public Comparator<Entity> layerSortComperator = new Comparator<Entity>() {
        @Override
        public int compare(Entity e1, Entity e2) {
            return mLabel.get(e1).layer - mLabel.get(e2).layer;
        }
    };

    private float age;
    private FontManager fontManager;

    public LabelRenderSystem() {
        super(Aspect.getAspectForAll(Pos.class, Label.class));
        batch = new SpriteBatch(1000);
    }

    @Override
    protected void begin() {

        age += world.delta;

        batch.setProjectionMatrix(cameraSystem.camera.combined);
        batch.begin();
        batch.setColor(1f, 1f, 1f, 1f);
    }

    @Override
    protected void end() {
        batch.end();
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

    @Override
    protected boolean checkProcessing() {
        return true;
    }

    protected void process(final Entity entity) {

        final Label label = mLabel.get(entity);
        final Pos pos = pm.get(entity);

        if (label.text != null) {
            batch.setColor(label.color);

            final BitmapFont font = fontManager.font;
            font.setColor(label.color);
            switch ( label.align) {
                case LEFT:
                    font.draw(batch, label.text, pos.x, pos.y);
                    break;
                case RIGHT:
                    font.draw(batch, label.text, pos.x - font.getBounds(label.text).width, pos.y);
                    break;
            }
        }
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
