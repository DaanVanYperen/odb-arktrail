package net.mostlyoriginal.game.manager;

import com.artemis.ComponentMapper;
import com.artemis.Entity;
import com.artemis.annotations.Wire;
import com.artemis.managers.TagManager;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.math.MathUtils;
import net.mostlyoriginal.api.component.basic.Pos;
import net.mostlyoriginal.api.utils.EntityUtil;

/**
 * @todo Split game logic and library logic.
 * @author Daan van Yperen
 */
@Wire
public class AssetSystem extends net.mostlyoriginal.api.manager.AbstractAssetSystem {

    private TagManager tagManager;

    ComponentMapper<Pos> pm;

    public AssetSystem() {
        super();

        // @todo GAME SPECIFIC, split into library and game specific logic.

        add("progress-bubble-0", 56, 112, 8, 8, 1);
        add("progress-bubble-1", 56, 120, 8, 8, 1);
        add("progress-bar-0", 64, 112, 8, 8, 1);
        add("progress-bar-1", 64, 120, 8, 8, 1);
        add("progress-indicator", 56, 96, 8, 11, 1);

        add("gate", 32, 56, 24, 56, 1);

        add("btn-engage-up", 171, 85, 56, 20, 1);
        add("btn-engage-hover", 171, 84, 56, 20, 1);
        add("btn-engage-down", 171, 81, 56, 20, 1);


        loadSounds(new String[] {
        });
    }


    public void playSfx(String name, Entity origin) {
        if (sfxVolume > 0 )
        {
            Entity player = tagManager.getEntity("player");
            float distance = EntityUtil.distance(origin, player);

            float volume = sfxVolume - (distance / 2000f);
            if ( volume > 0.01f )
            {
                float balanceX = pm.has(origin) && pm.has(player) ? MathUtils.clamp((pm.get(origin).x - pm.get(player).x) / 100f, -1f, 1f) : 0;
                Sound sfx = getSfx(name);
                sfx.stop();
                sfx.play(volume, MathUtils.random(1f, 1.04f), balanceX);
            }
        }
    }

}
