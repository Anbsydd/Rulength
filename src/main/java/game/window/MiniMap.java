package game.window;

import event.MapTransformEvent;
import event.StageSizeChange;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import util.ImageManager;

import static game.Game.bus;

/**
 * 小地图组件
 *
 * 固定在右上角，显示整张地图的缩略图，
 * 并用矩形框标示当前视口可见区域。
 * 点击小地图可快速跳转到对应位置。
 */
public class MiniMap extends StackPane {

    // ---- 小地图尺寸 ----
    private double miniMapWidth;
    private double miniMapHeight;

    // ---- 视口矩形 ----
    private final Rectangle viewportRect;

    // ---- 视口尺寸（窗口尺寸）----
    private double viewportWidth;
    private double viewportHeight;

    // ---- 当前相机状态 ----
    private double currentOffsetX;
    private double currentOffsetY;
    private double currentZoom;

    // ---- 边距 ----
    private static final double MARGIN = 12;

    public MiniMap(double viewportWidth, double viewportHeight) {
        this.viewportWidth = viewportWidth;
        this.viewportHeight = viewportHeight;

        // 小地图尺寸：视口的 1/4
        this.miniMapWidth = viewportWidth / 4;
        this.miniMapHeight = viewportHeight / 4;

        setPickOnBounds(true);
        setMaxSize(miniMapWidth, miniMapHeight);
        setPrefSize(miniMapWidth, miniMapHeight);
        setMinSize(miniMapWidth, miniMapHeight);

        // 背景
        setStyle("-fx-background-color: rgba(0, 0, 0, 0.5); -fx-border-color: rgba(255, 255, 255, 0.6); -fx-border-width: 1; -fx-border-radius: 4; -fx-background-radius: 4;");

        // 地图缩略图
        ImageView thumb = new ImageView(ImageManager.load("uiImages/backgrounds/map.png"));
        thumb.setPreserveRatio(false);
        thumb.setSmooth(true);
        thumb.fitWidthProperty().bind(widthProperty());
        thumb.fitHeightProperty().bind(heightProperty());
        thumb.setOpacity(0.8);
        getChildren().add(thumb);

        // 视口矩形框
        viewportRect = new Rectangle();
        viewportRect.setFill(Color.TRANSPARENT);
        viewportRect.setStroke(Color.WHITE);
        viewportRect.setStrokeWidth(1.5);
        viewportRect.setOpacity(0.9);
        viewportRect.setManaged(false);
        getChildren().add(viewportRect);

        // 订阅地图变换事件
        bus.subscribe(MapTransformEvent.class, this::onMapTransform);
        bus.subscribe(StageSizeChange.class, this::onStageResize);

        // 点击小地图跳转
        setOnMouseClicked(e -> {
            double clickX = e.getX();
            double clickY = e.getY();
            jumpToMiniMapPos(clickX, clickY);
        });
    }

    /**
     * 定位到右上角
     */
    public void reposition() {
        // StackPane 中子节点默认居中，translateX/Y 是相对居中位置的偏移
        double parentWidth = getParent() != null ? ((javafx.scene.layout.Pane) getParent()).getWidth() : viewportWidth;
        double parentHeight = getParent() != null ? ((javafx.scene.layout.Pane) getParent()).getHeight() : viewportHeight;
        // 居中位置偏移到右上角：向右 (parentWidth/2 - miniMapWidth/2 - MARGIN)，向上 -(parentHeight/2 - miniMapHeight/2 - MARGIN)
        setTranslateX(parentWidth / 2 - miniMapWidth / 2 - MARGIN);
        setTranslateY(-parentHeight / 2 + miniMapHeight / 2 + MARGIN);
    }

    private void onMapTransform(MapTransformEvent e) {
        currentOffsetX = e.offsetX();
        currentOffsetY = e.offsetY();
        currentZoom = e.zoom();
        System.out.println("[MiniMap] onMapTransform: offsetX=" + currentOffsetX + ", offsetY=" + currentOffsetY + ", zoom=" + currentZoom);
        updateViewportRect();
    }

