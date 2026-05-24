package game;

import javafx.scene.image.ImageView;
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

        ImageView bgView = new ImageView(ImageManager.load("uiImages/backgrounds/bg.jpg"));
        bgView.setPreserveRatio(false);
        bgView.setSmooth(true);

        // 绑定ImageView尺寸到root，实现随窗口缩放
        bgView.fitWidthProperty().bind(root.widthProperty());
        bgView.fitHeightProperty().bind(root.heightProperty());

        root.getChildren().add(bgView);
    }

    public Stage getStage() {
        return stage;
    }
}
