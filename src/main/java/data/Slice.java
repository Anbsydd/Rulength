package data;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.event.EventType;
import javafx.scene.control.Button;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.ScrollEvent;

public abstract class Slice extends Button implements LifeCycled, TextSized {
    protected boolean loaded=false;
    // 拖动相关字段
    private double dragStartX;
    private double dragStartY;
    private double initialTranslateX;
    private double initialTranslateY;
    boolean isDragging = false;
    StringProperty name = new SimpleStringProperty("");
    public Slice() {
        super();
        name.addListener((observable, oldValue, newValue) -> setText(newValue));
        
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
            if (e.isPrimaryButtonDown()) pressed(e);
            if (e.isSecondaryButtonDown()) game.Game.bus.publish(new post.camera.Pressed(e));
        };
        EventHandler<MouseEvent> dragged = e -> {
            if (e.isPrimaryButtonDown()) dragged(e);
            if (e.isSecondaryButtonDown()) game.Game.bus.publish(new post.camera.Dragged(e));
        };
        EventHandler<MouseEvent> released = e -> {
            if (e.isPrimaryButtonDown()) isDragging = false;
            if (e.isSecondaryButtonDown()) game.Game.bus.publish(new post.camera.Released(e));
        };
//        addAndRegisterEventFilter(MouseEvent.MOUSE_ENTERED, enter);
//        addAndRegisterEventFilter(MouseEvent.MOUSE_EXITED, exit);
        addAndRegisterEventFilter(ScrollEvent.SCROLL,scrolled);
        addAndRegisterEventFilter(MouseEvent.MOUSE_PRESSED,pressed);
        addAndRegisterEventFilter(MouseEvent.MOUSE_DRAGGED,dragged);
        addAndRegisterEventFilter(MouseEvent.MOUSE_RELEASED,released);
    }
    
    private void dragged(MouseEvent e) {
        if (!isDragging) return;
        
        // 获取当前鼠标位置
        double currentX = e.getSceneX();
        double currentY = getScene().getWindow().getY() + getScene().getY() + getLocalToSceneTransform().getTy();
        
        // 计算拖动距离
        double deltaX = currentX - dragStartX;
        double deltaY = currentY - dragStartY;
        
        // 更新位置
        setTranslateX(initialTranslateX + deltaX);
        setTranslateY(initialTranslateY + deltaY);
    }
    
    private void pressed(MouseEvent e) {
        if (e.isPrimaryButtonDown()) {
            isDragging = true;
            dragStartX = e.getSceneX();
            dragStartY = e.getSceneY();
            initialTranslateX = getTranslateX();
            initialTranslateY = getTranslateY();
        }
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
