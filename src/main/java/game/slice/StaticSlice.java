package game.slice;

import event.MapTransformEvent;
import event.StageSizeChange;
import javafx.scene.input.MouseEvent;

import static game.Game.bus;
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
    protected double isMoveSlice(){
        return zoom;
    }

    @Override
    public double finalTraToMapX(double traX) {
        return traX / game.Game.multiX;
    }

    @Override
    public double finalTraToMapY(double traY) {
        return traY / game.Game.multiY;
    }

    @Override
    public double finalMapToTraX(double mapX) {
        return mapX * game.Game.multiX;
    }

    @Override
    public double finalMapToTraY(double mapY) {
        return mapY * game.Game.multiY;
    }
}