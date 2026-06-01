package data;

import game.window.Camera;
import post.ui.MapTransformEvent;

public class MoveSlice extends Slice {
    
    public MoveSlice() {
        super();
        game.Game.bus.subscribe(MapTransformEvent.class, this::onMapTransform);
    }
    
    public MoveSlice(double X, double Y) {
        super(X, Y);
        game.Game.bus.subscribe(MapTransformEvent.class, this::onMapTransform);
    }
    
    private void onMapTransform(MapTransformEvent mapTransformEvent) {
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
