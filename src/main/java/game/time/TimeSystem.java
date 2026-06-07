package game.time;

import config.TimeConfig;
import event.TickEvent;
import javafx.animation.AnimationTimer;

import static core.CoreAPI.bus;

/**
 * 游戏刻时钟系统
 * <p>
 * 以固定频率驱动游戏逻辑更新。
 * 每经过一个游戏刻，通过EventBus发布TickEvent，
 * 所有需要每刻更新的系统订阅TickEvent即可。
 * <p>
 * 频率由 TimeConfig 配置，默认 60 tick/s（每刻约16.67ms）。
 * <p>
 * 与Camera的渲染循环（AnimationTimer）独立运行：
 * - Camera: 每帧渲染插值，驱动画面显示
 * - TimeSystem: 固定频率驱动游戏逻辑，保证逻辑帧率稳定
 * <p>
 * 使用方式（在其他类中订阅）：
 * <pre>{@code
 * bus.subscribe(TickEvent.class, e -> {
 *     // 每游戏刻执行的逻辑
 *     long currentTick = e.tickCount();
 * });
 * }</pre>
 */
public class TimeSystem {

    /** 每秒游戏刻数（从配置注入） */
    private final long ticksPerSecond;

    /** 每刻纳秒间隔 */
    private final long tickIntervalNanos;

    /** 单例实例 */
    private static TimeSystem instance;

    /** AnimationTimer 驱动循环 */
    private AnimationTimer timer;

    /** 是否正在运行 */
    private volatile boolean running = false;

    /** 累计游戏刻数 */
    private long tickCount = 0;

    /** 上一次刻的纳秒时间戳 */
    private long lastTickTime = 0;

    /** 累积的纳秒偏移量（用于补偿精度丢失） */
    private long accumulated = 0;

    /** FPS统计 */
    private long lastFpsTime = 0;
    private long fpsTickCount = 0;

    public TimeSystem(TimeConfig config) {
        if (instance != null) {
            throw new IllegalStateException("TimeSystem 已存在实例，请使用 getInstance()");
        }
        instance = this;
        this.ticksPerSecond = config.ticksPerSecond;
        this.tickIntervalNanos = 1_000_000_000L / config.ticksPerSecond;
    }

    /**
     * 启动游戏刻时钟
     * <p>
     * AnimationTimer 在 JavaFX 线程中每帧调用 handle(long now)，
     * now 为系统纳秒时间（System.nanoTime() 风格）。
     * 累计时间达到 tickIntervalNanos 时发布一个游戏刻。
     */
    public void start() {
        if (running) return;
        running = true;

        timer = new AnimationTimer() {
            @Override
            public void handle(long now) {
                if (!running) return;

                // 首次调用初始化时间基准
                if (lastTickTime == 0) {
                    lastTickTime = now;
                    lastFpsTime = now;
                    return;
                }

                // 计算距上一刻经过的时间，加入累积
                long elapsed = now - lastTickTime;
                lastTickTime = now;
                accumulated += elapsed;

                // 按固定间隔消费累积时间
                while (accumulated >= tickIntervalNanos) {
                    accumulated -= tickIntervalNanos;
                    tickCount++;
                    fpsTickCount++;
                    // 发布游戏刻事件
                    bus.publish(new TickEvent(tickCount));
                }

                // FPS统计（每秒打印一次）
                long fpsElapsed = now - lastFpsTime;
                if (fpsElapsed >= 1_000_000_000L) {
                    double tickFps = fpsTickCount * 1_000_000_000.0 / fpsElapsed;
//                    System.out.printf("TimeSystem: %.1f tick/s (累计: %d)%n", tickFps, tickCount);
                    fpsTickCount = 0;
                    lastFpsTime = now;
                }
            }
        };

        timer.start();
        System.out.println("TimeSystem: 已启动，目标 " + ticksPerSecond + " tick/s");
    }

    /**
     * 停止游戏刻时钟
     */
    public void stop() {
        if (!running) return;
        running = false;
        if (timer != null) {
            timer.stop();
            timer = null;
        }
        lastTickTime = 0;
        accumulated = 0;
        System.out.println("TimeSystem: 已停止（累计 " + tickCount + " tick）");
    }

    /**
     * 是否正在运行
     */
    public boolean isRunning() {
        return running;
    }

    /**
     * 获取当前累计游戏刻数
     */
    public long getTickCount() {
        return tickCount;
    }

    /**
     * 获取目标每秒游戏刻数
     */
    public long getTicksPerSecond() {
        return ticksPerSecond;
    }

    /**
     * 重置计时器（不会自动停止）
     */
    public void reset() {
        tickCount = 0;
        lastTickTime = 0;
        accumulated = 0;
        fpsTickCount = 0;
        lastFpsTime = 0;
    }

    /**
     * 获取单例实例
     */
    public static TimeSystem getInstance() {
        return instance;
    }
}
