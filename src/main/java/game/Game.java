package game;

import config.CameraConfig;
import config.ConfigLoader;
import data.MoveSlice;
import data.StaticSlice;
import game.window.Camera;
import game.window.Stage;
import javafx.scene.Node;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
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
    StackPane static1;
    StackPane move;
    public static ExecutorService mainPool;
    // Camera 配置文件路径
    private static final String CAMERA_CONFIG_PATH = "assets/config/cameraConfig.json";
    public final double ORIGIN_SCENE_WIDTH;
    public final double ORIGIN_SCENE_HEIGHT;
    public static double multiX = 1.0;
    public static double multiY = 1.0;
    public Game(Stage stage) throws Exception {
        bus = new EventBus();
        mainPool = new ThreadPoolExecutor(4, 8, 60L, TimeUnit.SECONDS, new LinkedBlockingQueue<>(100), Executors.defaultThreadFactory(), new ThreadPoolExecutor.AbortPolicy());
        this.stage = stage;
        initRoot();
        ORIGIN_SCENE_WIDTH=root.getWidth();
        ORIGIN_SCENE_HEIGHT=root.getHeight();
//        initStartMenu();
//        root.getChildren().add(startMenu);
        initMap();
        initCamera();
        initStatic();
        initMove();
        // 先添加 map（底层），再添加 camera（顶层，拦截输入）
        root.getChildren().add(map);
        root.getChildren().add(camera);
        root.getChildren().add(static1);
        root.getChildren().add(move);
        camera.addEventFilter(MouseEvent.MOUSE_CLICKED, e -> {
            if(e.getButton() == MouseButton.SECONDARY){
            
            }
        });
    }
    
    private void initStatic() {
        static1 = new StackPane();
        PaneSizeManager.add(static1, 1);
        PaneSizeManager.set(static1, root.getWidth(), root.getHeight());
        static1.setPickOnBounds(false);
        moveWithMap(static1);
        // 创建Player实例
        StaticSlice player = new StaticSlice();
        player.setName("Player1");
        player.onLoad();
        static1.getChildren().add(player);
    }
    private void initMove() {
        move = new StackPane();
        move.setPickOnBounds(false);
        PaneSizeManager.add(move, 1);
        PaneSizeManager.set(move, root.getWidth(), root.getHeight());
        // 创建Player实例
        MoveSlice player = new MoveSlice();
        player.setName("Player2");
        player.onLoad();
        move.getChildren().add(player);
    }
    
    
    
    private void initRoot() {
        root = stage.getRoot();
        root.heightProperty().addListener((obs, oldVal, newVal) -> sendRootSizeChangedEvent(root.getWidth(), newVal.doubleValue(),root.getWidth(), oldVal.doubleValue()));
        root.widthProperty().addListener((obs, oldVal, newVal) -> sendRootSizeChangedEvent(newVal.doubleValue(), root.getHeight(), oldVal.doubleValue(), root.getHeight()));
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
        moveWithMap(map);
        
    }

    private void initCamera() throws Exception {
        CameraConfig cameraConfig = ConfigLoader.loadConfig(CAMERA_CONFIG_PATH, CameraConfig.class);
        camera = new Camera(cameraConfig, root.getWidth(), root.getHeight());
    }
    private void sendRootSizeChangedEvent(double width, double height, double oldWidth, double oldHeight) {
        multiX = width/ORIGIN_SCENE_WIDTH;
        multiY = height/ORIGIN_SCENE_HEIGHT;
        double oldMultiX= oldWidth/ORIGIN_SCENE_WIDTH;
        double oldMultiY= oldHeight/ORIGIN_SCENE_HEIGHT;
        bus.publish(new StageSizeChange(width, height,multiX,multiY,oldWidth,oldHeight,oldMultiX,oldMultiY));
    }
    void moveWithMap(Node node){
        bus.subscribe(MapTransformEvent.class, (MapTransformEvent event) -> {
            node.setTranslateX(event.offsetX());
            node.setTranslateY(event.offsetY());
            node.setScaleX(event.zoom());
            node.setScaleY(event.zoom());
        });
    }
    public Stage getStage() {
        return stage;
    }
}
