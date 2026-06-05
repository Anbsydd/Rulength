package config;

/**
 * 游戏主配置
 */
public class GameConfig {
    /** 线程池核心线程数 */
    public int corePoolSize;
    /** 线程池最大线程数 */
    public int maxPoolSize;
    /** 线程池空闲线程存活时间（秒） */
    public long keepAliveSeconds;
    /** 线程池任务队列容量 */
    public int queueCapacity;
    /** 地图背景图片路径 */
    public String mapImagePath;
    /** 开始菜单背景图片路径 */
    public String startMenuImagePath;
    /** 渲染帧率上限（需显示器支持，默认60） */
    public int maxFrameRate = 60;
    /** 是否开启垂直同步（默认true，关闭可能画面撕裂但帧率更高） */
    public boolean vSync = true;

    public GameConfig() {
    }
}
