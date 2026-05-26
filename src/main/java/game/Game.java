package game;

import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;
import post.EventBus;
import post.ui.StageSizeChange;
import util.ImageManager;
import util.PaneSizeManager;

import java.util.concurrent.*;

public class Game {
    public static EventBus bus;
    private final Stage stage;
    StackPane root;
    StackPane startMenu;
    StackPane map;
    public static ExecutorService mainPool;
    public Game(Stage stage) {
        bus = new EventBus();
        mainPool = new ThreadPoolExecutor(4, 8, 60L, TimeUnit.SECONDS, new LinkedBlockingQueue<>(100), Executors.defaultThreadFactory(), new ThreadPoolExecutor.AbortPolicy());
        this.stage = stage;
        initRoot();
//        initStartMenu();
        initMap();
        root.getChildren().add(map);
//        root.getChildren().add(startMenu);
    }
    
    
    private void initRoot() {
        root = stage.getRoot();
        root.heightProperty().addListener((obs, oldVal, newVal) -> {
            sendRootSizeChangedEvent(root.getWidth(), newVal.doubleValue());
        });
        root.widthProperty().addListener((obs, oldVal, newVal) -> {
            sendRootSizeChangedEvent(newVal.doubleValue(), root.getHeight());
        });
    }
    private void initStartMenu() {
        startMenu = new StackPane();
        PaneSizeManager.add(startMenu,1);
        PaneSizeManager.set(startMenu, root.getWidth(), root.getHeight());
        ImageView bgView = new ImageView(ImageManager.load("uiImages/backgrounds/bg.jpg"));
        bgView.setPreserveRatio(false);
        bgView.setSmooth(true);
        bgView.fitWidthProperty().bind(startMenu.widthProperty());
        bgView.fitHeightProperty().bind(startMenu.heightProperty());
        startMenu.getChildren().add(bgView);
    }
    private void initMap() {
        map = new StackPane();
        PaneSizeManager.add(map,1);
        PaneSizeManager.set(map, root.getWidth(), root.getHeight());
        ImageView bgView = new ImageView(ImageManager.load("uiImages/backgrounds/map.png"));
        bgView.setPreserveRatio(false);
        bgView.setSmooth(true);
        bgView.fitWidthProperty().bind(map.widthProperty());
        bgView.fitHeightProperty().bind(map.heightProperty());
        map.getChildren().add(bgView);
    }
    private void sendRootSizeChangedEvent(double width, double height) {
        bus.publish(new StageSizeChange(width, height));
    }
    public Stage getStage() {
        return stage;
    }
    
}
