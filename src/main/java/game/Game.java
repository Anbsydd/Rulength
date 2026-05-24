package game;

import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;
import util.ImageManager;

import java.util.concurrent.*;

public class Game {

    private final Stage stage;
    StackPane root;
    StackPane startMenu;
    public static ExecutorService mainPool;
    public Game(Stage stage) {
        mainPool = new ThreadPoolExecutor(4, 8, 60L, TimeUnit.SECONDS, new LinkedBlockingQueue<>(100), Executors.defaultThreadFactory(), new ThreadPoolExecutor.AbortPolicy());
        this.stage = stage;
        initRoot();
        startMenu = new StackPane();
        root.getChildren().add(startMenu);
        initStartMenu();
    }
    
    private void initStartMenu() {
        // startMenu层：绑定尺寸到root
        startMenu.prefWidthProperty().bind(root.widthProperty());
        startMenu.prefHeightProperty().bind(root.heightProperty());
        
        ImageView bgView = new ImageView(ImageManager.load("uiImages/backgrounds/bg.jpg"));
        bgView.setPreserveRatio(false);
        bgView.setSmooth(true);
        bgView.fitWidthProperty().bind(startMenu.widthProperty());
        bgView.fitHeightProperty().bind(startMenu.heightProperty());
        startMenu.getChildren().add(bgView);
    }
    
    private void initRoot() {
        root = stage.getRoot();
        root.heightProperty().addListener((obs, oldVal, newVal) -> {
        
        });
    }

    public Stage getStage() {
        return stage;
    }
}
