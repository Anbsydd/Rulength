package game;

import javafx.scene.input.ScrollEvent;
import javafx.scene.layout.StackPane;
import post.ui.MapTransformEvent;
import post.ui.StageSizeChange;
import util.PaneSizeManager;

import static game.Game.bus;

/**
 * Camera 视窗层
 *
 * 透明 StackPane，覆盖在 Map 上方，固定不动。
 * 拦截所有鼠标/滚轮输入事件，维护视窗状态（偏移、缩放），
 * 当状态发生变化时通过 EventBus 发布 MapTransformEvent。
 *
 * 层级结构：
 *   root
 *     ├── map   (StackPane) — 地图背景，被平移/缩放
 *     └── camera (StackPane) — 本类，接收指令，不移动
 */
public class Camera {

    private final StackPane camera;

    // ---- 视窗状态 ----
    private double offsetX = 0;
    private double offsetY = 0;
    private double zoom = 1.0;

    // ---- 缩放限制 ----
    private final double minZoom;
    private final double maxZoom;

    // ---- 缩放步进 ----
    private static final double ZOOM_STEP = 0.1;

    // ---- 地图逻辑尺寸（用于边界约束）----
    private double mapWidth;
    private double mapHeight;

    // ---- 视口尺寸（camera 自身尺寸 = 窗口尺寸）----
    private double viewportWidth;
    private double viewportHeight;

    // ---- 拖拽状态 ----
    private double dragStartX;
    private double dragStartY;
    private double dragStartOffsetX;
    private double dragStartOffsetY;
    private boolean isDragging = false;

    /**
     * @param minZoom   最小缩放倍率
     * @param maxZoom   最大缩放倍率
     * @param mapWidth  地图逻辑宽度（像素）
     * @param mapHeight 地图逻辑高度（像素）
     */
    public Camera(double minZoom, double maxZoom, double mapWidth, double mapHeight) {
        this.camera = new StackPane();
        this.minZoom = minZoom;
        this.maxZoom = maxZoom;
        this.mapWidth = mapWidth;
        this.mapHeight = mapHeight;

        initCameraPane();
        initInputHandlers();
        initViewportListener();
    }

    private void initCameraPane() {
        // camera 透明，不拦截背景绘制，但拦截鼠标事件
        camera.setPickOnBounds(true);
        // camera 尺寸始终等于窗口尺寸
        PaneSizeManager.add(camera, 1);
    }

    private void initViewportListener() {
        // 监听窗口大小变化，更新视口尺寸
        bus.subscribe(StageSizeChange.class, (StageSizeChange event) -> {
            viewportWidth = event.width();
            viewportHeight = event.height();
            // 窗口变化后，重新约束偏移（可能需要回弹）
            clampAndPublish();
        });
    }

    private void initInputHandlers() {
        // ---- 滚轮缩放 ----
        camera.setOnScroll((ScrollEvent event) -> {
            event.consume();

            double delta = event.getDeltaY() > 0 ? ZOOM_STEP : -ZOOM_STEP;
            double newZoom = clampZoom(zoom + delta);
            if (newZoom == zoom) return; // 缩放无变化，不发送事件

            // 以鼠标位置为中心缩放
            double mouseX = event.getX();
            double mouseY = event.getY();

            // 计算缩放前鼠标指向的地图坐标
            double mapPointX = (mouseX - offsetX) / zoom;
            double mapPointY = (mouseY - offsetY) / zoom;

            // 更新缩放
            zoom = newZoom;

            // 缩放后，让同一地图坐标仍在鼠标位置下
            offsetX = mouseX - mapPointX * zoom;
            offsetY = mouseY - mapPointY * zoom;

            clampAndPublish();
        });

        // ---- 拖拽平移 ----
        camera.setOnMousePressed(event -> {
            dragStartX = event.getX();
            dragStartY = event.getY();
            dragStartOffsetX = offsetX;
            dragStartOffsetY = offsetY;
            isDragging = true;
            event.consume();
        });

        camera.setOnMouseDragged(event -> {
            if (!isDragging) return;
            event.consume();

            double dx = event.getX() - dragStartX;
            double dy = event.getY() - dragStartY;

            offsetX = dragStartOffsetX + dx;
            offsetY = dragStartOffsetY + dy;

            clampAndPublish();
        });

        camera.setOnMouseReleased(event -> {
            isDragging = false;
            event.consume();
        });
    }

    /**
     * 约束偏移量到合法范围，并在状态有变化时发布事件
     */
    private void clampAndPublish() {
        double oldOffsetX = offsetX;
        double oldOffsetY = offsetY;

        offsetX = clampOffsetX(offsetX);
        offsetY = clampOffsetY(offsetY);

        // 只有状态真正变化时才发布事件
        if (offsetX != oldOffsetX || offsetY != oldOffsetY || true) {
            // 注意：缩放变化时 offset 可能被 clamp 回原值，但 zoom 变了也需要发布
            publishTransform();
        }
    }

    /**
     * 发布当前视窗状态
     */
    private void publishTransform() {
        bus.publish(new MapTransformEvent(offsetX, offsetY, zoom));
    }

    // ---- 约束计算 ----

    private double clampZoom(double z) {
        return Math.max(minZoom, Math.min(maxZoom, z));
    }

    /**
     * 约束水平偏移：不允许拖出地图边界
     * 地图可视范围 = [offsetX, offsetX + viewportWidth]
     * 地图实际范围 = [0, mapWidth * zoom]
     */
    private double clampOffsetX(double ox) {
        double scaledMapWidth = mapWidth * zoom;
        // 如果缩放后地图比视口小，居中显示
        if (scaledMapWidth <= viewportWidth) {
            return (viewportWidth - scaledMapWidth) / 2;
        }
        // 否则约束边界
        double minOffset = viewportWidth - scaledMapWidth; // 左边界
        double maxOffset = 0;                               // 右边界
        return Math.max(minOffset, Math.min(maxOffset, ox));
    }

    /**
     * 约束垂直偏移：不允许拖出地图边界
     */
    private double clampOffsetY(double oy) {
        double scaledMapHeight = mapHeight * zoom;
        // 如果缩放后地图比视口小，居中显示
        if (scaledMapHeight <= viewportHeight) {
            return (viewportHeight - scaledMapHeight) / 2;
        }
        // 否则约束边界
        double minOffset = viewportHeight - scaledMapHeight;
        double maxOffset = 0;
        return Math.max(minOffset, Math.min(maxOffset, oy));
    }

    // ---- Getter ----

    public StackPane getPane() {
        return camera;
    }

    public double getOffsetX() {
        return offsetX;
    }

    public double getOffsetY() {
        return offsetY;
    }

    public double getZoom() {
        return zoom;
    }

    /**
     * 更新地图逻辑尺寸（例如切换地图时调用）
     */
    public void setMapSize(double width, double height) {
        this.mapWidth = width;
        this.mapHeight = height;
        clampAndPublish();
    }
}
