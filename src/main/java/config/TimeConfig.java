package config;

/**
 * TimeSystem 配置
 */
public class TimeConfig {
    /** 每秒游戏刻数 */
    public long ticksPerSecond = 60;

    /** 是否在启动时自动开始计时 */
    public boolean autoStart = true;

    public TimeConfig() {
    }
}
