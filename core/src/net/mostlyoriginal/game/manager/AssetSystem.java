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
        add("cursor", 1, 1 , 1, 1, 1);

        add("progress-bubble-0", 56, 112, 8, 8, 1);
        add("progress-bubble-1", 56, 120, 8, 8, 1);
        add("progress-bar-0", 64, 112, 8, 8, 1);
        add("progress-bar-1", 64, 120, 8, 8, 1);
        add("progress-indicator", 56, 96, 8, 11, 1);

        add("gate", 32, 56, 24, 56, 1);

        add("state-dead", 120, 64, 8, 8, 1);
        add("state-slug", 104, 72, 8, 8, 1);

        add("btn-engage-up", 171, 85, 56, 20, 1);
        add("btn-engage-hover", 171, 84, 56, 20, 1);
        add("btn-engage-down", 171, 81, 56, 20, 1);

        add("bar-fuel", 48,177, 8, 6, 1);
        add("bar-food", 48,185, 8, 6, 1);
        add("bar-biogel", 48,169, 8, 6, 1);
        //add("bar-crew", 48,160, 8, 8, 1);

        add("bar-fuel-open", 48+8,177, 8, 6, 1);
        add("bar-food-open", 48+8,185, 8, 6, 1);
        add("bar-biogel-open", 48+8,169, 8, 6, 1);
        //add("bar-crew-open", 48+8,160, 8, 8, 1);

        add("pickup-fuel", 48,177, 8, 6, 1);
        add("pickup-food", 48,185, 8, 6, 1);
        add("pickup-biogel", 48,169, 8, 6, 1);
        add("pickup-crew", 48,160, 8, 8, 1);

        add("crew-0", 104,64, 8, 8, 1);
        add("crew-1", 104 - 8,64, 8, 8, 1);
        add("crew-2", 104 - 8,64 + 8, 8, 8, 1);

        add("btn-construct-up", 173,109, 17, 17, 1);
        add("btn-construct-hover", 196, 108, 17, 17, 1);
        add("btn-construct-down", 171,107, 17, 17, 1);

        add("btn-scan-up",    165, 181, 31, 17, 1);
        add("btn-scan-hover", 165, 180, 31, 17, 1);
        add("btn-scan-down",  165, 178, 31, 17, 1);

        add("btn-heal-up",     81, 169, 6, 8, 1);
        add("btn-heal-hover",  81, 168, 6, 8, 1);
        add("btn-heal-down",   81, 167, 6, 8, 1);

        add("c-indicator",112,72, 8, 8, 1);

        add("c-bunks",152,24, 8, 8, 1);
        add("c-medbay",144,16, 8, 8, 1);
        add("c-hydroponics",136,40, 8, 8, 1);
        add("c-storagepod",144,32, 8, 8, 1);
        add("c-engine",80,24, 8, 8, 1);
        add("c-ramscoop",112+8,56, 8, 8, 1);
        add("c-chain",64,40, 8, 8, 1);

        add("c-bunks-placed",152,24, 8, 8, 1);
        add("c-medbay-placed",144,16, 8, 8, 1);
        add("c-hydroponics-placed",136,40, 8, 8, 1);
        add("c-storagepod-placed",144,32, 8, 8, 1);
        add("c-engine-placed",80,24, 16, 8, 1);
        add("c-ramscoop-placed",112,56, 16, 8, 1);
        add("c-chain-placed",64,40, 8, 8, 1);

        add("c-bunks-building",184,24, 8, 8, 1);
        add("c-medbay-building",176,16, 8, 8, 1);
        add("c-hydroponics-building",168,40, 8, 8, 1);
        add("c-storagepod-building",176,32, 8, 8, 1);
        add("c-engine-building",176,40, 16, 8, 1);
        add("c-ramscoop-building",176,48, 16, 8, 1);
        add("c-chain-building",64,40, 8, 8, 1);

        add("hull-0",88, 32, 8, 8, 1); // top left
        add("hull-1",96, 32, 8, 8, 1); // top
        add("hull-2",120,32, 8, 8, 1); // top right

        add("hull-3",88, 40, 8, 8, 1); // left
        add("hull-4",120, 40, 8, 8, 1); // right

        add("hull-5",88, 48, 8, 8, 1); // bottom left
        add("hull-6",96, 48, 8, 8, 1); // bottom
        add("hull-7",120,48, 8, 8, 1); // bottom right

        add("hull-inny-0", 112,16, 8, 8, 1); // bottom-right-inny.
        add("hull-inny-1", 120,16, 8, 8, 1); // bottom-left-inny
        add("hull-inny-2", 112,24, 8, 8, 1); // top-right-inny
        add("hull-inny-3", 120,24, 8, 8, 1); // top-left-inny

        add("hull-missing",96,40, 8, 8, 1); // missing

        add("star-0-0",32,136, 4, 4, 1);
        add("star-0-1",32-4,136, 4, 4, 1);
        add("star-0-2",32-8,136, 4, 4, 1);
        add("star-0-3",40,136, 7, 4, 1);
        add("star-0-4",48,136,26, 4, 1);
        add("star-0-5",80,136,36, 4, 1);

        add("star-1-0",32,144, 2, 2, 1);
        add("star-1-1",32-4,144, 2, 2, 1);
        add("star-1-2",32-8,144, 2, 2, 1);
        add("star-1-3",40,144, 6, 2, 1);
        add("star-1-4",48,144,12, 2, 1);
        add("star-1-5",80,144,21, 2, 1);

        add("star-2-0",32,152, 1, 1, 1);
        add("star-2-1",32-4,152, 1, 1, 1);
        add("star-2-2",32-8,152, 1, 1, 1);
        add("star-2-3",40,152, 4, 1, 1);
        add("star-2-4",48,152, 7, 1, 1);
        add("star-2-5",80,152,15, 1, 1);

        add("engine-0",64,16,16, 8, 1);
        add("engine-1",64,16+8,16, 8, 1);
        add("engine-2",64,16+16,16, 8, 1);

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
