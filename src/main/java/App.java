import config.StageConfig;
import config.ConfigLoader;
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

    public void start(Stage stage) throws Exception {
        StageConfig config = ConfigLoader.loadConfig("assets/stageConfig.json", StageConfig.class);

        StackPane root = new StackPane();
        Scene sc = new Scene(root);
        stage.setWidth(config.width);
        stage.setHeight(config.height);
        stage.setScene(sc);
        stage.setTitle(config.title);
        stage.setFullScreenExitHint(config.fullScreenExitHint);
        root.setBackground(new Background(new BackgroundFill(Color.BLACK, CornerRadii.EMPTY, Insets.EMPTY)));
        stage.show();
    }
}
