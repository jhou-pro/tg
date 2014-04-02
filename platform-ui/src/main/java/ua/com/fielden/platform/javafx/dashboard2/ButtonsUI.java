package ua.com.fielden.platform.javafx.dashboard2;

import javafx.application.Platform;
import javafx.embed.swing.JFXPanel;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.paint.Color;

public class ButtonsUI extends JFXPanel {
    private static final long serialVersionUID = 2381155520490835378L;
    private Group buttonsNode;
    private Scene scene;
    private final Runnable configureAction, refreshAction;

    public ButtonsUI(final Runnable refreshAction) {
        this.configureAction = new Runnable() {
            public void run() {
            }
        };
        this.refreshAction = refreshAction;
        Platform.setImplicitExit(false);

        initSceneIfNotInitialised();
    }

    public void initSceneIfNotInitialised() {
        if (getScene() == null) {
            Platform.runLater(new Runnable() {
                @Override
                public void run() {
                    // This method is invoked on the JavaFX thread
                    final Scene scene = createScene();
                    setScene(scene);
                }

            });
        }
    }

    private Scene createScene() {
        scene = new Scene(buttonsNode = createButtonsNode());
        scene.setFill(Color.rgb(214, 217, 223));
        return scene;
    }

    private Group createButtonsNode() {
        final Group settingsGroup = AbstractDashboardUi.createSettingsGroup(configureAction);
        final double width = 60, mainPosY = 0;
        settingsGroup.setTranslateX(width - 40);
        settingsGroup.setTranslateY(mainPosY + 15);

        final Group refreshGroup = AbstractDashboardUi.createRefreshGroup(refreshAction);
        refreshGroup.setTranslateX(width - 15);
        refreshGroup.setTranslateY(mainPosY + 15);

        final Group dashboardNode = new Group(settingsGroup, refreshGroup);
        return dashboardNode;
    }
}
