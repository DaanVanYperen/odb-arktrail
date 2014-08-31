package net.mostlyoriginal.game.system.render;

import com.artemis.Aspect;
import com.artemis.ComponentMapper;
import com.artemis.Entity;
import com.artemis.annotations.Wire;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import net.mostlyoriginal.api.component.basic.Pos;
import net.mostlyoriginal.api.component.graphics.Color;
import net.mostlyoriginal.api.manager.AbstractAssetSystem;
import net.mostlyoriginal.api.system.camera.CameraSystem;
import net.mostlyoriginal.api.system.delegate.DeferredEntityProcessingSystem;
import net.mostlyoriginal.api.system.delegate.EntityProcessPrincipal;
import net.mostlyoriginal.game.component.ui.Bar;
import net.mostlyoriginal.game.manager.FontManager;

/**
 * Render and progress animations.
 *
 * @author Daan van Yperen
 * @see net.mostlyoriginal.api.component.graphics.Anim
 */
@Wire
public class BarRenderSystem extends DeferredEntityProcessingSystem {

    private ComponentMapper<Pos> pm;
    protected ComponentMapper<Color> mColor;
    private ComponentMapper<Bar> mBar;

    private AbstractAssetSystem abstractAssetSystem;
    private CameraSystem cameraSystem;
    private FontManager fontManager;

    private SpriteBatch batch;

    public BarRenderSystem(EntityProcessPrincipal principal) {
        super(Aspect.getAspectForAll(Pos.class, Bar.class), principal);
        batch  = new SpriteBatch(1000);
    }

    @Override
    protected void begin() {

        batch.setProjectionMatrix(cameraSystem.camera.combined);
        batch.begin();
        batch.setColor(1f, 1f, 1f, 1f);
    }

    @Override
    protected void end() {
        batch.end();
    }

    @Override
    protected boolean checkProcessing() {
        return true;
    }

    /** Pixel perfect aligning. */
    private float roundToPixels(final float val) {
        // since we use camera zoom rounding to integers doesn't work properly.
        return ((int)(val * cameraSystem.zoom)) / (float)cameraSystem.zoom;
    }

    protected void process(final Entity entity) {

        final Bar bar = mBar.get(entity);
        final Pos pos = pm.get(entity);

        final BitmapFont font = fontManager.font;
        if ( mColor.has(entity) ) {
            final Color color = mColor.get(entity);
            font.setColor(color.r, color.g, color.b, color.a);
            batch.setColor(color.r, color.g, color.b, color.a);
        } else {
            font.setColor(1f,1f,1f,1f);
            batch.setColor(1f,1f,1f,1f);
        }

        font.draw(batch, bar.text, roundToPixels(pos.x), roundToPixels(pos.y));

        BitmapFont.TextBounds bounds = font.getBounds(bar.text);

        final com.badlogic.gdx.graphics.g2d.Animation gdxanim = abstractAssetSystem.get(bar.animationId);
        if ( gdxanim == null) return;
        final com.badlogic.gdx.graphics.g2d.Animation gdxanim2 = abstractAssetSystem.get(bar.animationIdEmpty);
        if ( gdxanim2 == null) return;

        final TextureRegion frame = gdxanim.getKeyFrame(0,false);
        final TextureRegion frame2 = gdxanim2.getKeyFrame(0,false);

        // make sure one bubble is always shown.
        int emptyCount = ( bar.value == 0 && bar.valueEmpty == 0 ) ? 1 : bar.valueEmpty;

        int barWidth = frame.getRegionWidth() + 1;

        if ( bar.value + bar.valueEmpty >= 10 ) barWidth -=1;
        if ( bar.value + bar.valueEmpty >= 20 ) barWidth -=1;
        if ( bar.value + bar.valueEmpty >= 30 ) barWidth -=1;
        if ( bar.value + bar.valueEmpty >= 40 ) barWidth -=1;

        for ( int i =0; i< bar.value; i++)
        {
            batch.draw(frame,
                    roundToPixels(pos.x + bounds.width + i * barWidth),
                    roundToPixels(pos.y - bounds.height),
                    frame.getRegionWidth(),
                    frame.getRegionHeight());
        }
        for ( int i =0; i< emptyCount; i++)
        {
            batch.draw(frame2,
                    roundToPixels(pos.x + bounds.width + (i+bar.value) * barWidth),
                    roundToPixels(pos.y - bounds.height),
                    frame.getRegionWidth(),
                    frame.getRegionHeight());
        }
    }

}
