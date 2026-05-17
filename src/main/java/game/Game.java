package game;

import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundImage;
import javafx.scene.layout.BackgroundPosition;
import javafx.scene.layout.BackgroundRepeat;
import javafx.scene.layout.BackgroundSize;
import javafx.scene.layout.StackPane;
import util.ImageManager;

public class Game {

    private final SceneSet sceneSet;

    public Game(SceneSet sceneSet) {
        this.sceneSet = sceneSet;
        initRoot();
    }

    private void initRoot() {
        StackPane root = sceneSet.getRoot();
        root.setBackground(new Background(new BackgroundImage(
                ImageManager.load("assets/uiImages/backgrounds/bg.jpg"),
                BackgroundRepeat.NO_REPEAT,
                BackgroundRepeat.NO_REPEAT,
                BackgroundPosition.CENTER,
                BackgroundSize.DEFAULT
        )));
    }

    public SceneSet getSceneSet() {
        return sceneSet;
    }
}
