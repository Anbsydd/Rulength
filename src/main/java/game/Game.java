package game;

import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundImage;
import javafx.scene.layout.BackgroundPosition;
import javafx.scene.layout.BackgroundRepeat;
import javafx.scene.layout.BackgroundSize;
import javafx.scene.layout.StackPane;
import util.ImageManager;

public class Game {

    private final Stage stage;

    public Game(Stage stage) {
        this.stage = stage;
        initRoot();
    }

    private void initRoot() {
        StackPane root = stage.getRoot();
        root.setBackground(new Background(new BackgroundImage(
                ImageManager.load("uiImages/backgrounds/bg.jpg"),
                BackgroundRepeat.NO_REPEAT,
                BackgroundRepeat.NO_REPEAT,
                BackgroundPosition.CENTER,
                new BackgroundSize(1, 1, true, true, false, true)
        )));
    }

    public Stage getStage() {
        return stage;
    }
}
