package net.mostlyoriginal.game.system.ship;

/**
 * @author Daan van Yperen
 */

import com.artemis.Aspect;
import com.artemis.ComponentMapper;
import com.artemis.Entity;
import com.artemis.EntitySystem;
import com.artemis.annotations.Wire;
import com.artemis.utils.EntityBuilder;
import com.artemis.utils.ImmutableBag;
import com.badlogic.gdx.math.MathUtils;
import net.mostlyoriginal.api.component.basic.Bounds;
import net.mostlyoriginal.api.component.basic.Pos;
import net.mostlyoriginal.api.component.graphics.Anim;
import net.mostlyoriginal.api.component.graphics.Color;
import net.mostlyoriginal.api.component.graphics.Renderable;
import net.mostlyoriginal.api.utils.GdxUtil;
import net.mostlyoriginal.api.utils.SafeEntityReference;
import net.mostlyoriginal.game.G;
import net.mostlyoriginal.game.NameRolodex;
import net.mostlyoriginal.game.component.ship.CrewMember;
import net.mostlyoriginal.game.component.ui.Button;
import net.mostlyoriginal.game.component.ui.ButtonListener;
import net.mostlyoriginal.game.component.ui.Clickable;
import net.mostlyoriginal.game.component.ui.Label;
import net.mostlyoriginal.game.manager.EntityFactorySystem;

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
public class CrewSystem extends EntitySystem {

    public static final int MARGIN_BOTTOM = 40;
    public static final int MARGIN_LEFT = 2;
    private ComponentMapper<CrewMember> mCrew;

    protected InventorySystem inventorySystem;
    protected LifesupportSimulationSystem lifesupportSimulationSystem;
    protected ComponentMapper<CrewMember> mCrewMember;
    protected ComponentMapper<Pos> mPos;
    protected ComponentMapper<Anim> mAnim;
    protected ComponentMapper<Label> mLabel;
    protected ComponentMapper<Color> mColor;
    protected EntityFactorySystem efs;

    protected float labelCooldown = 4;
    protected boolean showStatusLabel = false;

    protected int crewIndex = 0;

    public static final Color NEUTRAL = GdxUtil.asColor("2C4142");

    private final List<Entity> sortedEntities = new ArrayList<Entity>();
    public boolean sortedDirty = false;

    public Comparator<Entity> layerSortComperator = new Comparator<Entity>() {
        @Override
        public int compare(Entity e1, Entity e2) {
            return mCrew.get(e1).name.compareTo(mCrew.get(e2).name);
        }
    };
    private int ROW_HEIGHT = 9;

    public CrewSystem() {
        super(Aspect.getAspectForAll(CrewMember.class));
    }

    @Override
    protected void initialize() {
        super.initialize();

        createCrew("The Captain", "crew-0", CrewMember.Effect.HEALTHY);
    }

    @Override
    protected void processEntities(ImmutableBag<Entity> entities) {

        if (sortedDirty) {
            sortedDirty = false;
            Collections.sort(sortedEntities, layerSortComperator);
        }

        for (Entity entity : sortedEntities) {
            process(entity);
            crewIndex++;
        }
    }

    @Override
    protected void begin() {
        super.begin();
        crewIndex=0;

        // slowly toggle between labels.
        labelCooldown -= world.delta;
        if ( labelCooldown <= 0 )
        {
            labelCooldown = 2;
            showStatusLabel = !showStatusLabel;
        }
    }

    protected void process(Entity e) {
        final CrewMember crewMember = mCrewMember.get(e);

        populateTransients(crewMember, e);

        int offsetX = G.SCREEN_WIDTH - 8 - MARGIN_LEFT;
        int offsetY = MARGIN_BOTTOM + crewIndex * ROW_HEIGHT;

        if ( crewMember.icon.isActive() )
        {
            Entity e2 = crewMember.icon.get();
            if ( mPos.has(e2)) {
                Pos pos = mPos.get(e2);
                pos.x = offsetX;
                pos.y = offsetY;

                offsetX -= 8;
            }
            if ( mAnim.has(e2))
            {
                Anim anim = mAnim.get(e2);
                anim.id2=crewMember.effect.animStatusId;
            }
        }

        if ( crewMember.biogelButton.isActive() )
        {
            if ( inventorySystem.get(InventorySystem.Resource.BIOGEL) > 0 && crewMember.effect.can(CrewMember.Ability.BIOGELLABLE) ) {
                Entity e2 = crewMember.biogelButton.get();
                if (mPos.has(e2)) {
                    Pos pos = mPos.get(e2);
                    pos.x = offsetX;
                    pos.y = offsetY - 1;
                    offsetX -= 6;
                }
            }
        }

        // fonts are typically offset in the wrong dir.
        offsetY += 6;
        offsetX += 5;

        // move label to right location.
        if ( crewMember.labelName.isActive() )
        {
            // show name when applicable, or when healthy.
            updateLabel(offsetX, offsetY, crewMember.labelName.get(), !showStatusLabel || crewMember.effect == CrewMember.Effect.HEALTHY ?  crewMember.name : null, NEUTRAL);
        }

        // move label to right location.
        if ( crewMember.labelStatus.isActive() )
        {
            // show effect only when not healthy.
            updateLabel(offsetX, offsetY, crewMember.labelStatus.get(), showStatusLabel && crewMember.effect != CrewMember.Effect.HEALTHY  ? crewMember.effect.label : null, crewMember.effect.color);
        }

    }

