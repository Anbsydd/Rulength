package game;

import config.*;
import event.EventBus;
import event.MapTransformEvent;
import event.StageSizeChange;
import game.slice.ConfiguredMoveSlice;
import game.slice.ConfiguredStaticSlice;
import game.slice.Slice;
import game.window.Camera;
import game.window.MiniMap;
import game.window.SettingsUI;
import game.window.Stage;
import javafx.scene.Node;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;
import util.CollisionUtil;
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
    /** 存储所有碰撞相关 slice，用于碰撞检测 */
    private static final java.util.List<Slice> allSlices = new java.util.ArrayList<>();

    /** 当前活跃碰撞对集合（pair key），用于进出碰撞检测 */
    private static final java.util.Set<Long> activeCollisions = java.util.concurrent.ConcurrentHashMap.newKeySet();
    public static ExecutorService mainPool;
    // 配置文件路径
    private static final String GAME_CONFIG_PATH = "assets/config/gameConfig.json";
    private static final String CAMERA_CONFIG_PATH = "assets/config/cameraConfig.json";
    private static final String MINIMAP_CONFIG_PATH = "assets/config/miniMapConfig.json";
    private static final String TIMESYSTEM_CONFIG_PATH = "assets/config/timeConfig.json";
    // 游戏配置
    private GameConfig gameConfig;
    private SettingsUI settingsUI;
    public static game.window.Stage stage_ref;
    public final double ORIGIN_SCENE_WIDTH;
    public final double ORIGIN_SCENE_HEIGHT;
    public static double multiX = 1.0;
    public static double multiY = 1.0;
    private game.time.TimeSystem timeSystem;

    public Game(Stage stage) throws Exception {
        bus = new EventBus();
        // 加载语言配置
        util.TextLan.load("Simplified Chinese.json");
        // 加载游戏配置
        gameConfig = ConfigLoader.loadConfig(GAME_CONFIG_PATH, GameConfig.class);
        mainPool = new ThreadPoolExecutor(gameConfig.corePoolSize, gameConfig.maxPoolSize, gameConfig.keepAliveSeconds, TimeUnit.SECONDS, new LinkedBlockingQueue<>(gameConfig.queueCapacity), Executors.defaultThreadFactory(), new ThreadPoolExecutor.AbortPolicy());
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
        initSlices();
        initMiniMap();
        // 先添加 map（底层），再添加 camera（顶层，拦截输入），最后添加小地图（最顶层），设置界面（最最顶层）
        root.getChildren().add(map);
        root.getChildren().add(camera);
        root.getChildren().add(static1);
        root.getChildren().add(move);
        root.getChildren().add(miniMap);
        initSettings();
        initTimeSystem();
    }
    
    private void initStatic() {
        static1 = new StackPane();
        PaneSizeManager.add(static1, 1);
        PaneSizeManager.set(static1, root.getWidth(), root.getHeight());
        static1.setPickOnBounds(false);
        moveWithMap(static1);
    }
    private void initMove() {
        move = new StackPane();
        move.setPickOnBounds(false);
        PaneSizeManager.add(move, 1);
        PaneSizeManager.set(move, root.getWidth(), root.getHeight());
    }

    /**
     * 通过SliceInjector加载所有slice配置，根据moved字段创建对应的Slice实例
     * moved=true → ConfiguredMoveSlice（跟随地图移动）
     * moved=false → ConfiguredStaticSlice（固定位置）
     */
    private void initSlices() throws Exception {
        java.util.List<config.SliceConfig> configs = config.SliceInjector.loadAll();
        for (config.SliceConfig cfg : configs) {
            game.slice.Slice slice;
            if (cfg.moved) {
                slice = new game.slice.ConfiguredMoveSlice(cfg);
                move.getChildren().add(slice);
            } else {
                slice = new game.slice.ConfiguredStaticSlice(cfg);
                static1.getChildren().add(slice);
            }
            slice.onLoad();
            allSlices.add(slice);
            // 打印加载信息，便于调试
            System.out.println("SliceInjector: 已加载 Slice [" + cfg.name + "] moved=" + cfg.moved + " attributes=" + cfg.attributes + " methods=" + cfg.methods);
        }
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
        ImageView bgView = new ImageView(ImageManager.load(gameConfig.startMenuImagePath));
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

        ImageView bgView = new ImageView(ImageManager.load(gameConfig.mapImagePath));
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

    private MiniMap miniMap;

    private void initMiniMap() throws Exception {
        MiniMapConfig miniMapConfig = ConfigLoader.loadConfig(MINIMAP_CONFIG_PATH, MiniMapConfig.class);
        miniMap = new MiniMap(miniMapConfig, root.getWidth(), root.getHeight());
        // 延迟定位：等布局完成后再定位
        javafx.application.Platform.runLater(() -> miniMap.reposition());
        // 窗口大小变化时重新定位
        root.widthProperty().addListener((obs, oldVal, newVal) -> miniMap.reposition());
        root.heightProperty().addListener((obs, oldVal, newVal) -> miniMap.reposition());
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

    private void initSettings() {
        stage_ref = stage;
        settingsUI = new SettingsUI();
        PaneSizeManager.add(settingsUI, 1);
        PaneSizeManager.set(settingsUI, root.getWidth(), root.getHeight());
        root.getChildren().add(settingsUI);
    }

    /**
     * 加载 TimeConfig 并启动 TimeSystem
     */
    private void initTimeSystem() throws Exception {
        TimeConfig timeConfig = ConfigLoader.loadConfig(TIMESYSTEM_CONFIG_PATH, TimeConfig.class);
        timeSystem = new game.time.TimeSystem(timeConfig);
        if (timeConfig.autoStart) {
            timeSystem.start();
        }
    }

    /**
     * 应用相机配置（运行时热更新）
     */
    public void applyCameraConfig(CameraConfig config) {
        camera.use(config);
    }

    /**
     * 应用小地图配置（运行时热更新）
     */
    public void applyMiniMapConfig(MiniMapConfig config) {
        miniMap.applyConfig(config);
    }

    /**
     * 应用窗口配置（运行时热更新）
     */
    public void applyStageConfig(StageConfig config) {
        javafx.stage.Stage javafxStage = stage.getJavafxStage();
        javafxStage.setTitle(config.title);
        javafxStage.setWidth(config.width);
        javafxStage.setHeight(config.height);
        javafxStage.setFullScreenExitHint(config.fullScreenExitHint);
    }

    /**
     * 为两个Slice生成唯一碰撞对key（较小的identity在前）
     */
    private static long collisionPairKey(Slice a, Slice b) {
        int idA = System.identityHashCode(a);
        int idB = System.identityHashCode(b);
        long key = ((long) Math.min(idA, idB) << 32) | (Math.max(idA, idB) & 0xFFFFFFFFL);
        return key;
    }

    /**
     * 检测指定Slice与其他所有Slice的碰撞
     * 记录碰撞进出状态，每次进出视为一次碰撞
     */
    public static void checkCollisions(Slice self) {
        for (Slice other : allSlices) {
            if (other == self) continue;
            long pairKey = collisionPairKey(self, other);
            boolean colliding = CollisionUtil.checkCollision(self, other);
            boolean wasColliding = activeCollisions.contains(pairKey);

            if (colliding && !wasColliding) {
                // 进入碰撞 → 触发hit方法
                activeCollisions.add(pairKey);
                onCollisionEnter(self, other);
            } else if (!colliding && wasColliding) {
                // 退出碰撞
                activeCollisions.remove(pairKey);
            }
        }
    }

    /**
     * 碰撞进入时：触发self的methods映射中的hit方法
     */
    private static void onCollisionEnter(Slice self, Slice other) {
        // 触发self的hit（攻击方）
        if (self instanceof ConfiguredMoveSlice ms) {
            String methodName = ms.getConfig().getMethod("hit");
            if (methodName != null) invokeSliceMethod(ms, methodName, other);
        } else if (self instanceof ConfiguredStaticSlice ss) {
            String methodName = ss.getConfig().getMethod("hit");
            if (methodName != null) invokeSliceMethod(ss, methodName, other);
        }
        // 触发对方的beHit
        if (other instanceof ConfiguredMoveSlice ms) {
            String methodName = ms.getConfig().getMethod("beHit");
            if (methodName != null) invokeSliceMethod(ms, methodName, self);
        } else if (other instanceof ConfiguredStaticSlice ss) {
            String methodName = ss.getConfig().getMethod("beHit");
            if (methodName != null) invokeSliceMethod(ss, methodName, self);
        }
    }

    /**
     * 通过methods映射调用方法
     */
    private static void invokeSliceMethod(Slice caller, String methodName, Slice target) {
        if ("attack".equals(methodName)) {
            System.out.println(caller.getName() + "攻击了" + target.getName());
        } else if ("beAttacked".equals(methodName)) {
            System.out.println(caller.getName() + "被" + target.getName()+ "攻击了");
        } else {
            System.out.println("未知方法: " + methodName);
        }
    }
}
