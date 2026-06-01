package data;

public class StaticSlice extends Slice{

    public StaticSlice() {
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
}
