package data;

import javafx.scene.input.MouseEvent;
import post.ui.MapScrolledEvent;

import static game.Game.bus;
import static game.window.Camera.zoom;

public class StaticSlice extends Slice{
    @Override
    void finalTraToMapX(double traX) {
    
    }
    
    @Override
    void finalTraToMapY(double traY) {
    
    }
    
    @Override
    void finalMapToTraX(double mapX) {
    
    }
    
    @Override
    void finalMapToTraY(double mapY) {
    
    }
    
    public StaticSlice() {
        super();
        bus.subscribe(MapScrolledEvent.class, e-> {
            released();
        });
    }
    public StaticSlice(double x, double y) {
        super(x, y);
        bus.subscribe(MapScrolledEvent.class, e-> {
            released();
        });
    }
    
    @Override
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
        mapX.set((currentX - lastMouseX)/zoom+lastTraX);
        mapY.set((currentY - lastMouseY)/zoom+lastTraY);
    }
    
    @Override
    public double traToMapX(double x) {
        return x;
    }
    
    @Override
    public double traToMapY(double y) {
        return y;
    }
    
    @Override
    public double mapToTraX(double x) {
        return x;
    }
    
    @Override
    public double mapToTraY(double y) {
        return y;
    }
    
}
