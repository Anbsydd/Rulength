package event;

/**
 * 游戏刻事件
 * TimeSystem 每帧按固定频率（60tick/s）发布此事件，
 * 所有需要每刻更新的逻辑通过订阅此事件实现
 *
 * @param tickCount 从TimeSystem启动至今的总游戏刻数
 */
public record TickEvent(long tickCount) {}
