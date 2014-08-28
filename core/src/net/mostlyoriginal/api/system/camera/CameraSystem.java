package net.mostlyoriginal.api.system.camera;

import com.artemis.systems.VoidEntitySystem;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.OrthographicCamera;

/**
 * Setup and manages basic orthographic camera.
 *
 * @author Daan van Yperen
 */
public class CameraSystem extends VoidEntitySystem {

    public final OrthographicCamera camera;
    public final OrthographicCamera guiCamera;

    /**
     * @param zoom How much
     */
    public CameraSystem( float zoom, float uiZoom ) {

        float zoomFactorInverter = 1f/zoom;

        camera = new OrthographicCamera(Gdx.graphics.getWidth() * zoomFactorInverter, Gdx.graphics.getHeight() * zoomFactorInverter);
        camera.setToOrtho(false, Gdx.graphics.getWidth() * zoomFactorInverter, Gdx.graphics.getHeight() * zoomFactorInverter);
        camera.update();

        float zoomFactorInverterUi = 1f/uiZoom;

        guiCamera = new OrthographicCamera(Gdx.graphics.getWidth() * zoomFactorInverterUi, Gdx.graphics.getHeight() * zoomFactorInverterUi);
        guiCamera.setToOrtho(false, Gdx.graphics.getWidth() * zoomFactorInverterUi, Gdx.graphics.getHeight() * zoomFactorInverterUi);
        guiCamera.update();
    }

    @Override
    protected void processSystem() {

    }
}
