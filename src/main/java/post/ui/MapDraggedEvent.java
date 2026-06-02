package post.ui;

/**
 * 地图变换事件
 * 当 Camera 视窗状态发生变化时发布
 * Map 层订阅此事件来应用 translateX/Y, scaleX/Y
 *
 * @param offsetX  地图水平偏移（像素）
 * @param offsetY  地图垂直偏移（像素）
 */
public record MapDraggedEvent(double offsetX, double offsetY) {
}
