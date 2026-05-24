package game;

import config.ConfigLoader;
import config.StageConfig;
import javafx.scene.Scene;
import javafx.scene.layout.StackPane;

public class Stage {

    private static final String CONFIG_PATH = "assets/stageConfig.json";

    private final javafx.stage.Stage javafxStage;
    private final StackPane root;
    private final Scene scene;
    private final Game game;

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
        javafxStage.show();
        this.game = new Game(this);
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

    public Game getGame() {
        return game;
    }

    public void show() {
        javafxStage.show();
    }
}
