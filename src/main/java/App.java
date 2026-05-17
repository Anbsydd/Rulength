import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.CornerRadii;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

public class App extends Application {
    static class StageConfig {
        String title;
        int width;
        int height;
        String fullScreenExitHint;

        StageConfig() {}
    }

    public void start(Stage stage) throws Exception {
//        StageConfig config = new StageConfig();
//        ConfigLoader.fillConfigObject(config);

        StackPane root = new StackPane();
        Scene sc = new Scene(root);
        stage.setWidth(800);
        stage.setHeight(600);
        stage.setScene(sc);
        stage.setTitle("config.title");
        stage.setFullScreenExitHint(null);
        root.setBackground(new Background(new BackgroundFill(Color.BLACK, CornerRadii.EMPTY, Insets.EMPTY)));
        stage.show();
    }
}
