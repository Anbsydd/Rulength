package game;

import config.ConfigLoader;
import config.StageConfig;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.CornerRadii;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;

public class Stage {

    private static final String CONFIG_PATH = "assets/stageConfig.json";

    private final javafx.stage.Stage javafxStage;
    private final StackPane root;
    private final Scene scene;

    public Stage(javafx.stage.Stage javafxStage) throws Exception {
        this.javafxStage = javafxStage;
        this.root = new StackPane();
        this.scene = new Scene(root);

        StageConfig config = ConfigLoader.loadConfig(CONFIG_PATH, StageConfig.class);

        javafxStage.setWidth(config.width);
        javafxStage.setHeight(config.height);
        javafxStage.setScene(scene);
        javafxStage.setTitle(config.title);
        javafxStage.setFullScreenExitHint(config.fullScreenExitHint);
        root.setBackground(new Background(new BackgroundFill(Color.BLACK, CornerRadii.EMPTY, Insets.EMPTY)));
    }

    public javafx.stage.Stage getJavafxStage() {
        return javafxStage;
    }

    public StackPane getRoot() {
        return root;
    }

    public Scene getScene() {
        return scene;
    }

    public void show() {
        javafxStage.show();
    }
}
