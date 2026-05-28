package game;

import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;
import post.EventBus;
import post.ui.MapTransformEvent;
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
    Camera camera;
    public static ExecutorService mainPool;

    // 地图逻辑尺寸（原始图片尺寸，用于 Camera 边界约束）
    private static final double MAP_LOGICAL_WIDTH = 3840;
    private static final double MAP_LOGICAL_HEIGHT = 2160;

    // 缩放范围
    private static final double MIN_ZOOM = 0.3;
    private static final double MAX_ZOOM = 3.0;

    public Game(Stage stage) {
        bus = new EventBus();
        mainPool = new ThreadPoolExecutor(4, 8, 60L, TimeUnit.SECONDS, new LinkedBlockingQueue<>(100), Executors.defaultThreadFactory(), new ThreadPoolExecutor.AbortPolicy());
        this.stage = stage;
        initRoot();
//        initStartMenu();
//        root.getChildren().add(startMenu);
        initMap();
        initCamera();
        // 先添加 map（底层），再添加 camera（顶层，拦截输入）
        root.getChildren().add(map);
        root.getChildren().add(camera.getPane());
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
        // map 使用地图逻辑尺寸，不再跟随窗口大小
        map.setPrefSize(MAP_LOGICAL_WIDTH, MAP_LOGICAL_HEIGHT);
        map.setMaxSize(MAP_LOGICAL_WIDTH, MAP_LOGICAL_HEIGHT);
        map.setMinSize(MAP_LOGICAL_WIDTH, MAP_LOGICAL_HEIGHT);

        ImageView bgView = new ImageView(ImageManager.load("uiImages/backgrounds/map.png"));
        bgView.setPreserveRatio(false);
        bgView.setSmooth(true);
        bgView.setFitWidth(MAP_LOGICAL_WIDTH);
        bgView.setFitHeight(MAP_LOGICAL_HEIGHT);
        map.getChildren().add(bgView);

        // 订阅 MapTransformEvent，应用平移和缩放
        bus.subscribe(MapTransformEvent.class, (MapTransformEvent event) -> {
            map.setTranslateX(event.offsetX());
            map.setTranslateY(event.offsetY());
            map.setScaleX(event.zoom());
            map.setScaleY(event.zoom());
        });
    }

    private void initCamera() {
        camera = new Camera(MIN_ZOOM, MAX_ZOOM, MAP_LOGICAL_WIDTH, MAP_LOGICAL_HEIGHT);
    }
    private void sendRootSizeChangedEvent(double width, double height) {
        bus.publish(new StageSizeChange(width, height));
    }
    public Stage getStage() {
        return stage;
    }
    
}
