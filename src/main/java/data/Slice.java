package data;

import game.window.Camera;
import game.window.Stage;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.event.EventType;
import javafx.scene.control.Button;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.ScrollEvent;

public abstract class Slice extends Button implements LifeCycled, TextSized {
    protected boolean loaded=false;
    // 拖动相关字段
    private double dragStartMapX;
    private double dragStartMapY;
    private double mapX;
    private double mapY;
    boolean isDragging = false;
    StringProperty name = new SimpleStringProperty("");
    public Slice(double X, double Y) {
        super();
        name.addListener((observable, oldValue, newValue) -> setText(newValue));
        mapX = X;
        mapY = Y;
    }
    
    @Override
    public void load() {
        if (loaded) return;
        addFilters();
        loaded=true;
    }
    protected void addFilters() {
        EventHandler<ScrollEvent> scrolled = e -> game.Game.bus.publish(new post.camera.Scrolled(e));
        EventHandler<MouseEvent> pressed = e ->  {
            if (e.getButton() == MouseButton.PRIMARY  ) pressed(e);
            if (e.getButton() == MouseButton.SECONDARY) game.Game.bus.publish(new post.camera.Pressed(e));
        };
        EventHandler<MouseEvent> dragged = e -> {
            if (e.getButton() == MouseButton.PRIMARY  ) dragged(e);
            if (e.getButton() == MouseButton.SECONDARY) game.Game.bus.publish(new post.camera.Dragged(e));
        };
        EventHandler<MouseEvent> released = e -> {
            if (e.getButton() == MouseButton.PRIMARY  ) released(e);
            if (e.getButton() == MouseButton.SECONDARY) game.Game.bus.publish(new post.camera.Released(e));
        };
//        addAndRegisterEventFilter(MouseEvent.MOUSE_ENTERED, enter);
//        addAndRegisterEventFilter(MouseEvent.MOUSE_EXITED, exit);
        addAndRegisterEventFilter(ScrollEvent.SCROLL,scrolled);
        addAndRegisterEventFilter(MouseEvent.MOUSE_PRESSED,pressed);
        addAndRegisterEventFilter(MouseEvent.MOUSE_DRAGGED,dragged);
        addAndRegisterEventFilter(MouseEvent.MOUSE_RELEASED,released);
    }
    
    private void released(MouseEvent e) {
        // 保存最终的地图坐标
        mapX = Camera.traToMapX(getTranslateX());
        mapY = Camera.traToMapY(getTranslateY());
        isDragging = false;
    }
    
    private void dragged(MouseEvent e) {
        if (!isDragging) return;
        
        // 将当前鼠标屏幕坐标转换为地图坐标
        double currentMapX = Camera.traToMapX(e.getSceneX());
        double currentMapY = Camera.traToMapY(e.getSceneY());
        
        // 计算地图空间中的拖动距离
        double deltaX = (currentMapX - dragStartMapX)/Camera.zoom;
        double deltaY = (currentMapY - dragStartMapY)/Camera.zoom;
        
        // 更新位置：新地图坐标 → 屏幕坐标
        setTranslateX(Camera.mapToTraX(mapX + deltaX));
        setTranslateY(Camera.mapToTraY(mapY + deltaY));
    }
    
    private void pressed(MouseEvent e) {
        isDragging = true;
        dragStartMapX = Camera.traToMapX(e.getSceneX());
        dragStartMapY = Camera.traToMapY(e.getSceneY());
    }
    
    @Override
    public void unload() {
        if (!loaded) return;
        loaded=false;
    }
    public void setSize(double size) {
        TextSized.super.setSize(size);
    }
    public  <T extends Event> void addAndRegisterEventFilter(EventType<T> var1, EventHandler<? super T> var2){
        addEventFilter(var1,var2);
        register(() -> removeEventFilter(var1,var2));
    }
    public void setName(String name) {
        this.name.set(name);
    }
    
}
