package game.window;

import config.CameraConfig;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.ScrollEvent;
import javafx.scene.layout.StackPane;
import post.ui.MapDraggedEvent;
import post.ui.MapScrolledEvent;
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
//    逻辑：滚轮缩放、右键拖拽移动
//    缩放、移动记录需要缩放及移动的大小与距离
//    进行边界约束、缩放限制检测
//    判断是否需要进行实际移动与缩放
//    缩放与移动
public class Camera extends StackPane {
    
    
    // ---- 视窗状态（从配置注入初始值）----\
    //地图对镜头的偏移量
    public static double offsetX;
    public static double offsetY;
    public static double zoom;
    // ---- 缩放限制（从配置注入）----
    private double minZoom;
    private double maxZoom;

    // ---- 缩放步进（从配置注入）----
    private double ZOOM_STEP;

    // ---- 视口尺寸（camera 自身尺寸 = 窗口尺寸）----
    private double viewportWidth;
    private double viewportHeight;

    // ---- 拖拽状态 ----
    private double dragStartX;
    private double dragStartY;
    private double dragStartOffsetX;
    private double dragStartOffsetY;
    private boolean isDragging = false;

    public Camera(CameraConfig config, double width, double height) {
        use(config);
        viewportWidth = width;
        viewportHeight = height;
        initCameraPane();
        initInputHandlers();
        initViewportListener();
    }
    public void use(CameraConfig config){
        // ---- 配置 ----
        offsetX = config.offsetX;
        offsetY = config.offsetY;
        zoom = config.zoom;
        this.minZoom = config.minZoom;
        this.maxZoom = config.maxZoom;
        this.ZOOM_STEP = config.ZOOM_STEP;
    }
    private void initCameraPane() {
        // camera 透明，不拦截背景绘制，但拦截鼠标事件
        setPickOnBounds(true);
        // camera 尺寸始终等于窗口尺寸
        PaneSizeManager.add(this, 1);
        bus.subscribe(StageSizeChange.class, (StageSizeChange event) -> {
            viewportWidth = event.width();
            viewportHeight = event.height();
        });
    }
    private void initViewportListener() {
        // 监听窗口大小变化，更新视口尺寸
        bus.subscribe(StageSizeChange.class, (StageSizeChange event) -> {
            if (event.width()==0||event.height()==0) return;
            offsetX=offsetX*event.width()/viewportWidth;
            offsetY=offsetY*event.height()/viewportHeight;
            viewportWidth = event.width();
            viewportHeight = event.height();
            // 窗口变化后，重新约束偏移（可能需要回弹）
            clampAndPublish();
        });
    }
    private void initInputHandlers() {
        bus.subscribe(post.camera.Scrolled.class, e -> cameraScrolled(e.event()));
        bus.subscribe(post.camera.Pressed.class, e -> cameraPressed(e.event()));
        bus.subscribe(post.camera.Dragged.class, e->cameraDragged(e.event()));
        bus.subscribe(post.camera.Released.class, e->cameraReleased(e.event()));
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
            double dx = event.getSceneX() - 0.5* viewportWidth - dragStartX;
            double dy = event.getSceneY() - 0.5* viewportHeight - dragStartY;
            
            offsetX = dragStartOffsetX + dx;
            offsetY = dragStartOffsetY + dy;
            clampAndPublish();
        }
    }
    
    private void cameraPressed(MouseEvent event) {
        if (event.getButton() == MouseButton.SECONDARY) {
            dragStartX = event.getSceneX() -0.5*  viewportWidth;
            dragStartY = event.getSceneY() - 0.5* viewportHeight;
            dragStartOffsetX = offsetX;
            dragStartOffsetY = offsetY;
            isDragging = true;
        }
    }
    
    private void cameraScrolled(ScrollEvent event) {
        double delta = event.getDeltaY() > 0 ? ZOOM_STEP : 1 / ZOOM_STEP;
        double newZoom = Math.max(minZoom, Math.min(maxZoom, zoom * delta));
        if (newZoom == zoom) return; // 缩放无变化，不发送事件
        // 以鼠标位置为中心缩放
        double mouseX = event.getSceneX() - 0.5 * viewportWidth;
        double mouseY = event.getSceneY() - 0.5 * viewportHeight;
        
        // 计算缩放前鼠标指向的地图坐标
        double mapPointX = (mouseX - offsetX) / zoom;
        double mapPointY = (mouseY - offsetY) / zoom;
        // 更新缩放
        zoom = newZoom;
        
        // 缩放后，让同一地图坐标仍在鼠标位置下
        offsetX = -mapPointX * zoom + mouseX;
        offsetY = -mapPointY * zoom + mouseY;
        
        clampAndPublish_Scrolled();
    }
    
    private void clampAndPublish_Scrolled() {
        offsetX = clampOffsetX(offsetX);
        offsetY = clampOffsetY(offsetY);
        bus.publish(new MapScrolledEvent(offsetX, offsetY, zoom));
    }
    
    /**
     * 约束偏移量到合法范围，并在状态有变化时发布事件
     */
    private void clampAndPublish() {
        offsetX = clampOffsetX(offsetX);
        offsetY = clampOffsetY(offsetY);
        bus.publish(new MapDraggedEvent(offsetX, offsetY));
    }

    // ---- 约束计算 ----
    

    /**
     * 约束水平偏移：不允许拖出地图边界
     * 地图可视范围 = [offsetX - 0.5 * viewportWidth, offsetX + 0.5 * viewportWidth]
     * 地图实际范围 = [-0.5*mapWidth * zoom, 0.5*mapWidth * zoom]
     */
    private double clampOffsetX(double ox) {
        double limitWidth = 0.5*viewportWidth*(zoom-1);
        return Math.max(-limitWidth, Math.min(limitWidth, ox));
    }

    /**
     * 约束垂直偏移：不允许拖出地图边界
     */
    private double clampOffsetY(double oy) {
        double limitHeight = 0.5*viewportHeight *(zoom-1);
        return Math.max(-limitHeight, Math.min(limitHeight, oy));
    }
    
}
