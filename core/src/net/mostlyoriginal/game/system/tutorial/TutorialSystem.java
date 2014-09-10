package net.mostlyoriginal.game.system.tutorial;

import com.artemis.Aspect;
import com.artemis.ComponentMapper;
import com.artemis.Entity;
import com.artemis.annotations.Wire;
import com.artemis.systems.EntityProcessingSystem;
import com.artemis.utils.EntityBuilder;
import net.mostlyoriginal.api.component.basic.Pos;
import net.mostlyoriginal.api.component.graphics.Anim;
import net.mostlyoriginal.api.component.graphics.Renderable;
import net.mostlyoriginal.game.component.ship.ShipComponent;
import net.mostlyoriginal.game.component.tutorial.TutorialStep;
import net.mostlyoriginal.game.component.ui.Button;
import net.mostlyoriginal.game.system.ship.ProductionSimulationSystem;
import net.mostlyoriginal.game.system.ui.ButtonSystem;
import net.mostlyoriginal.game.system.ui.ConstructionSystem;
import net.mostlyoriginal.game.system.ui.DilemmaSystem;

/**
 * @author Daan van Yperen
 */
@Wire
public class TutorialSystem extends EntityProcessingSystem {

    protected ComponentMapper<Pos> mPos;
    protected ComponentMapper<Anim> mAnim;
    protected ComponentMapper<Button> mButton;

    int step = 0;
    private Step currentStep = null;
    public Entity arrow;
    private ConstructionSystem constructionSystem;
    private ButtonSystem buttonSystem;

    public float payoutDelay = 0;
    public Step payoutStepNr = null;
    private ProductionSimulationSystem productionSimulationSystem;
    private DilemmaSystem dilemmaSystem;

    public int requiredRepeats = 0;

    public TutorialSystem() {
        super(Aspect.getAspectForAll(TutorialStep.class));
    }

    @Override
    protected void initialize() {
        super.initialize();
        arrow = new EntityBuilder(world).with(new Pos(), new Anim("arrow"), new Renderable(9000)).build();
        arrow.disable();
    }

    public void activateNextStep() {
        if (currentStep == null) {
            currentStep = Step.SELECT_ENGINE;
            delayedInitStep(currentStep);
        } else {
            final int nextStep = currentStep.ordinal() + 1;
            if (nextStep < Step.values().length) {
                arrow.disable();
                payoutDelay = 0.5f;
                payoutStepNr = currentStep;
                currentStep = Step.values()[nextStep];
                initStep(currentStep);
            }
        }
    }

    private void enableAllConstructButtons() {
        for (Entity entity : constructionSystem.constructionButton.values()) {
            setConstructionButton(entity, true);
        }
    }

    private void enableConstructionButton(Entity entity) {
        setConstructionButton(entity, true);
    }

    private void setConstructionButton(Entity entity, boolean enable) {
        final Button button = mButton.get(entity);
        button.manualDisable = !enable;

    }

    private void disableAllConstructButtons() {
        for (Entity entity : constructionSystem.constructionButton.values()) {
            setConstructionButton(entity, false);
        }
    }

    private void updateHint(Step currentStep) {

        String text = null;
        if ( currentStep == null ) return;
        switch (currentStep) {
            case SELECT_ENGINE:
                text = "Select the engine.";
                break;
            case PLACE_ENGINE:
                text = "Place engine by clicking any of the red indicators.";
                break;
            case SELECT_STORAGEPOD:
                text = "Storage pods hold fuel, food and biogel.";
                break;
            case PLACE_STORAGEPOD:
                text = "Place "+requiredRepeats+" more storage pod(s).";
                break;
            case FINISHED:
                break;
        }

        if ( text != null ) {
            buttonSystem.hintlabel.text = text;
        }
    }

    private void payoutStep(Step step) {
        delayedInitStep(currentStep);
        switch (step) {

            case SELECT_ENGINE:
                break;
            case PLACE_ENGINE:
                productionSimulationSystem.finishAllConstruction();
                break;
            case SELECT_STORAGEPOD:
                break;
            case PLACE_STORAGEPOD:
                productionSimulationSystem.finishAllConstruction();
                break;
            case FINISHED:
                break;
        }
    }

    /** Called immediately after a step becomes active */
    private void initStep(Step step) {
        requiredRepeats = 1;
        switch (step) {
            case PLACE_ENGINE:
                disableAllConstructButtons();
                break;
            case PLACE_STORAGEPOD:
                requiredRepeats = 3;
                break;
        }
    }

    /** called with a short delay after step becomes active. */
    private void delayedInitStep(Step step) {
        switch (step) {
            case SELECT_ENGINE:
                disableAllConstructButtons();
                highlightConstructButton(ShipComponent.Type.ENGINE);
                break;
            case PLACE_ENGINE:
                break;
            case SELECT_STORAGEPOD:
                highlightConstructButton(ShipComponent.Type.STORAGEPOD);
                break;
            case PLACE_STORAGEPOD:
                highlightConstructButton(ShipComponent.Type.STORAGEPOD);
                break;
            case FINISHED:
                enableAllConstructButtons();
                dilemmaSystem.afterTutorialDilemma();
                break;
        }
    }

    private void highlightConstructButton(ShipComponent.Type type) {

        if ( arrow == null ) return;

        final Entity button = constructionSystem.constructionButton.get(type);
        final Pos pos = mPos.get(button);

        final Pos arrowPos = mPos.get(arrow);
        arrowPos.x = pos.x + 4;
        arrowPos.y = pos.y + 16;

        enableConstructionButton(button);
        arrow.enable();
    }

    public void complete(Step step) {
        if (step == currentStep) {
            requiredRepeats--;
            if (requiredRepeats <= 0) {
                activateNextStep();
            }
        }
    }

    @Override
    protected void begin() {
        updateHint(currentStep);

        if (payoutDelay > 0) {
            payoutDelay -= world.delta;
            if (payoutDelay <= 0) {
                payoutStep(payoutStepNr);
            }
        }
    }

    @Override
    protected void process(Entity e) {
    }

    public static enum Step {
        SELECT_ENGINE,
        PLACE_ENGINE,
        SELECT_STORAGEPOD,
        PLACE_STORAGEPOD,
        FINISHED;
    }
}
