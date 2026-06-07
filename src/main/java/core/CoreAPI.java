package core;

import event.EventBus;
import game.window.Stage;

import java.util.concurrent.ExecutorService;

/**
 * CoreAPI — 基础设施门面
 * <p>
 * 承载所有跨模块的基础设施，消除 game.Game 作为全局静态依赖中心的现状。
 * 所有模块只需依赖 core 包即可访问基础设施，无需直接引用 game 包。
 * <p>
 * 当前承载的基础设施：
 * - EventBus 事件总线（原 Game.bus）
 * - 线程池（原 Game.mainPool）
 * - 窗口缩放系数（原 Game.multiX/Y）
 * - 舞台引用（原 Game.stage_ref）
 * - 原始场景尺寸（原 Game.ORIGIN_SCENE_WIDTH/HEIGHT）
 */
public final class CoreAPI {

    /** 事件总线 — 全局唯一，组件间通信 */
    public static EventBus bus;

    /** 主线程池 — 异步任务执行 */
    public static ExecutorService mainPool;

    /** 窗口缩放系数 X（当前宽度 / 原始宽度） */
    public static double multiX = 1.0;

    /** 窗口缩放系数 Y（当前高度 / 原始高度） */
    public static double multiY = 1.0;

    /** 舞台引用 — 用于获取 JavaFX Stage 和 Game */
    public static Stage stageRef;

    /** 原始场景宽度（设计分辨率） */
    public final double originSceneWidth;

    /** 原始场景高度（设计分辨率） */
    public final double originSceneHeight;

    /** 单例实例 */
    private static CoreAPI instance;

    public CoreAPI(EventBus bus, ExecutorService mainPool,
                   double originSceneWidth, double originSceneHeight) {
        if (instance != null) {
            throw new IllegalStateException("CoreAPI 已存在实例，请使用 getInstance()");
        }
        instance = this;

        CoreAPI.bus = bus;
        CoreAPI.mainPool = mainPool;
        this.originSceneWidth = originSceneWidth;
        this.originSceneHeight = originSceneHeight;
    }

    /**
     * 获取 CoreAPI 单例
     */
    public static CoreAPI getInstance() {
        return instance;
    }
}
