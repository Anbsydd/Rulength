package game.slice;

import event.MapTransformEvent;
import event.StageSizeChange;
import javafx.scene.input.MouseEvent;

import static core.CoreAPI.bus;
import static game.window.Camera.zoom;

public abstract class StaticSlice extends Slice {

    public StaticSlice() {
        super();
        bus.subscribe(MapTransformEvent.class, e -> {
            released();
            reloadTra();
        });
        bus.subscribe(StageSizeChange.class, e -> {
            released();
            reloadTra();
        });
    }

    public StaticSlice(double x, double y) {
        super(x, y);
        bus.subscribe(MapTransformEvent.class, e -> {
            released();
            reloadTra();
        });
        bus.subscribe(StageSizeChange.class, e -> {
            released();
            reloadTra();
        });
    }

    @Override
    protected void pressed(MouseEvent e) {
        super.pressed(e);
        if (isDragging) {
            game.window.Camera.freeze();
        }
    }

    @Override
    public double isMoveSlice(){
        return zoom;
    }

    @Override
    public double finalTraToMapX(double traX) {
        return traX / core.CoreAPI.multiX;
    }

    @Override
    public double finalTraToMapY(double traY) {
        return traY / core.CoreAPI.multiY;
    }

    @Override
    public double finalMapToTraX(double mapX) {
        return mapX * core.CoreAPI.multiX;
    }

    @Override
    public double finalMapToTraY(double mapY) {
        return mapY * core.CoreAPI.multiY;
    }
}