package game;

import config.*;
import core.CoreAPI;
import event.EventBus;
import event.MapTransformEvent;
import event.StageSizeChange;
import game.dialog.DialogSystem;
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
    // 配置文件路径
    private static final String GAME_CONFIG_PATH = "assets/config/gameConfig.json";
    private static final String CAMERA_CONFIG_PATH = "assets/config/cameraConfig.json";
    private static final String MINIMAP_CONFIG_PATH = "assets/config/miniMapConfig.json";
    private static final String TIMESYSTEM_CONFIG_PATH = "assets/config/timeConfig.json";
    // 游戏配置
    private GameConfig gameConfig;
    private SettingsUI settingsUI;
    private game.time.TimeSystem timeSystem;
    private DialogSystem dialogSystem;

    public Game(Stage stage) throws Exception {
        // 1. 初始化事件总线
        EventBus eventBus = new EventBus();
        // 2. 加载语言配置
        util.TextLan.load("Simplified Chinese.json");
        // 3. 加载游戏配置（获取线程池参数）
        gameConfig = ConfigLoader.loadConfig(GAME_CONFIG_PATH, GameConfig.class);
        ExecutorService pool = new ThreadPoolExecutor(
                gameConfig.corePoolSize, gameConfig.maxPoolSize,
                gameConfig.keepAliveSeconds, TimeUnit.SECONDS,
                new LinkedBlockingQueue<>(gameConfig.queueCapacity),
                Executors.defaultThreadFactory(),
                new ThreadPoolExecutor.AbortPolicy());
        // 4. 初始化 CoreAPI（基础设施门面）
        this.stage = stage;
        initRoot();
        new CoreAPI(eventBus, pool, root.getWidth(), root.getHeight());
        // 5. 后续初始化
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
        initDialogSystem();
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
     * 通过SliceInjector加载默认slice定义，再加载地图文件展开实例
     * moved=true → ConfiguredMoveSlice（跟随地图移动）
     * moved=false → ConfiguredStaticSlice（固定位置）
     */
    private void initSlices() throws Exception {
        // 1. 加载默认 slice 定义（缓存到 SliceInjector）
        config.SliceInjector.loadAll();
        // 2. 加载地图文件，展开为实例列表
        java.util.List<config.SliceConfig> configs = config.SliceInjector.loadMap("saves/default/map/test.json");
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
            System.out.println("SliceInjector: 已加载 Slice [ID=" + cfg.ID + ", mapId=" + cfg.mapId + ", name=" + cfg.name + "] moved=" + cfg.moved + " attributes=" + cfg.attributes + " methods=" + cfg.methods);
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
        CoreAPI.multiX = width / CoreAPI.getInstance().originSceneWidth;
        CoreAPI.multiY = height / CoreAPI.getInstance().originSceneHeight;
        double oldMultiX= oldWidth / CoreAPI.getInstance().originSceneWidth;
        double oldMultiY= oldHeight / CoreAPI.getInstance().originSceneHeight;
        CoreAPI.bus.publish(new StageSizeChange(width, height, CoreAPI.multiX, CoreAPI.multiY, oldWidth, oldHeight, oldMultiX, oldMultiY));
    }
    void moveWithMap(Node node){
        CoreAPI.bus.subscribe(MapTransformEvent.class, (MapTransformEvent event) -> {
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
        CoreAPI.stageRef = stage;
        settingsUI = new SettingsUI();
        PaneSizeManager.add(settingsUI, 1);
        PaneSizeManager.set(settingsUI, root.getWidth(), root.getHeight());
        root.getChildren().add(settingsUI);
    }

    private void initDialogSystem() {
        dialogSystem = new DialogSystem();
        PaneSizeManager.add(dialogSystem, 1,0.4);
        root.getChildren().add(dialogSystem);
        // 延迟一帧显示示例对话（等布局完成后）
        javafx.application.Platform.runLater(() ->
                dialogSystem.showDialog("assets/dialog/test.json")
        );
    }

    /**
     * 根据名称在所有已加载的 Slice 中查找
     */
    public static Slice findSliceByName(String name) {
        for (Slice slice : allSlices) {
            if (name.equals(slice.getName())) {
                return slice;
            }
        }
        return null;
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
                // 进入碰撞
                activeCollisions.add(pairKey);
                onCollisionEnter(self, other);
            } else if (colliding && wasColliding) {
                // 持续碰撞中（每帧约束）
                onCollisionStay(self, other);
            } else if (!colliding && wasColliding) {
                // 退出碰撞
                activeCollisions.remove(pairKey);
            }
        }
    }

    /**
     * 进入碰撞：触发操控方的hit及被碰撞方的beHit
     */
    private static void onCollisionEnter(Slice self, Slice other) {
        // 触发self的hit
        if (self instanceof ConfiguredMoveSlice ms) {
            String m = ms.getConfig().getMethod("hit");
            if (m != null) invokeSliceMethod(ms, m, other);
        } else if (self instanceof ConfiguredStaticSlice ss) {
            String m = ss.getConfig().getMethod("hit");
            if (m != null) invokeSliceMethod(ss, m, other);
        }
        // 触发对方的beHit（Obstruct等由invokeSliceMethod分发）
        if (other instanceof ConfiguredMoveSlice ms) {
            String m = ms.getConfig().getMethod("beHit");
            if (m != null) invokeSliceMethod(ms, m, self);
        } else if (other instanceof ConfiguredStaticSlice ss) {
            String m = ss.getConfig().getMethod("beHit");
            if (m != null) invokeSliceMethod(ss, m, self);
        }
    }

    /**
     * 持续碰撞中：如果障碍物有Obstruct，持续clamp在边界
     */
    private static void onCollisionStay(Slice mover, Slice obstacle) {
        if (hasObstruct(obstacle)) {
            clampToBoundary(mover, obstacle);
        }
    }

    private static boolean hasObstruct(Slice slice) {
        String m = null;
        if (slice instanceof ConfiguredMoveSlice ms) m = ms.getConfig().getMethod("beHit");
        else if (slice instanceof ConfiguredStaticSlice ss) m = ss.getConfig().getMethod("beHit");
        return "Obstruct".equals(m);
    }

    /**
     * 通过methods映射调用方法
     */
    private static void invokeSliceMethod(Slice caller, String methodName, Slice target) {
        switch (methodName) {
            case "attack" -> System.out.println(caller.getName() + "攻击了" + target.getName());
            case "Obstruct" -> clampToBoundary(target, caller); // caller=障碍物, target=移动者
            default -> System.out.println("未知方法: " + methodName);
        }
    }

    /**
     * 沿运动轨迹将mover挡在障碍物边界外，同时允许贴墙滑动
     *
     * 阶段1 — 二分查找：在上一帧安全位置与当前位置之间，精确找到碰撞入口点
     * 阶段2 — 轴独立滑动：从入口点分别尝试X和Y轴，不碰撞则放行
     */
    private static void clampToBoundary(Slice mover, Slice obstructing) {
        double lastTraX = mover.getLastTranslateX();
        double lastTraY = mover.getLastTranslateY();
        double curTraX = mover.getTranslateX();
        double curTraY = mover.getTranslateY();
        double dx = curTraX - lastTraX;
        double dy = curTraY - lastTraY;
        double totalLen = Math.sqrt(dx * dx + dy * dy);
        // totalLen 是 translate 空间距离，需转换到场景（鼠标）空间比较
        // StaticSlice: translateDelta = mouseDelta / zoom，场景距离 = totalLen * zoom
        // MoveSlice: translateDelta = mouseDelta，场景距离 = totalLen * 1.0
        if (totalLen * mover.isMoveSlice() < 0.5) return;

        // —— 阶段1：二分查找入口点 ——
        // lastTra 是安全的，curTra 是碰撞的；在它们之间找边界
        double safeT = 0.0, collideT = 1.0;
        for (int iter = 0; iter < 12; iter++) {
            double mid = (safeT + collideT) / 2.0;
            double testX = lastTraX + dx * mid;
            double testY = lastTraY + dy * mid;
            mover.setMapX(mover.finalTraToMapX(testX));
            mover.setMapY(mover.finalTraToMapY(testY));
            if (CollisionUtil.checkCollision(mover, obstructing)) {
                collideT = mid;
            } else {
                safeT = mid;
            }
        }

        // 入口点 = 最后一个安全位置
        double entryX = lastTraX + dx * safeT;
        double entryY = lastTraY + dy * safeT;
        mover.setMapX(mover.finalTraToMapX(entryX));
        mover.setMapY(mover.finalTraToMapY(entryY));

        // —— 阶段2：从入口点轴独立滑动 ——
        double remX = curTraX - entryX;
        double remY = curTraY - entryY;

        // 尝试X轴（Y固定在入口点）
        double finalX, finalY;
        if (Math.abs(remX) * mover.isMoveSlice() > 0.5) {
            mover.setMapX(mover.finalTraToMapX(entryX + remX));
            mover.setMapY(mover.finalTraToMapY(entryY));
            finalX = CollisionUtil.checkCollision(mover, obstructing) ? entryX : entryX + remX;
        } else {
            finalX = entryX;
        }

        // 尝试Y轴（X使用上一步结果）
        if (Math.abs(remY) * mover.isMoveSlice() > 0.5) {
            mover.setMapX(mover.finalTraToMapX(finalX));
            mover.setMapY(mover.finalTraToMapY(entryY + remY));
            finalY = CollisionUtil.checkCollision(mover, obstructing) ? entryY : entryY + remY;
        } else {
            finalY = entryY;
        }

        mover.setMapX(mover.finalTraToMapX(finalX));
        mover.setMapY(mover.finalTraToMapY(finalY));
        // 同步所有锚点
        mover.syncFullDragAnchor();
    }
}
