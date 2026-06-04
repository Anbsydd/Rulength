package game.window;

import config.CameraConfig;
import event.MapTransformEvent;
import event.StageSizeChange;
import javafx.animation.AnimationTimer;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.ScrollEvent;
import javafx.scene.layout.StackPane;
import util.PaneSizeManager;

import static game.Game.bus;

/**
 * Camera 视窗层
 *
 * 透明 StackPane，覆盖在 Map 上方，固定不动。
 * 拦截所有鼠标/滚轮输入事件，维护视窗状态（偏移、缩放），
 * 当状态发生变化时通过 EventBus 发布 MapTransformEvent。
 *
 * 采用目标状态 + 渲染状态双缓冲：
 *   - 拖拽/缩放操作修改目标状态（targetOffsetX/Y, targetZoom）
 *   - AnimationTimer 每帧将渲染状态向目标插值
 *   - 每帧统一发布渲染状态，确保平滑过渡
 *
 * 层级结构：
 *   root
 *     ├── map   (StackPane) — 地图背景，被平移/缩放
 *     └── camera (StackPane) — 本类，接收指令，不移动
 */
public class Camera extends StackPane {

    // ---- 目标状态（输入操作直接修改）----
    private double targetOffsetX;
    private double targetOffsetY;
    private double targetZoom;

    // ---- 渲染状态（AnimationTimer 插值后的实际显示值）----
    public static double offsetX;
    public static double offsetY;
    public static double zoom;

    // ---- 缩放限制（从配置注入）----
    private double minZoom;
    private double maxZoom;

    // ---- 缩放步进（从配置注入）----
    private double ZOOM_STEP;

    // ---- 插值速度（从配置注入）----
    private double lerpDrag;
    private double lerpZoom;

    // ---- 视口尺寸（camera 自身尺寸 = 窗口尺寸）----
    private double viewportWidth;
    private double viewportHeight;

    // ---- 拖拽状态 ----
    private double dragStartX;
    private double dragStartY;
    private double dragStartOffsetX;
    private double dragStartOffsetY;
    private boolean isDragging = false;

    // ---- 单例引用 ----
    private static Camera instance;

    // ---- 动画计时器 ----
    private final AnimationTimer renderLoop;

    public Camera(CameraConfig config, double width, double height) {
        instance = this;
        use(config);
        viewportWidth = width;
        viewportHeight = height;
        // 初始化：渲染状态 = 目标状态
        offsetX = targetOffsetX;
        offsetY = targetOffsetY;
        zoom = targetZoom;

        initCameraPane();
        initInputHandlers();
        initViewportListener();

        // 启动渲染循环：每帧插值并发布事件
        renderLoop = new AnimationTimer() {
            @Override
            public void handle(long now) {
                lerpAndPublish();
            }
        };
        renderLoop.start();
    }

    public void use(CameraConfig config) {
        targetOffsetX = config.offsetX;
        targetOffsetY = config.offsetY;
        targetZoom = config.zoom;
        this.minZoom = config.minZoom;
        this.maxZoom = config.maxZoom;
        this.ZOOM_STEP = config.ZOOM_STEP;
        this.lerpDrag = config.lerpDrag;
        this.lerpZoom = config.lerpZoom;
    }

    private void initCameraPane() {
        setPickOnBounds(true);
        PaneSizeManager.add(this, 1);
        bus.subscribe(StageSizeChange.class, (StageSizeChange event) -> {
            viewportWidth = event.width();
            viewportHeight = event.height();
        });
    }

    private void initViewportListener() {
        bus.subscribe(StageSizeChange.class, (StageSizeChange event) -> {
            if (event.width() == 0 || event.height() == 0) return;
            targetOffsetX = targetOffsetX * event.width() / viewportWidth;
            targetOffsetY = targetOffsetY * event.height() / viewportHeight;
            viewportWidth = event.width();
            viewportHeight = event.height();
            clampTarget();
        });
    }

    private void initInputHandlers() {
        bus.subscribe(event.input.MouseScrolled.class, e -> cameraScrolled(e.event()));
        bus.subscribe(event.input.MousePressed.class, e -> cameraPressed(e.event()));
        bus.subscribe(event.input.MouseDragged.class, e -> cameraDragged(e.event()));
        bus.subscribe(event.input.MouseReleased.class, e -> cameraReleased(e.event()));
        addEventFilter(ScrollEvent.ANY, this::cameraScrolled);
        addEventFilter(MouseEvent.MOUSE_PRESSED, this::cameraPressed);
        addEventFilter(MouseEvent.MOUSE_DRAGGED, this::cameraDragged);
        addEventFilter(MouseEvent.MOUSE_RELEASED, this::cameraReleased);
    }

    private void cameraReleased(MouseEvent event) {
        if (event.getButton() == MouseButton.SECONDARY) {
            isDragging = false;
        }
    }

    private void cameraDragged(MouseEvent event) {
        if (event.getButton() == MouseButton.SECONDARY) {
            if (!isDragging) return;
            double currentX = event.getSceneX() - 0.5 * viewportWidth;
            double currentY = event.getSceneY() - 0.5 * viewportHeight;

            targetOffsetX = dragStartOffsetX + (currentX - dragStartX);
            targetOffsetY = dragStartOffsetY + (currentY - dragStartY);
            clampTarget();
        }
    }

