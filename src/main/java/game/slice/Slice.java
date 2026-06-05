package game.slice;

import event.MapTransformEvent;
import event.StageSizeChange;
import event.input.MouseDragged;
import event.input.MousePressed;
import event.input.MouseReleased;
import event.input.MouseScrolled;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.event.EventType;
import javafx.scene.control.Button;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.ScrollEvent;

import static game.Game.bus;

public abstract class Slice extends Button implements LifeCycled, TextSized, Coordinatable {
    protected boolean loaded = false;

    // 拖动相关字段
    protected double lastMouseX;
    protected double lastMouseY;
    protected double lastTraX;
    protected double lastTraY;
    protected DoubleProperty mapX;
    protected DoubleProperty mapY;
    boolean isDragging = false;
    public boolean canBeDragged = true;
    StringProperty name = new SimpleStringProperty("");

    public Slice() {
        super();
        // 取消按钮默认的焦点光环和按下发光效果
        setFocusTraversable(false);
        mapX = new SimpleDoubleProperty(0);
        mapY = new SimpleDoubleProperty(0);
        name.addListener((observable, oldValue, newValue) -> setText(newValue));
        mapX.addListener((observable, oldValue, newValue) -> {
            setTranslateX(finalMapToTraX(newValue.doubleValue()));
        });
        mapY.addListener((observable, oldValue, newValue) -> {
            setTranslateY(finalMapToTraY(newValue.doubleValue()));
        });
        bus.subscribe(MapTransformEvent.class, e -> {
            reloadTra();
        });
        bus.subscribe(StageSizeChange.class, e -> {
//            setSize();
        });
    }

    public Slice(double X, double Y) {
        this();
        setLocation(X, Y);
    }

    @Override
    public void load() {
        if (loaded) return;
        addFilters();
        loaded = true;
    }

    protected void addFilters() {
        EventHandler<ScrollEvent> scrolled = e -> bus.publish(new MouseScrolled(e));
        EventHandler<MouseEvent> pressed = e -> {
            if (e.getButton() == MouseButton.PRIMARY) pressed(e);
            if (e.getButton() == MouseButton.SECONDARY) bus.publish(new MousePressed(e));
        };
        EventHandler<MouseEvent> dragged = e -> {
            if (e.getButton() == MouseButton.PRIMARY) dragged(e);
            if (e.getButton() == MouseButton.SECONDARY) bus.publish(new MouseDragged(e));
        };
        EventHandler<MouseEvent> released = e -> {
            if (e.getButton() == MouseButton.PRIMARY) released();
            if (e.getButton() == MouseButton.SECONDARY) bus.publish(new MouseReleased(e));
        };
        addAndRegisterEventFilter(ScrollEvent.SCROLL, scrolled);
        addAndRegisterEventFilter(MouseEvent.MOUSE_PRESSED, pressed);
        addAndRegisterEventFilter(MouseEvent.MOUSE_DRAGGED, dragged);
        addAndRegisterEventFilter(MouseEvent.MOUSE_RELEASED, released);
    }

    protected void pressed(MouseEvent e) {
        isDragging = canBeDragged;
        recordXAY(e.getSceneX(), e.getSceneY());
    }

    protected void recordXAY(double MouseSceneX, double MouseSceneY) {
        lastMouseX = MouseSceneX;
        lastMouseY = MouseSceneY;
        lastTraX = getTranslateX();
        lastTraY = getTranslateY();
    }

    protected void released() {
        isDragging = false;
    }

    /**
     * 根据当前 mapX/mapY 和坐标转换函数，重新计算并设置 translateX/Y
     */
    protected void reloadTra() {
        setTranslateX(finalMapToTraX(mapX.doubleValue()));
        setTranslateY(finalMapToTraY(mapY.doubleValue()));
    }

    abstract protected void dragged(MouseEvent e);

    @Override
    public void unload() {
        if (!loaded) return;
        loaded = false;
    }

    public void setSize(double size) {
        TextSized.super.setSize(size);
    }
    
    protected void setSize(double width, double height) {
        // 设置尺寸（允许超过屏幕限制）
        setPrefSize(width, height);
        setMaxSize(width, height);
        setMinSize(width, height);
    }
    public <T extends Event> void addAndRegisterEventFilter(EventType<T> var1, EventHandler<? super T> var2) {
        addEventFilter(var1, var2);
        register(() -> removeEventFilter(var1, var2));
    }

    public void setName(String name) {
        this.name.set(name);
    }

    public void setLocation(double x, double y) {
        setMapX(x);
        setMapY(y);
    }

    public void setMapX(double mapX) {
        this.mapX.set(mapX);
    }

    public void setMapY(double mapY) {
        this.mapY.set(mapY);
    }

    public double getMapX() {
        return mapX.get();
    }

    public DoubleProperty mapXProperty() {
        return mapX;
    }

    public double getMapY() {
        return mapY.get();
    }

    public DoubleProperty mapYProperty() {
        return mapY;
    }
}