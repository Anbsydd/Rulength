package post.ui;

/**
 * 地图变换事件（统一）
 * 当 Camera 视窗状态（偏移、缩放）发生变化时发布
 * 替代原先的 MapDraggedEvent 和 MapScrolledEvent，消除双事件处理冲突
 *
 * @param offsetX  地图水平偏移（像素）
 * @param offsetY  地图垂直偏移（像素）
 * @param zoom     当前缩放比例
 */
public record MapTransformEvent(double offsetX, double offsetY, double zoom) {}