    private void updateLabel(int offsetX, int offsetY, Entity e2, String label, Color color) {
        if ( mPos.has(e2)) {
            Pos pos = mPos.get(e2);
            pos.x = offsetX;
            pos.y = offsetY;
        }
        if ( mLabel.has(e2) )
        {
            Label l = mLabel.get(e2);
            l.text = label;
            mColor.get(e2).set(color);
        }
    }

    /** return random crewmember with ability, or null if none. */
    public Entity randomWith(CrewMember.Ability ability) {
        Entity[] entities = getAllWith(ability);
        return entities.length == 0 ? null : entities[MathUtils.random(0,entities.length-1)];
    }

    /** return all crewmembers with given agbility. */
    public Entity[] getAllWith(CrewMember.Ability ability) {

        final ArrayList<Entity> list = new ArrayList<>();

        for (Entity e : sortedEntities) {
            final CrewMember crewMember = mCrewMember.get(e);
            if ( crewMember != null )
            {
                if ( crewMember.effect.can( ability ) )
                {
                    list.add(e);
                }
            }
        }

        return list.toArray(new Entity[list.size()]);
    }

    public int countNotOf(CrewMember.Ability ability) {
        int count=0;
        for (Entity e : sortedEntities) {
            final CrewMember crewMember = mCrewMember.get(e);
            if ( crewMember != null )
            {
                if ( !crewMember.effect.can( ability ) )
                {
                    count++;
                }
            }
        }
        return count;
    }

    /** return count of crew with given ability. */
    public int countOf(CrewMember.Ability ability)
    {
        int count=0;
        for (Entity e : sortedEntities) {
            final CrewMember crewMember = mCrewMember.get(e);
            if ( crewMember != null )
            {
                if ( crewMember.effect.can( ability ) )
                {
                    count++;
                }
            }
        }
        return count;
    }

    private void populateTransients(CrewMember crewMember, Entity e) {
        if ( crewMember.icon == null ) {
            crewMember.icon = new SafeEntityReference(new EntityBuilder(world).with(new Pos(), new Renderable(), new Anim(crewMember.animId)).build());
        }

        if ( crewMember.biogelButton == null ) {
            Button btn = new Button("btn-heal", new HealButton(crewMember, e), "Heal the crewmember.");
            crewMember.biogelButton = new SafeEntityReference(new EntityBuilder(world).with(new Pos(), new Bounds(-2,-2,10, 8), new Clickable(), new Renderable(), new Anim(),
                    btn).build());
            btn.hideIfDisabled=true;

        }

        if ( crewMember.labelName == null ) {
            Label label = new Label(crewMember.name);
            label.align = Label.Align.RIGHT;
            crewMember.labelName = new SafeEntityReference(new EntityBuilder(world).with(new Pos(), label, new Renderable(),  new Color(NEUTRAL)).build());
        }
        if ( crewMember.labelStatus == null ) {
            Label label = new Label(crewMember.effect.label);
            label.align = Label.Align.RIGHT;
            crewMember.labelStatus = new SafeEntityReference(new EntityBuilder(world).with(new Pos(), label, new Renderable(),  new Color(NEUTRAL)).build());
        }
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
        removeTransientProperties(e);
        sortedEntities.remove(e);
    }

    /** cleanup label and icon for crewmember. */
    private void removeTransientProperties(Entity e) {
        final CrewMember crewMember = mCrewMember.get(e);
        if ( crewMember != null ) {
            if (crewMember.icon != null && !crewMember.icon.isActive()) {
                crewMember.icon.get().deleteFromWorld();
            }
            if (crewMember.labelName != null && !crewMember.labelName.isActive()) {
                crewMember.labelName.get().deleteFromWorld();
            }
            if (crewMember.labelStatus != null && !crewMember.labelStatus.isActive()) {
                crewMember.labelStatus.get().deleteFromWorld();
            }
            if (crewMember.biogelButton != null && !crewMember.biogelButton.isActive()) {
                crewMember.biogelButton.get().deleteFromWorld();
            }
        }
    }

    public void createRandomCrewmember()
    {
        createCrew(getRandomName(), "crew-" + MathUtils.random(0,2), CrewMember.Effect.HEALTHY);
    }

    public void createCrew(String name, String animId, CrewMember.Effect effect) {
        new EntityBuilder(world).with(new CrewMember(name, animId, effect)).build();
    }

    public String getRandomName() {
        return NameRolodex.randomName();
    }

    public CrewMember randomWithAsCrew(CrewMember.Ability ability) {
        Entity entity = randomWith(ability);
        return entity != null ? mCrewMember.get(entity) : null;
    }

    private class HealButton extends ButtonListener {
        private final CrewMember crewMember;
        public Entity e;

        public HealButton(CrewMember crewMember, Entity e) {
            this.crewMember = crewMember;
            this.e=e;
        }

        @Override
        public void run() {
            super.run();
            lifesupportSimulationSystem.changeState(e, CrewMember.Effect.HEALTHY);
            inventorySystem.alter(InventorySystem.Resource.BIOGEL,-1);
        }

        @Override
        public boolean enabled() {
            return inventorySystem.get(InventorySystem.Resource.BIOGEL) > 0 && crewMember.effect.can(CrewMember.Ability.BIOGELLABLE);
        }
    }
}