    private void onStageResize(StageSizeChange e) {
        viewportWidth = e.width();
        viewportHeight = e.height();
        miniMapWidth = viewportWidth / 4;
        miniMapHeight = viewportHeight / 4;
        setMaxSize(miniMapWidth, miniMapHeight);
        setPrefSize(miniMapWidth, miniMapHeight);
        setMinSize(miniMapWidth, miniMapHeight);
        reposition();
        updateViewportRect();
    }

    /**
     * 更新视口矩形框的位置和大小
     *
     * 变换公式（与 moveWithMap 一致）：
     *   setTranslateX(offsetX), setScaleX(zoom)
     *   StackPane 中节点居中，缩放以中心为原点：
     *   场景坐标 = (本地坐标 - mapW/2) * zoom + mapW/2 + offsetX
     *   反推：本地坐标 = (场景坐标 - mapW/2 - offsetX) / zoom + mapW/2
     */
    private void updateViewportRect() {
        if (currentZoom <= 0) return;

        double mapW = viewportWidth;
        double mapH = viewportHeight;

        // 视口左上角场景坐标 (0, 0) → map 本地坐标
        double localLeft = (0 - mapW / 2.0 - currentOffsetX) / currentZoom + mapW / 2.0;
        double localTop = (0 - mapH / 2.0 - currentOffsetY) / currentZoom + mapH / 2.0;

        // 视口右下角场景坐标 (viewportWidth, viewportHeight) → map 本地坐标
        double localRight = (viewportWidth - mapW / 2.0 - currentOffsetX) / currentZoom + mapW / 2.0;
        double localBottom = (viewportHeight - mapH / 2.0 - currentOffsetY) / currentZoom + mapH / 2.0;

        // map 本地坐标 [0, mapW] → 小地图坐标 [0, miniMapWidth]
        double rectX = localLeft / mapW * miniMapWidth;
        double rectY = localTop / mapH * miniMapHeight;
        double rectW = (localRight - localLeft) / mapW * miniMapWidth;
        double rectH = (localBottom - localTop) / mapH * miniMapHeight;

        // 限制矩形不超出小地图边界
        rectX = Math.max(0, Math.min(miniMapWidth, rectX));
        rectY = Math.max(0, Math.min(miniMapHeight, rectY));
        rectW = Math.min(rectW, miniMapWidth - rectX);
        rectH = Math.min(rectH, miniMapHeight - rectY);

        viewportRect.setLayoutX(rectX);
        viewportRect.setLayoutY(rectY);
        viewportRect.setWidth(Math.max(2, rectW));
        viewportRect.setHeight(Math.max(2, rectH));
    }

    /**
     * 点击小地图跳转到对应位置
     */
    private void jumpToMiniMapPos(double clickX, double clickY) {
        double mapW = viewportWidth;
        double mapH = viewportHeight;

        // 小地图坐标 → map 本地坐标
        double localX = (clickX / miniMapWidth) * mapW;
        double localY = (clickY / miniMapHeight) * mapH;

        // map 本地坐标 → 场景坐标
        double sceneX = (localX - mapW / 2.0) * currentZoom + mapW / 2.0 + currentOffsetX;
        double sceneY = (localY - mapH / 2.0) * currentZoom + mapH / 2.0 + currentOffsetY;

        // 要使该场景点移动到视口中心，需要调整 offset
        // 目标：sceneX + deltaOffset = viewportWidth/2  =>  deltaOffset = viewportWidth/2 - sceneX
        double targetOffsetX = currentOffsetX + (viewportWidth / 2.0 - sceneX);
        double targetOffsetY = currentOffsetY + (viewportHeight / 2.0 - sceneY);

        Camera.jumpTo(targetOffsetX, targetOffsetY);
    }
}
