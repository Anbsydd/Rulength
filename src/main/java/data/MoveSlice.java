package data;

import game.window.Camera;

public abstract class MoveSlice extends Slice {
    
    public MoveSlice() {
    }
    public MoveSlice(double X, double Y) {
        super(X, Y);
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
