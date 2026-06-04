package game.window;

import config.MiniMapConfig;
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

    // ---- 配置 ----
    private MiniMapConfig config;

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

    public MiniMap(MiniMapConfig config, double viewportWidth, double viewportHeight) {
        this.config = config;
        this.viewportWidth = viewportWidth;
        this.viewportHeight = viewportHeight;

        // 小地图尺寸
        this.miniMapWidth = viewportWidth * config.sizeRatio;
        this.miniMapHeight = viewportHeight * config.sizeRatio;

        setPickOnBounds(true);
        setMaxSize(miniMapWidth, miniMapHeight);
        setPrefSize(miniMapWidth, miniMapHeight);
        setMinSize(miniMapWidth, miniMapHeight);

        // 背景
        setStyle("-fx-background-color: " + config.bgColor + ";"
                + " -fx-border-color: " + config.borderColor + ";"
                + " -fx-border-width: " + config.borderWidth + ";"
                + " -fx-border-radius: " + config.borderRadius + ";"
                + " -fx-background-radius: " + config.borderRadius + ";");

        // 地图缩略图
        ImageView thumb = new ImageView(ImageManager.load(config.thumbImagePath));
        thumb.setPreserveRatio(false);
        thumb.setSmooth(true);
        thumb.fitWidthProperty().bind(widthProperty());
        thumb.fitHeightProperty().bind(heightProperty());
        thumb.setOpacity(config.thumbOpacity);
        getChildren().add(thumb);

        // 视口矩形框
        viewportRect = new Rectangle();
        viewportRect.setFill(Color.TRANSPARENT);
        viewportRect.setStroke(Color.valueOf(config.viewportStrokeColor));
        viewportRect.setStrokeWidth(config.viewportStrokeWidth);
        viewportRect.setOpacity(config.viewportOpacity);
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
        setTranslateX(parentWidth / 2 - miniMapWidth / 2 - config.margin);
        setTranslateY(-parentHeight / 2 + miniMapHeight / 2 + config.margin);
    }

    private void onMapTransform(MapTransformEvent e) {
        currentOffsetX = e.offsetX();
        currentOffsetY = e.offsetY();
        currentZoom = e.zoom();
        updateViewportRect();
    }

    private void onStageResize(StageSizeChange e) {
        viewportWidth = e.width();
        viewportHeight = e.height();
        miniMapWidth = viewportWidth * config.sizeRatio;
        miniMapHeight = viewportHeight * config.sizeRatio;
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
        viewportRect.setWidth(Math.max(config.viewportMinSize, rectW));
        viewportRect.setHeight(Math.max(config.viewportMinSize, rectH));
    }

    /**
     * 应用新配置（运行时热更新）
     */
    public void applyConfig(MiniMapConfig newConfig) {
        // 更新配置引用
        this.config = newConfig;

        // 更新小地图尺寸
        miniMapWidth = viewportWidth * config.sizeRatio;
        miniMapHeight = viewportHeight * config.sizeRatio;
        setMaxSize(miniMapWidth, miniMapHeight);
        setPrefSize(miniMapWidth, miniMapHeight);
        setMinSize(miniMapWidth, miniMapHeight);

        // 更新背景样式
        setStyle("-fx-background-color: " + config.bgColor + ";"
                + " -fx-border-color: " + config.borderColor + ";"
                + " -fx-border-width: " + config.borderWidth + ";"
                + " -fx-border-radius: " + config.borderRadius + ";"
                + " -fx-background-radius: " + config.borderRadius + ";");

        // 更新缩略图
        if (getChildren().get(0) instanceof ImageView thumb) {
            thumb.setImage(ImageManager.load(config.thumbImagePath));
            thumb.setOpacity(config.thumbOpacity);
        }

        // 更新视口矩形框样式
        viewportRect.setStroke(Color.valueOf(config.viewportStrokeColor));
        viewportRect.setStrokeWidth(config.viewportStrokeWidth);
        viewportRect.setOpacity(config.viewportOpacity);

        // 重新定位和更新
        reposition();
        updateViewportRect();
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
