package game;

import config.CameraConfig;
import config.ConfigLoader;
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

    // Camera 配置文件路径
    private static final String CAMERA_CONFIG_PATH = "assets/cameraConfig.json";

    public Game(Stage stage) throws Exception {
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
        root.getChildren().add(camera);
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
        // Camera 的边界约束基于此逻辑尺寸计算
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
        // JavaFX 默认以节点中心为缩放原点，但 Camera 的 offsetX/offsetY 是以左上角计算的
        // 所以需要在 translate 中补偿缩放原点偏移：
        //   实际位置 = offset - (中心偏移 * (zoom - 1))
        //   即 offset - (size/2 * (zoom - 1))
        bus.subscribe(MapTransformEvent.class, (MapTransformEvent event) -> {
            double z = event.zoom();
            map.setTranslateX(event.offsetX() - MAP_LOGICAL_WIDTH / 2.0 * (z - 1));
            map.setTranslateY(event.offsetY() - MAP_LOGICAL_HEIGHT / 2.0 * (z - 1));
            map.setScaleX(z);
            map.setScaleY(z);
        });
    }

    private void initCamera() throws Exception {
        CameraConfig cameraConfig = ConfigLoader.loadConfig(CAMERA_CONFIG_PATH, CameraConfig.class);
        camera = new Camera(cameraConfig, MAP_LOGICAL_WIDTH, MAP_LOGICAL_HEIGHT);
    }
    private void sendRootSizeChangedEvent(double width, double height) {
        bus.publish(new StageSizeChange(width, height));
    }
    public Stage getStage() {
        return stage;
    }
    
}
