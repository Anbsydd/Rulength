package game;

import javafx.scene.Scene;
import javafx.scene.layout.StackPane;

public class SceneSet {

    private final StackPane root;
    private final Scene scene;

    public SceneSet() {
        this.root = new StackPane();
        this.scene = new Scene(root);
    }

    public StackPane getRoot() {
        return root;
    }

    public Scene getScene() {
        return scene;
    }
}
