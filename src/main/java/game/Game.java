package game;

import javafx.geometry.Insets;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.CornerRadii;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;

public class Game {

    private final SceneSet sceneSet;

    public Game(SceneSet sceneSet) {
        this.sceneSet = sceneSet;
        initRoot();
    }

    private void initRoot() {
        StackPane root = sceneSet.getRoot();
        root.setBackground(new Background(new BackgroundFill(Color.BLACK, CornerRadii.EMPTY, Insets.EMPTY)));
    }

    public SceneSet getSceneSet() {
        return sceneSet;
    }
}
