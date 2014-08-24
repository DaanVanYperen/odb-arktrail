package net.mostlyoriginal.game.component.ship;

import com.artemis.Component;

/**
 * @author Daan van Yperen
 */
public class EngineFlame extends Component {
    public float timer;
    public int gridX;
    public int gridY;

    public EngineFlame(int gridX, int gridY) {
        this.gridX = gridX;


        this.gridY = gridY;
    }
}
