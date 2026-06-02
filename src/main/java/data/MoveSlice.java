package data;

import game.window.Camera;
import javafx.scene.input.MouseEvent;
import post.ui.MapDraggedEvent;
import post.ui.MapScrolledEvent;

public class MoveSlice extends Slice {
    
    public MoveSlice() {
        super();
        game.Game.bus.subscribe(MapDraggedEvent.class, this::onMapTransform);
        game.Game.bus.subscribe(MapScrolledEvent.class, e->moveTo(mapToTraX(mapX.doubleValue()), mapToTraY(mapY.doubleValue())));
    }
    
    public MoveSlice(double X, double Y) {
        super(X, Y);
        game.Game.bus.subscribe(MapDraggedEvent.class, this::onMapTransform);
        game.Game.bus.subscribe(MapScrolledEvent.class, e->moveTo(mapToTraX(mapX.doubleValue()), mapToTraY(mapY.doubleValue())));
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
