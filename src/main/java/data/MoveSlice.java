package data;

import game.window.Camera;
import javafx.scene.input.MouseEvent;
import post.ui.MapDraggedEvent;
import post.ui.MapScrolledEvent;

import static game.Game.bus;

public class MoveSlice extends Slice {
    double lastTraX;
    double lastTraY;
    double lastMouseX;
    double lastMouseY;
    public MoveSlice() {
        super();
        bus.subscribe(MapDraggedEvent.class, this::onMapTransform);
        bus.subscribe(MapScrolledEvent.class, e->{
            moveTo(mapToTraX(mapX.doubleValue()), mapToTraY(mapY.doubleValue()));
        });
    }
    
    public MoveSlice(double X, double Y) {
        super(X, Y);
        bus.subscribe(MapDraggedEvent.class, this::onMapTransform);
        bus.subscribe(MapScrolledEvent.class, e->{
            isDragging = false;
            moveTo(mapToTraX(mapX.doubleValue()), mapToTraY(mapY.doubleValue()));
        });
    }
    
    @Override
    protected void pressed(MouseEvent e) {
        isDragging = true;
        // 记录鼠标地图坐标与Slice地图坐标之间的偏移量
        recordXAY(e);
    }
    private void recordXAY(MouseEvent e) {
        lastMouseX = e.getSceneX();
        lastMouseY = e.getSceneY();
        lastTraX = getTranslateX();
        lastTraY = getTranslateY();
    }
    
    protected void released() {
        // 保存最终的地图坐标（Slice的translateX/Y就是Move局部坐标=地图坐标）
        setMapX(traToMapX(getTranslateX()));
        setMapY(traToMapY(getTranslateY()));
        isDragging = false;
    }
    
    @Override
    protected void dragged(MouseEvent e) {
        if (!isDragging) return;
        double currentX = e.getSceneX();
        double currentY = e.getSceneY();
        
        // 使用偏移量计算新位置，避免缩放后跳变
        setMapX(traToMapX((currentX - lastMouseX)+lastTraX));
        setMapY(traToMapY((currentY - lastMouseY)+lastTraY));
    }
    
    
    private void onMapTransform(MapDraggedEvent m) {
        setTranslateX(mapToTraX(mapX.doubleValue()));
        setTranslateY(mapToTraY(mapY.doubleValue()));
    }
    @Override
    public double traToMapX(double x){
        return Camera.traToMapX(x);
    }
    @Override
    public double traToMapY(double y){
        return Camera.traToMapY(y);
    }
    @Override
    public double mapToTraX(double x){
        return Camera.mapToTraX(x);
    }
    @Override
    public double mapToTraY(double y){
        return Camera.mapToTraY(y);
    }
}
