package data;

import javafx.scene.input.MouseEvent;
import post.ui.MapScrolledEvent;
import post.ui.StageSizeChange;

import static game.Game.bus;
import static game.window.Camera.zoom;

public class StaticSlice extends Slice{
    
    public StaticSlice() {
        super();
        bus.subscribe(MapScrolledEvent.class, e-> {
            released();
        });
        bus.subscribe(StageSizeChange.class, e-> {
            released();
            setTranslateX(finalMapToTraX(mapX.doubleValue()));
            setTranslateY(finalMapToTraY(mapY.doubleValue()));
        });
    }
    public StaticSlice(double x, double y) {
        super(x, y);
        bus.subscribe(MapScrolledEvent.class, e-> {
            released();
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
        setMapX(finalTraToMapX((currentX - lastMouseX)/zoom+lastTraX));
        setMapY(finalTraToMapY((currentY - lastMouseY)/zoom+lastTraY));
    }
    
    @Override
    public double finalTraToMapX(double traX) {
        return traX/game.Game.multiX;
    }
    @Override
    public double finalTraToMapY(double traY) {
        return traY/game.Game.multiY;
    }
    @Override
    public double finalMapToTraX(double mapX) {
        return mapX*game.Game.multiX;
    }
    @Override
    public double finalMapToTraY(double mapY) {
        return mapY*game.Game.multiY;
    }
}
