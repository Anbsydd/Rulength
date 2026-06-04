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

    public GameConfig() {
    }
}
