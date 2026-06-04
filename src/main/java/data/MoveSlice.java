package data;

import game.window.Camera;
import javafx.scene.input.MouseEvent;
import post.ui.MapTransformEvent;
import post.ui.StageSizeChange;

import static game.Game.bus;
import static game.window.Camera.zoom;

public class MoveSlice extends Slice {
    public MoveSlice() {
        super();
        bus.subscribe(MapTransformEvent.class, e->{
            reloadTra();
        });
        bus.subscribe(StageSizeChange.class, e-> {
            released();
            setTranslateX(finalMapToTraX(mapX.doubleValue()));
            setTranslateY(finalMapToTraY(mapY.doubleValue()));
        });
    }
    
    public MoveSlice(double X, double Y) {
        super(X, Y);
        bus.subscribe(MapTransformEvent.class, e->{
            reloadTra();
        });
        bus.subscribe(StageSizeChange.class, e-> {
            released();
            setTranslateX(finalMapToTraX(mapX.doubleValue()));
            setTranslateY(finalMapToTraY(mapY.doubleValue()));
        });
    }
    @Override
    protected void dragged(MouseEvent e) {
        if (!isDragging) return;
        double currentX = e.getSceneX();
        double currentY = e.getSceneY();
        // 使用偏移量计算新位置，避免缩放后跳变
        setMapX(finalTraToMapX((currentX - lastMouseX)+lastTraX));
        setMapY(finalTraToMapY((currentY - lastMouseY)+lastTraY));
    }
    
    

    @Override
    public double finalTraToMapX(double traX) {
        return (traX-Camera.offsetX)/zoom/game.Game.multiX;
    }
    
    @Override
    public double finalTraToMapY(double traY) {
        return (traY-Camera.offsetY)/zoom/game.Game.multiY;
    }
    
    @Override    // 重写父类的finalMapToTraX方法
    public double finalMapToTraX(double mapX) {    // 将地图坐标X转换为轨迹坐标X，参数为地图坐标X，返回轨迹坐标X
        return mapX*zoom*game.Game.multiX+Camera.offsetX;
    }
    
    @Override
    public double finalMapToTraY(double mapY) {
        return mapY*zoom*game.Game.multiY+Camera.offsetY;
    }
}
