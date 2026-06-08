package core;

import config.CameraConfig;
import config.MiniMapConfig;
import config.SliceConfig;
import config.StageConfig;
import game.slice.ConfiguredMoveSlice;
import game.slice.ConfiguredStaticSlice;
import game.slice.Slice;
import game.window.Camera;
import game.window.MiniMap;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;

import java.util.ArrayList;
import java.util.List;

/**
 * GameAPI — 游戏操作 API
 * <p>
 * 封装所有"对游戏对象的操作"，供其他模块统一调用。
 * 与 CoreAPI（基础设施门面）互补：
 * - CoreAPI = 基础设施（EventBus、线程池、缩放系数……）
 * - GameAPI = 游戏逻辑（Slice 管理、Camera 操作、配置热更新……）
 * <p>
 * 由 {@code Game} 构造函数在完成子系统初始化后调用 {@link #init} 注入内部引用。
 * 初始化完成后，其他模块（如 SettingsUI、DialogSystem）通过静态方法操作游戏，
 * 无需直接引用 game 包或 Game 类。
 * <p>
 * ============================================================
 * ⚠️ 修改此类时须同步更新 GameAPI.md 说明文档
 * ============================================================
 */
public final class GameAPI {

    // ==================== 内部引用（由 Game.init() 注入） ====================

    private static Camera camera;
    private static MiniMap miniMap;
    private static StackPane moveLayer;
    private static StackPane staticLayer;
    private static javafx.stage.Stage javafxStage;

    // ==================== Slice 对象池 ====================

    /** 所有已加载的 Slice（用于碰撞检测、查找等） */
    private static final List<Slice> allSlices = new ArrayList<>();

    // ==================== 初始化 ====================

    /**
     * 注入内部引用（可多次调用，后续调用更新新增的引用）
     * <p>
     * MiniMap 在 initSlices 之后才创建，因此需要分两次调用：
     * 第一次在 initSlices 前（miniMap=null），第二次在 initMiniMap 后（miniMap 就绪）。
     */
    public static void init(Camera cam, MiniMap mm,
                            StackPane movePane, StackPane staticPane,
                            javafx.stage.Stage jfxStage) {
        if (cam != null) camera = cam;
        if (mm != null) miniMap = mm;
        if (movePane != null) moveLayer = movePane;
        if (staticPane != null) staticLayer = staticPane;
        if (jfxStage != null) javafxStage = jfxStage;
    }

    // ==================== Slice 管理 ====================

    /**
     * 根据 SliceConfig 创建 Slice 并加入场景和碰撞池
     *
     * @param config Slice 配置（含 moved/mapX/mapY/text 等）
     * @return 创建的 Slice 实例
     */
    public static Slice spawnSlice(SliceConfig config) {
        Slice slice;
        if (config.moved) {
            slice = new ConfiguredMoveSlice(config);
            moveLayer.getChildren().add(slice);
        } else {
            slice = new ConfiguredStaticSlice(config);
            staticLayer.getChildren().add(slice);
        }
        slice.onLoad();
        allSlices.add(slice);
        System.out.println("GameAPI: 已加载 Slice [ID=" + config.ID
                + ", mapId=" + config.mapId + ", name=" + config.name
                + "] moved=" + config.moved);
        return slice;
    }

    /**
     * 从场景和碰撞池移除 Slice
     *
     * @param slice 要移除的 Slice 实例
     */
    public static void removeSlice(Slice slice) {
        slice.onUnload();
        // 从父容器移除
        Pane parent = (Pane) slice.getParent();
        if (parent != null) {
            parent.getChildren().remove(slice);
        }
        allSlices.remove(slice);
    }

    /**
     * 根据名称查找 Slice
     */
    public static Slice findSlice(String name) {
        for (Slice slice : allSlices) {
            if (name.equals(slice.getName())) {
                return slice;
            }
        }
        return null;
    }

    /**
     * 获取所有已加载的 Slice
     */
    public static List<Slice> getAllSlices() {
        return allSlices;
    }

    // ==================== Camera 操作 ====================

    /**
     * 跳转到指定偏移位置（保持当前缩放不变）
     */
    public static void cameraJumpTo(double offsetX, double offsetY) {
        Camera.jumpTo(offsetX, offsetY);
    }

    /**
     * 冻结相机动画，停在当前位置
     */
    public static void cameraFreeze() {
        Camera.freeze();
    }

    /**
     * 获取当前相机缩放倍数
     */
    public static double getCameraZoom() {
        return Camera.zoom;
    }

    // ==================== 配置热更新 ====================

    /**
     * 应用相机配置（运行时热更新）
     */
    public static void applyCameraConfig(CameraConfig config) {
        if (camera != null) camera.use(config);
    }

    /**
     * 应用小地图配置（运行时热更新）
     */
    public static void applyMiniMapConfig(MiniMapConfig config) {
        if (miniMap != null) miniMap.applyConfig(config);
    }

    /**
     * 应用窗口配置（运行时热更新）
     */
    public static void applyStageConfig(StageConfig config) {
        if (javafxStage == null) return;
        javafxStage.setTitle(config.title);
        javafxStage.setWidth(config.width);
        javafxStage.setHeight(config.height);
        javafxStage.setFullScreenExitHint(config.fullScreenExitHint);
    }
}
