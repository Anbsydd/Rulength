package data;

import game.window.Camera;
import javafx.scene.input.MouseEvent;
import post.ui.MapDraggedEvent;

public class StaticSlice extends Slice{

    public StaticSlice() {
        super();
    }
    public StaticSlice(double x, double y) {
        super(x, y);
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
    
    private void onMapTransform(MapDraggedEvent m) {
        setTranslateX(mapToTraX(mapX.doubleValue()));
        setTranslateY(mapToTraY(mapY.doubleValue()));
    }
}
