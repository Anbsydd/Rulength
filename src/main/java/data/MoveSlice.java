package data;

import game.window.Camera;
import javafx.scene.input.MouseEvent;
import post.ui.MapDraggedEvent;
import post.ui.MapScrolledEvent;

import static game.Game.bus;
import static game.window.Camera.zoom;

public class MoveSlice extends Slice {
    double lastTraX;
    double lastTraY;
    public MoveSlice() {
        super();
        bus.subscribe(MapDraggedEvent.class, this::onMapTransform);
        bus.subscribe(MapScrolledEvent.class, e->moveTo(mapToTraX(mapX.doubleValue()), mapToTraY(mapY.doubleValue())));
    }
    
    public MoveSlice(double X, double Y) {
        super(X, Y);
        bus.subscribe(MapDraggedEvent.class, this::onMapTransform);
        bus.subscribe(MapScrolledEvent.class, e->moveTo(mapToTraX(mapX.doubleValue()), mapToTraY(mapY.doubleValue())));
    }
    
    @Override
    protected void pressed(MouseEvent e) {
        isDragging = true;
        // 记录鼠标地图坐标与Slice地图坐标之间的偏移量
        recordXAY(e);
        bus.subscribe(MapScrolledEvent.class, e1-> {
            recordXAY(e);
        });
    }
    private void recordXAY(MouseEvent e) {
        dragOffsetX = e.getSceneX();
        dragOffsetY = e.getSceneY();
        lastTraX = getTranslateX();
        lastTraY = getTranslateY();
    }
    
    @Override
    protected void released(MouseEvent e) {
        
        // 保存最终的地图坐标（Slice的translateX/Y就是Move局部坐标=地图坐标）
        setMapX(getTranslateX());
        setMapY(getTranslateY());
        isDragging = false;
        System.out.println("release:"+e.getSceneX());
    }
    
    @Override
    protected void dragged(MouseEvent e) {
        
        if (!isDragging) return;
        double currentX = e.getSceneX();
        double currentY = e.getSceneY();
        
        // 使用偏移量计算新位置，避免缩放后跳变
        mapX.set((currentX - dragOffsetX)/zoom+lastTraX);
        mapY.set((currentY - dragOffsetY)/zoom+lastTraY);
        System.out.println("drag:mouse,"+e.getSceneX());
        System.out.println("drag:map,"+mapX.get());
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