    private void cameraPressed(MouseEvent event) {
        if (event.getButton() == MouseButton.SECONDARY) {
            freeze();
            dragStartX = event.getSceneX() - 0.5 * viewportWidth;
            dragStartY = event.getSceneY() - 0.5 * viewportHeight;
            dragStartOffsetX = targetOffsetX;
            dragStartOffsetY = targetOffsetY;
            isDragging = true;
        }
    }

    /**
     * 跳转到指定偏移位置（保持当前缩放不变）
     */
    public static void jumpTo(double offsetX, double offsetY) {
        if (instance != null) {
            instance.targetOffsetX = Math.max(-0.5 * instance.viewportWidth * (instance.targetZoom - 1),
                    Math.min(0.5 * instance.viewportWidth * (instance.targetZoom - 1), offsetX));
            instance.targetOffsetY = Math.max(-0.5 * instance.viewportHeight * (instance.targetZoom - 1),
                    Math.min(0.5 * instance.viewportHeight * (instance.targetZoom - 1), offsetY));
        }
    }

    /**
     * 冻结动画：将目标状态同步到当前渲染状态，使动画停在当前位置
     */
    public static void freeze() {
        if (instance != null) {
            instance.targetOffsetX = instance.offsetX;
            instance.targetOffsetY = instance.offsetY;
            instance.targetZoom = instance.zoom;
        }
    }

    private void cameraScrolled(ScrollEvent event) {
        double delta = event.getDeltaY() > 0 ? ZOOM_STEP : 1 / ZOOM_STEP;
        double newZoom = Math.max(minZoom, Math.min(maxZoom, targetZoom * delta));
        if (newZoom == targetZoom) return;

        double mouseX = event.getSceneX() - 0.5 * viewportWidth;
        double mouseY = event.getSceneY() - 0.5 * viewportHeight;

        // 以鼠标位置为中心计算新的目标偏移
        double mapPointX = (mouseX - targetOffsetX) / targetZoom;
        double mapPointY = (mouseY - targetOffsetY) / targetZoom;
        targetZoom = newZoom;
        targetOffsetX = -mapPointX * targetZoom + mouseX;
        targetOffsetY = -mapPointY * targetZoom + mouseY;

        // 缩放时同步更新拖拽基准
        if (isDragging) {
            dragStartOffsetX = targetOffsetX;
            dragStartOffsetY = targetOffsetY;
            dragStartX = mouseX;
            dragStartY = mouseY;
        }

        clampTarget();
    }

    /**
     * 约束目标偏移量到合法范围
     */
    private void clampTarget() {
        targetOffsetX = clampOffsetX(targetOffsetX, targetZoom);
        targetOffsetY = clampOffsetY(targetOffsetY, targetZoom);
    }

    /**
     * 每帧插值：渲染状态向目标状态平滑过渡，并发布事件
     */
    private void lerpAndPublish() {
        // 偏移量：拖拽时即时跟随，非拖拽时平滑过渡
        double offsetLerp = isDragging ? lerpDrag : lerpZoom;
        // 缩放：始终平滑过渡，拖拽不会打断缩放动画
        double zoomLerp = lerpZoom;

        double newOffsetX = lerp(offsetX, targetOffsetX, offsetLerp);
        double newOffsetY = lerp(offsetY, targetOffsetY, offsetLerp);
        double newZoom = lerp(zoom, targetZoom, zoomLerp);

        // 如果已经非常接近目标，直接对齐（避免无限逼近）
        boolean changed = false;
        if (Math.abs(newOffsetX - offsetX) > 0.01) { offsetX = newOffsetX; changed = true; }
        else if (Math.abs(offsetX - targetOffsetX) > 0.01) { offsetX = targetOffsetX; changed = true; }
        if (Math.abs(newOffsetY - offsetY) > 0.01) { offsetY = newOffsetY; changed = true; }
        else if (Math.abs(offsetY - targetOffsetY) > 0.01) { offsetY = targetOffsetY; changed = true; }
        if (Math.abs(newZoom - zoom) > 0.0001) { zoom = newZoom; changed = true; }
        else if (Math.abs(zoom - targetZoom) > 0.0001) { zoom = targetZoom; changed = true; }

        if (changed) {
            // 渲染状态也需要约束
            offsetX = clampOffsetX(offsetX, zoom);
            offsetY = clampOffsetY(offsetY, zoom);
            bus.publish(new MapTransformEvent(offsetX, offsetY, zoom));
        }
    }

    /**
     * 线性插值
     */
    private static double lerp(double current, double target, double factor) {
        return current + (target - current) * factor;
    }

    // ---- 约束计算 ----

    private double clampOffsetX(double ox, double z) {
        double limitWidth = 0.5 * viewportWidth * (z - 1);
        return Math.max(-limitWidth, Math.min(limitWidth, ox));
    }

    private double clampOffsetY(double oy, double z) {
        double limitHeight = 0.5 * viewportHeight * (z - 1);
        return Math.max(-limitHeight, Math.min(limitHeight, oy));
    }
}
